"""
Robot Manager for ESP32 robot lifecycle and communication management.
"""

import logging
import threading
import time
from typing import Dict, List, Optional
from robot import Robot, ESP32Command
from network_manager import NetworkManager

logger = logging.getLogger(__name__)


class RobotManager:
    """Manages ESP32 robot discovery, connection, and command transmission"""
    
    def __init__(self, network_manager: NetworkManager):
        self.network_manager = network_manager
        self.connected_robots: Dict[str, Robot] = {}
        self.discovered_robots: Dict[str, Robot] = {}
        self.current_game_state = "standby"  # "standby" or "teleop"
        self.emergency_stop_active = False
        self._discovery_thread: Optional[threading.Thread] = None
        self._running = False
        self._lock = threading.Lock()
        
        logger.info("ESP32 Robot Manager initialized with discovery protocol")
    
    def start_discovery(self):
        """Start discovery service - listens for robot discovery pings"""
        if self._running:
            logger.warn("Discovery already running")
            return
        
        self._running = True
        self._discovery_thread = threading.Thread(target=self._discovery_loop, daemon=True)
        self._discovery_thread.start()
        logger.info(f"Discovery service started on port {NetworkManager.DISCOVERY_PORT}")
    
    def _discovery_loop(self):
        """Main discovery loop - listens for robot pings"""
        while self._running:
            try:
                message = self.network_manager.receive_discovery_message()
                if message and message.startswith("DISCOVER:"):
                    self._handle_discovery_ping(message)
            except Exception as e:
                logger.debug(f"Discovery listening error: {e}")
            
            time.sleep(0.5)  # Check every 500ms
    
    def _handle_discovery_ping(self, message: str):
        """
        Handle discovery ping from robot: "DISCOVER:<robotId>:<IP>"
        """
        parts = message.split(':')
        if len(parts) >= 3:
            robot_id = parts[1]
            ip_address = parts[2]
            
            with self._lock:
                # Add/update discovered robot
                robot = self.discovered_robots.get(robot_id)
                if robot is None:
                    robot = Robot(robot_id, robot_id, ip_address, "discovered")
                    self.discovered_robots[robot_id] = robot
                    logger.info(f"Discovered new robot: {robot_id} at {ip_address}")
                else:
                    robot.set_ip_address(ip_address)
                    robot.update_last_seen_time()
    
    def scan_for_robots(self):
        """Scan for robots - This method is now passive, just waits for discovery pings"""
        logger.info("Scanning for robots (passive discovery mode)")
        # Discovery is automatic via start_discovery()
    
    def get_discovered_robots(self) -> List[Robot]:
        """Get all discovered robots"""
        with self._lock:
            return list(self.discovered_robots.values())
    
    def get_connected_robots(self) -> List[Robot]:
        """Get all connected robots"""
        with self._lock:
            return list(self.connected_robots.values())
    
    def get_robot(self, robot_id: str) -> Optional[Robot]:
        """Get robot by ID from either connected or discovered lists"""
        with self._lock:
            robot = self.connected_robots.get(robot_id)
            if robot is None:
                robot = self.discovered_robots.get(robot_id)
            return robot
    
    def connect_discovered_robot(self, robot_id: str) -> Optional[Robot]:
        """Connect to a discovered robot"""
        with self._lock:
            robot = self.discovered_robots.get(robot_id)
            if robot:
                robot.set_connected(True)
                robot.status = "connected"
                self.connected_robots[robot_id] = robot
                logger.info(f"Connected to discovered robot: {robot_id} at {robot.ip_address}")
                return robot
            
            logger.warn(f"Cannot connect - robot not found in discovered robots: {robot_id}")
            return None
    
    def add_robot(self, robot_name: str, ip_address: str) -> Robot:
        """Add an ESP32 robot manually by IP address and name"""
        with self._lock:
            robot = Robot(robot_name, robot_name, ip_address, "connected")
            robot.set_connected(True)
            self.connected_robots[robot_name] = robot
            logger.info(f"Added ESP32 robot: {robot_name} at {ip_address}")
            return robot
    
    def remove_robot(self, robot_id: str):
        """Remove a robot from connected list"""
        with self._lock:
            if robot_id in self.connected_robots:
                del self.connected_robots[robot_id]
                logger.info(f"Removed robot: {robot_id}")
    
    def send_movement_command(self, robot_name: str,
                             left_stick_x: float, left_stick_y: float,
                             right_stick_x: float, right_stick_y: float):
        """Send movement command to robot using normalized values (-1.0 to 1.0)"""
        robot = self.get_robot(robot_name)
        if not robot:
            logger.warn(f"Robot not found: {robot_name}")
            return
        
        command = ESP32Command.from_controller_input(
            robot_name, left_stick_x, left_stick_y,
            right_stick_x, right_stick_y
        )
        
        self._send_esp32_command(robot, command)
    
    def send_stop_command(self, robot_name: str):
        """Send stop command to specific robot"""
        robot = self.get_robot(robot_name)
        if not robot:
            logger.warn(f"Robot not found: {robot_name}")
            return
        
        stop_command = ESP32Command.create_stop_command(robot_name)
        self._send_esp32_command(robot, stop_command)
    
    def _send_esp32_command(self, robot: Robot, command: ESP32Command):
        """Send ESP32 command to robot"""
        # Only send movement commands during teleop mode
        if self.current_game_state != "teleop":
            command = ESP32Command.create_stop_command(command.robot_name)
        
        self.network_manager.send_robot_command(
            command.robot_name,
            robot.ip_address,
            command.left_x,
            command.left_y,
            command.right_x,
            command.right_y,
            command.cross,
            command.circle,
            command.square,
            command.triangle
        )
        
        robot.update_last_command_time()
    
    def start_teleop(self):
        """Enable teleop mode - robots can receive movement commands"""
        self.current_game_state = "teleop"
        logger.info("Teleop mode started")
        
        # Send teleop status to all connected robots
        with self._lock:
            for robot in self.connected_robots.values():
                self.network_manager.send_game_status(robot.name, robot.ip_address, "teleop")
    
    def stop_teleop(self):
        """Disable teleop mode - robots stop moving"""
        self.current_game_state = "standby"
        logger.info("Teleop mode stopped")
        
        # Send standby status to all connected robots
        with self._lock:
            for robot in self.connected_robots.values():
                self.network_manager.send_game_status(robot.name, robot.ip_address, "standby")
                self.send_stop_command(robot.id)
    
    def emergency_stop_all(self):
        """Activate emergency stop for all robots"""
        self.emergency_stop_active = True
        self.network_manager.broadcast_emergency_stop(activate=True)
        logger.warn("Emergency stop activated for all robots")
    
    def deactivate_emergency_stop(self):
        """Deactivate emergency stop"""
        self.emergency_stop_active = False
        self.network_manager.broadcast_emergency_stop(activate=False)
        logger.info("Emergency stop deactivated")
    
    def shutdown(self):
        """Shutdown robot manager"""
        self._running = False
        if self._discovery_thread:
            self._discovery_thread.join(timeout=2)
        
        # Send stop commands to all robots
        with self._lock:
            for robot in self.connected_robots.values():
                self.send_stop_command(robot.id)
        
        logger.info("Robot manager shutdown complete")
