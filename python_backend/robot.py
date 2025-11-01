"""
Robot data model representing an ESP32 robot.
"""

import time
from typing import Optional


class Robot:
    """Represents an ESP32 robot with its connection state and properties"""
    
    def __init__(self, robot_id: str, name: str, ip_address: str, status: str = "discovered"):
        self.id = robot_id
        self.name = name
        self.ip_address = ip_address
        self.status = status  # "discovered", "connected", "disconnected"
        self.connected = False
        self.last_seen_time = time.time()
        self.last_command_time = 0
        self.paired_controller_id: Optional[str] = None
    
    def update_last_seen_time(self):
        """Update the last time robot was seen"""
        self.last_seen_time = time.time()
    
    def update_last_command_time(self):
        """Update the last time a command was sent to robot"""
        self.last_command_time = time.time()
    
    def set_connected(self, connected: bool):
        """Set robot connection status"""
        self.connected = connected
        self.status = "connected" if connected else "disconnected"
    
    def set_ip_address(self, ip_address: str):
        """Update robot IP address"""
        self.ip_address = ip_address
    
    def set_paired_controller(self, controller_id: Optional[str]):
        """Set paired controller ID"""
        self.paired_controller_id = controller_id
    
    def to_dict(self) -> dict:
        """Convert robot to dictionary for API responses"""
        return {
            'id': self.id,
            'name': self.name,
            'ipAddress': self.ip_address,
            'status': self.status,
            'connected': self.connected,
            'signal': 85,  # Mock signal strength
            'disabled': False,
            'pairedControllerId': self.paired_controller_id,
            'lastSeenTime': self.last_seen_time,
            'lastCommandTime': self.last_command_time
        }


class ESP32Command:
    """Represents an ESP32 robot command with normalized controller input"""
    
    def __init__(self, robot_name: str, left_x: int, left_y: int,
                 right_x: int, right_y: int,
                 cross: bool = False, circle: bool = False,
                 square: bool = False, triangle: bool = False):
        self.robot_name = robot_name
        self.left_x = left_x  # 0-255
        self.left_y = left_y  # 0-255
        self.right_x = right_x  # 0-255
        self.right_y = right_y  # 0-255
        self.cross = cross
        self.circle = circle
        self.square = square
        self.triangle = triangle
    
    @staticmethod
    def from_controller_input(robot_name: str,
                             left_stick_x: float, left_stick_y: float,
                             right_stick_x: float, right_stick_y: float,
                             cross: bool = False, circle: bool = False,
                             square: bool = False, triangle: bool = False):
        """
        Create command from normalized controller input (-1.0 to 1.0).
        Converts to ESP32 format (0-255, with 127 as center).
        """
        # Convert from -1.0 to 1.0 range to 0-255 range
        left_x = int((left_stick_x + 1.0) * 127.5)
        left_y = int((left_stick_y + 1.0) * 127.5)
        right_x = int((right_stick_x + 1.0) * 127.5)
        right_y = int((right_stick_y + 1.0) * 127.5)
        
        return ESP32Command(robot_name, left_x, left_y, right_x, right_y,
                          cross, circle, square, triangle)
    
    @staticmethod
    def create_stop_command(robot_name: str):
        """Create a stop command (all axes centered at 127)"""
        return ESP32Command(robot_name, 127, 127, 127, 127)
    
    def has_movement(self) -> bool:
        """Check if command has significant movement (threshold 5)"""
        threshold = 5
        center = 127
        return (abs(self.left_x - center) > threshold or
                abs(self.left_y - center) > threshold or
                abs(self.right_x - center) > threshold or
                abs(self.right_y - center) > threshold)
