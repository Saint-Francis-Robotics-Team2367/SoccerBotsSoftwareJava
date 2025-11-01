"""
SoccerBots Control System - Python Backend

A Python backend for controlling ESP32-based soccer robots with PlayStation controller support.
"""

__version__ = "1.0.0"
__author__ = "SoccerBots Team"

from .network_manager import NetworkManager
from .robot_manager import RobotManager
from .robot import Robot, ESP32Command
from .controller_manager import ControllerManager
from .controller_input import ControllerInput, GameController
from .api_server import ApiServer

__all__ = [
    'NetworkManager',
    'RobotManager',
    'Robot',
    'ESP32Command',
    'ControllerManager',
    'ControllerInput',
    'GameController',
    'ApiServer'
]
