#!/usr/bin/env python3
"""
Test script to simulate ESP32 robot discovery and verify pairing functionality.
This script acts as a simulated robot that sends discovery pings.
"""

import socket
import time
import sys

DISCOVERY_PORT = 12345
COMMAND_PORT = 2367

def simulate_robot(robot_name: str, robot_ip: str = "127.0.0.1"):
    """Simulate a robot sending discovery pings"""
    
    # Create UDP socket for sending discovery pings
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    # Don't use broadcast in test environment - use localhost
    # sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    
    # Create UDP socket for receiving commands
    # Security note: Binds to all interfaces ('') to simulate ESP32 robot behavior
    # This is a test utility only - not used in production
    cmd_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    cmd_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    cmd_sock.bind(('', COMMAND_PORT))
    cmd_sock.settimeout(0.1)
    
    print(f"Simulating robot: {robot_name}")
    print(f"IP Address: {robot_ip}")
    print(f"Sending discovery pings on port {DISCOVERY_PORT}")
    print(f"Listening for commands on port {COMMAND_PORT}")
    print("Press Ctrl+C to stop\n")
    
    try:
        while True:
            # Send discovery ping to localhost (instead of broadcast)
            message = f"DISCOVER:{robot_name}:{robot_ip}"
            sock.sendto(message.encode('utf-8'), ('127.0.0.1', DISCOVERY_PORT))
            print(f"[{time.strftime('%H:%M:%S')}] Sent discovery ping: {message}")
            
            # Listen for commands (non-blocking)
            try:
                data, addr = cmd_sock.recvfrom(1024)
                
                # Check if it's a binary command (24 bytes)
                if len(data) == 24:
                    robot_name_received = data[0:16].decode('utf-8').rstrip('\x00')
                    left_x = data[16]
                    left_y = data[17]
                    right_x = data[18]
                    right_y = data[19]
                    buttons = data[22]
                    
                    if robot_name_received == robot_name:
                        print(f"[{time.strftime('%H:%M:%S')}] ✓ Movement command from {addr}: LX={left_x} LY={left_y} RX={right_x} RY={right_y} BTN={buttons}")
                else:
                    # Text command
                    text_cmd = data.decode('utf-8')
                    print(f"[{time.strftime('%H:%M:%S')}] ✓ Text command from {addr}: {text_cmd}")
                    
                    if text_cmd == "ESTOP":
                        print(f"[{time.strftime('%H:%M:%S')}] ⚠️  EMERGENCY STOP ACTIVATED")
                    elif text_cmd == "ESTOP_OFF":
                        print(f"[{time.strftime('%H:%M:%S')}] ✓ Emergency stop released")
                    elif text_cmd.startswith(robot_name + ":teleop"):
                        print(f"[{time.strftime('%H:%M:%S')}] ✓ Teleop mode enabled")
                    elif text_cmd.startswith(robot_name + ":standby"):
                        print(f"[{time.strftime('%H:%M:%S')}] ✓ Standby mode enabled")
                        
            except socket.timeout:
                pass  # No command received, that's fine
            except Exception as e:
                print(f"Error receiving command: {e}")
            
            # Wait 2 seconds before next discovery ping
            time.sleep(2)
            
    except KeyboardInterrupt:
        print("\n\nStopping robot simulation...")
        sock.close()
        cmd_sock.close()

if __name__ == "__main__":
    robot_name = "TestBot1"
    robot_ip = "127.0.0.1"
    
    if len(sys.argv) > 1:
        robot_name = sys.argv[1]
    if len(sys.argv) > 2:
        robot_ip = sys.argv[2]
    
    simulate_robot(robot_name, robot_ip)
