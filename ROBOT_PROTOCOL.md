# ESP32 Robot Communication Protocol

## Overview

The SoccerBots system uses a dynamic discovery and port assignment protocol for minimal ESP32 code and robust connection management.

## Protocol Workflow

### 1. Robot Startup (ESP32)

```
1. Robot powers on
2. Connects to WiFi ("WATCHTOWER")
3. Starts listening on DISCOVERY_PORT (12345)
4. Sends discovery ping every 2 seconds until connected
5. Waits for port assignment from driver station
```

### 2. Discovery Ping (ESP32 → Driver Station)

**Format:** `DISCOVER:<robotId>:<IP>`

**Example:** `DISCOVER:Robot_Alpha:192.168.1.100`

**Port:** UDP 12345 (broadcast)

**Frequency:** Every 2 seconds when not connected

### 3. Port Assignment (Driver Station → ESP32)

**Format:** `PORT:<robotId>:<port>`

**Example:** `PORT:Robot_Alpha:12346`

**Port:** UDP 12345 (unicast to robot IP)

**Action:** Robot switches from discovery port to assigned port

### 4. Connected State

Once connected:
- Robot listens on **assigned port** for movement commands
- Robot expects commands every **5 seconds** (timeout)
- If timeout occurs, robot reverts to discovery mode
- Emergency stop persists across disconnects

## Message Types

### Discovery Messages (Port 12345)

| Message | Direction | Format | Purpose |
|---------|-----------|--------|---------|
| `DISCOVER:<id>:<ip>` | Robot → DS | Text | Robot announces presence |
| `PORT:<id>:<port>` | DS → Robot | Text | Assign unique port |
| `ESTOP` | DS → Robot | Text | Emergency stop all motors |
| `ESTOP_OFF` | DS → Robot | Text | Release emergency stop |

### Movement Commands (Assigned Port)

| Message | Direction | Format | Purpose |
|---------|-----------|--------|---------|
| Binary (24 bytes) | DS → Robot | Binary | Controller input |
| `<id>:teleop` | DS → Robot | Text | Enable movement |
| `<id>:standby` | DS → Robot | Text | Disable movement |

### Binary Movement Command Format (24 bytes)

```
Bytes 0-15:  Robot name (null-padded string)
Bytes 16-19: Stick axes (leftX, leftY, rightX, rightY) [0-255]
Bytes 20-21: Unused axes (reserved)
Bytes 22:    Button data (cross, circle, square, triangle)
Bytes 23:    Unused buttons (reserved)
```

## Port Assignment Strategy

**Driver Station:**
- Base port: 12346
- Each discovered robot gets: BASE + robot_index
- Port assignments persist for session
- Robot Alpha: 12346
- Robot Beta: 12347
- Robot Gamma: 12348
- etc.

**ESP32:**
- Listens on assigned port after receiving `PORT` message
- Falls back to discovery port (12345) on timeout
- Continues listening on assigned port until timeout

## Emergency Stop Behavior

### Activation

1. Driver station sends `ESTOP` to **all known robot IPs**
2. Sent to discovery port (12345) - always monitored
3. Robot immediately:
   - Sets emergency stop flag `true`
   - Calls `stopAllMotors()` (all motors to 0)
   - Ignores movement commands
   - Still processes discovery/status messages

### Deactivation

1. Driver station sends `ESTOP_OFF` to all robot IPs
2. Robot clears emergency stop flag
3. Movement commands resume normally

### Power Cycle Recovery

- Robot power cycle clears emergency stop state
- Robot starts fresh in discovery mode
- Driver station reassigns same port automatically

## Timeout and Reconnection

### Robot Timeout (5 seconds)

```cpp
if (connected && (now - lastCommandTime > 5000)) {
    // Revert to discovery mode
    connected = false;
    assignedPort = 0;
    stopAllMotors();
    udp.stop();
    udp.begin(DISCOVERY_PORT);
}
```

### Driver Station Behavior

- Passively listens for discovery pings
- Automatically reassigns ports to known robots
- No active "scanning" required
- Robots announce themselves

## State Machine

### ESP32 Robot States

```
[STARTUP] → WiFi connecting
    ↓
[DISCOVERY] → Sending pings, listening on 12345
    ↓ (receives PORT assignment)
[CONNECTED] → Listening on assigned port, processing commands
    ↓ (timeout or disconnect)
[DISCOVERY] → Back to discovery mode
```

### Emergency Stop States

```
Normal Operation → ESTOP received → Motors stopped, movement blocked
                                         ↓
                                    ESTOP_OFF received → Normal operation
                                         ↓
                                    Power cycle → Normal operation
```

## Code Size Optimization

**ESP32 firmware is kept minimal:**

- No WiFi management library (uses basic WiFi.h)
- No JSON parsing for discovery
- Simple string parsing with `String.split(':')`
- No state persistence (EEPROM/SPIFFS)
- Single UDP socket that switches ports
- Total added code: ~150 lines

## Driver Station Implementation

**RobotManager handles:**
- Discovery listening (500ms polling)
- Port assignment tracking
- Automatic reconnection
- Emergency stop broadcasting

**NetworkManager handles:**
- UDP socket management
- Discovery socket (port 12345)
- Command sockets (dynamic)
- Emergency stop delivery

## Example Session

```
1. Robot powers on
   ESP32: DISCOVER:Robot_Alpha:192.168.1.100 → Broadcast:12345

2. Driver Station receives ping
   DS: PORT:Robot_Alpha:12346 → 192.168.1.100:12345

3. Robot switches to assigned port
   ESP32: Listening on 12346
   DS: Sends movement commands → 192.168.1.100:12346

4. Emergency stop pressed
   DS: ESTOP → 192.168.1.100:12345
   ESP32: Motors stopped, flag set

5. Emergency stop released
   DS: ESTOP_OFF → 192.168.1.100:12345
   ESP32: Flag cleared, ready for commands

6. Robot loses connection (timeout)
   ESP32: Reverts to discovery mode
   ESP32: DISCOVER:Robot_Alpha:192.168.1.100 → Broadcast:12345
   DS: PORT:Robot_Alpha:12346 → 192.168.1.100:12345
   (Reconnected with same port)
```

## Benefits

✅ **Minimal ESP32 code** - No complex state management
✅ **Automatic discovery** - No manual IP configuration
✅ **Robust reconnection** - Handles power cycles gracefully
✅ **Emergency stop safety** - Works even when disconnected
✅ **Port persistence** - Same robot gets same port in session
✅ **Timeout handling** - Automatic fallback to discovery
✅ **Broadcast efficiency** - Robots announce themselves

## Testing Checklist

- [ ] Robot powers on and sends discovery pings
- [ ] Driver station assigns port successfully
- [ ] Robot receives and processes movement commands
- [ ] Emergency stop halts all motors immediately
- [ ] Emergency stop release allows movement
- [ ] Timeout triggers return to discovery mode
- [ ] Reconnection assigns same port
- [ ] Power cycle clears emergency stop
- [ ] Multiple robots get unique ports
- [ ] Network disconnect/reconnect handled gracefully
