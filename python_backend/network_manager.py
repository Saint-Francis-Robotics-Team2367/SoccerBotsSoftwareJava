"""
Network Manager for ESP32 robot communication via UDP.
Handles discovery protocol and command transmission.
"""

import socket
import threading
import logging
import struct
import netifaces
from typing import Optional, Tuple

logger = logging.getLogger(__name__)


class NetworkManager:
    """Manages UDP communication with ESP32 robots"""
    
    # ESP32 Communication Constants
    DISCOVERY_PORT = 12345
    ESP32_UDP_PORT = 2367
    EXPECTED_WIFI_NETWORK = "WATCHTOWER"
    
    def __init__(self):
        self.udp_socket: Optional[socket.socket] = None
        self.discovery_socket: Optional[socket.socket] = None
        self.is_connected_to_network = False
        self.current_ssid = ""
        self._lock = threading.Lock()
        
        self._initialize_udp_socket()
        self._initialize_discovery_socket()
        self._check_current_network_status()
    
    def _initialize_udp_socket(self):
        """Initialize UDP socket for robot commands"""
        try:
            self.udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
            logger.info("UDP socket initialized for ESP32 communication")
        except Exception as e:
            logger.error(f"Failed to initialize UDP socket: {e}")
    
    def _initialize_discovery_socket(self):
        """Initialize UDP socket for robot discovery"""
        try:
            self.discovery_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.discovery_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.discovery_socket.bind(('', self.DISCOVERY_PORT))
            self.discovery_socket.settimeout(0.1)  # 100ms timeout for non-blocking receives
            logger.info(f"Discovery socket initialized on port {self.DISCOVERY_PORT}")
        except Exception as e:
            logger.error(f"Failed to initialize discovery socket: {e}")
    
    def _check_current_network_status(self):
        """Check current WiFi network connection"""
        try:
            # Get active network interfaces
            interfaces = netifaces.interfaces()
            for interface in interfaces:
                addrs = netifaces.ifaddresses(interface)
                if netifaces.AF_INET in addrs:
                    for addr in addrs[netifaces.AF_INET]:
                        if 'addr' in addr and not addr['addr'].startswith('127.'):
                            self.is_connected_to_network = True
                            logger.info(f"Connected to network on interface {interface}: {addr['addr']}")
                            return
            
            logger.warn("Not connected to any network")
        except Exception as e:
            logger.error(f"Failed to check network status: {e}")
    
    def send_robot_command(self, robot_name: str, target_ip: str,
                          left_x: int, left_y: int, right_x: int, right_y: int,
                          cross: bool = False, circle: bool = False,
                          square: bool = False, triangle: bool = False):
        """
        Send binary command data to ESP32 robot.
        Format: robotName(16 bytes) + axes(6 bytes) + buttons(2 bytes) = 24 bytes total
        """
        if not self.udp_socket:
            logger.error("UDP socket not initialized")
            return
        
        try:
            # Create 24-byte packet
            packet = bytearray(24)
            
            # Robot name (16 bytes, null-padded)
            name_bytes = robot_name.encode('utf-8')[:16]
            packet[0:len(name_bytes)] = name_bytes
            
            # Axes data (6 bytes) - values 0-255
            packet[16] = max(0, min(255, left_x))
            packet[17] = max(0, min(255, left_y))
            packet[18] = max(0, min(255, right_x))
            packet[19] = max(0, min(255, right_y))
            packet[20] = 125  # unused axis
            packet[21] = 125  # unused axis
            
            # Button data (2 bytes)
            button1 = 0
            if cross: button1 |= 0x01
            if circle: button1 |= 0x02
            if square: button1 |= 0x04
            if triangle: button1 |= 0x08
            
            packet[22] = button1
            packet[23] = 0  # unused buttons
            
            # Send UDP packet
            self.udp_socket.sendto(bytes(packet), (target_ip, self.ESP32_UDP_PORT))
            logger.debug(f"Sent command to robot '{robot_name}' at {target_ip}:{self.ESP32_UDP_PORT}")
            
        except Exception as e:
            logger.error(f"Failed to send robot command to {target_ip}: {e}")
    
    def send_game_status(self, robot_name: str, target_ip: str, status: str):
        """
        Send game status command to ESP32 robot.
        Format: "robotName:status" (text)
        """
        if not self.udp_socket:
            logger.error("UDP socket not initialized")
            return
        
        try:
            message = f"{robot_name}:{status}"
            data = message.encode('utf-8')
            
            self.udp_socket.sendto(data, (target_ip, self.ESP32_UDP_PORT))
            logger.info(f"Sent game status '{status}' to robot '{robot_name}' at {target_ip}")
            
        except Exception as e:
            logger.error(f"Failed to send game status to {target_ip}: {e}")
    
    def broadcast_emergency_stop(self, activate: bool = True):
        """Broadcast emergency stop to all robots on discovery port"""
        if not self.udp_socket:
            logger.error("UDP socket not initialized")
            return
        
        try:
            message = "ESTOP" if activate else "ESTOP_OFF"
            data = message.encode('utf-8')
            
            # Send to broadcast address on discovery port (always monitored by robots)
            self.udp_socket.sendto(data, ('<broadcast>', self.DISCOVERY_PORT))
            logger.info(f"Broadcast emergency stop: {message}")
            
        except Exception as e:
            logger.error(f"Failed to broadcast emergency stop: {e}")
    
    def receive_discovery_message(self) -> Optional[str]:
        """Receive discovery message from robots (non-blocking)"""
        if not self.discovery_socket:
            return None
        
        try:
            data, addr = self.discovery_socket.recvfrom(1024)
            message = data.decode('utf-8')
            return message
        except socket.timeout:
            return None
        except Exception as e:
            logger.debug(f"Discovery receive error: {e}")
            return None
    
    def get_network_subnet(self) -> Optional[str]:
        """Get current network subnet (e.g., '192.168.1')"""
        try:
            interfaces = netifaces.interfaces()
            for interface in interfaces:
                addrs = netifaces.ifaddresses(interface)
                if netifaces.AF_INET in addrs:
                    for addr in addrs[netifaces.AF_INET]:
                        if 'addr' in addr and addr['addr'].startswith('192.168'):
                            parts = addr['addr'].split('.')
                            if len(parts) >= 3:
                                return f"{parts[0]}.{parts[1]}.{parts[2]}"
            return None
        except Exception as e:
            logger.error(f"Failed to get network subnet: {e}")
            return None
    
    def shutdown(self):
        """Cleanup network resources"""
        if self.udp_socket:
            self.udp_socket.close()
        if self.discovery_socket:
            self.discovery_socket.close()
        logger.info("Network manager shutdown complete")
