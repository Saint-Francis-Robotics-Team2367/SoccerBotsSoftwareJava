"""
Headless launcher for the Python robot control system with HTTP API.
Used when running with Electron frontend or standalone.
"""

import logging
import sys
import signal
from network_manager import NetworkManager
from robot_manager import RobotManager
from controller_manager import ControllerManager
from api_server import ApiServer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)


def main():
    """Main entry point for the headless Python backend"""
    logger.info("Starting SoccerBots Control System (Python Backend - Headless Mode)")
    
    try:
        # Parse port from args if provided
        api_port = 8080
        if len(sys.argv) > 1:
            try:
                api_port = int(sys.argv[1])
            except ValueError:
                logger.warning(f"Invalid port argument, using default: {api_port}")
        
        # Initialize network manager
        network_manager = NetworkManager()
        
        # Initialize robot manager
        robot_manager = RobotManager(network_manager)
        robot_manager.start_discovery()
        
        # Initialize controller manager
        controller_manager = ControllerManager(robot_manager)
        
        # Create API server
        api_server = ApiServer(robot_manager, controller_manager, network_manager)
        
        # Setup shutdown handler
        def shutdown_handler(signum, frame):
            logger.info("Shutting down...")
            controller_manager.shutdown()
            robot_manager.shutdown()
            network_manager.shutdown()
            logger.info("Shutdown complete")
            sys.exit(0)
        
        signal.signal(signal.SIGINT, shutdown_handler)
        signal.signal(signal.SIGTERM, shutdown_handler)
        
        logger.info("System initialized successfully")
        logger.info(f"API server running on http://localhost:{api_port}")
        logger.info(f"WebSocket endpoint: ws://localhost:{api_port}/")
        
        # Start API server (blocking)
        api_server.start(api_port)
        
    except Exception as e:
        logger.error(f"Failed to start headless launcher: {e}", exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()
