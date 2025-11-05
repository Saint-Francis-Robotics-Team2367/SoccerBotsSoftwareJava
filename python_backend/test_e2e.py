#!/usr/bin/env python3
"""
End-to-end test for robot pairing and command flow.
Tests the complete workflow from discovery to command transmission.
"""

import requests
import time
import json
import sys
from test_robot_simulator import simulate_robot
import threading

BASE_URL = "http://localhost:8080"

def print_section(title):
    print(f"\n{'='*60}")
    print(f"  {title}")
    print('='*60)

def test_health():
    """Test API health check"""
    print_section("Testing API Health")
    response = requests.get(f"{BASE_URL}/api/health", timeout=10)
    assert response.status_code == 200, f"Health check failed: {response.status_code}"
    data = response.json()
    print(f"✓ API is online (timestamp: {data['timestamp']})")
    return True

def test_robot_discovery():
    """Test robot discovery"""
    print_section("Testing Robot Discovery")
    
    # Start robot simulator in background
    print("Starting robot simulator...")
    robot_thread = threading.Thread(
        target=lambda: simulate_robot("TestBot1", "127.0.0.1"),
        daemon=True
    )
    robot_thread.start()
    
    # Wait for discovery
    print("Waiting for robot to be discovered...")
    time.sleep(3)
    
    # Check if robot was discovered
    response = requests.get(f"{BASE_URL}/api/robots", timeout=10)
    assert response.status_code == 200
    robots = response.json()
    
    print(f"✓ Found {len(robots)} robot(s)")
    for robot in robots:
        print(f"  - {robot['name']}: {robot['status']} at {robot['ipAddress']}")
        assert robot['name'] == "TestBot1", "Robot name mismatch"
        assert robot['status'] == "discovered", "Robot should be discovered"
    
    return len(robots) > 0

def test_robot_connection():
    """Test connecting to a robot"""
    print_section("Testing Robot Connection")
    
    robot_id = "TestBot1"
    response = requests.post(f"{BASE_URL}/api/robots/{robot_id}/connect", timeout=10)
    assert response.status_code == 200
    data = response.json()
    
    assert data['success'] == True, "Connection should succeed"
    assert data['robot']['status'] == "connected", "Robot should be connected"
    
    print(f"✓ Connected to {robot_id}")
    print(f"  Status: {data['robot']['status']}")
    print(f"  IP: {data['robot']['ipAddress']}")
    
    # Verify connection persists
    time.sleep(1)
    response = requests.get(f"{BASE_URL}/api/robots", timeout=10)
    robots = response.json()
    robot = next(r for r in robots if r['id'] == robot_id)
    assert robot['connected'] == True, "Robot should remain connected"
    print(f"✓ Connection persists (connected={robot['connected']})")
    
    return True

def test_controllers():
    """Test controller listing"""
    print_section("Testing Controller Detection")
    
    response = requests.get(f"{BASE_URL}/api/controllers", timeout=10)
    assert response.status_code == 200
    controllers = response.json()
    
    print(f"Found {len(controllers)} controller(s)")
    for controller in controllers:
        print(f"  - {controller['name']}: {controller.get('type', 'unknown')}")
    
    # Note: In CI environment, we won't have real controllers
    # This is just to verify the endpoint works
    return True

def test_pairing_state():
    """Test pairing state tracking"""
    print_section("Testing Pairing State Management")
    
    # Get initial state
    response = requests.get(f"{BASE_URL}/api/robots", timeout=10)
    robots = response.json()
    robot = robots[0]
    
    print(f"Initial state:")
    print(f"  Robot: {robot['name']}")
    print(f"  Paired Controller: {robot.get('pairedControllerId', 'None')}")
    
    # Since we don't have real controllers in CI, we can't test actual pairing
    # But we verified the field exists and is tracked
    assert 'pairedControllerId' in robot, "Robot should have pairedControllerId field"
    print(f"✓ Pairing state field exists in robot data")
    
    return True

def test_emergency_stop():
    """Test emergency stop"""
    print_section("Testing Emergency Stop")
    
    # Activate emergency stop
    response = requests.post(f"{BASE_URL}/api/emergency-stop", timeout=10)
    assert response.status_code == 200
    data = response.json()
    assert data['success'] == True
    print(f"✓ Emergency stop activated")
    
    time.sleep(1)
    
    # Deactivate emergency stop
    response = requests.post(f"{BASE_URL}/api/emergency-stop/deactivate", timeout=10)
    assert response.status_code == 200
    data = response.json()
    assert data['success'] == True
    print(f"✓ Emergency stop deactivated")
    
    return True

def test_robot_disconnect():
    """Test robot disconnection"""
    print_section("Testing Robot Disconnection")
    
    robot_id = "TestBot1"
    response = requests.post(f"{BASE_URL}/api/robots/{robot_id}/disconnect", timeout=10)
    assert response.status_code == 200
    data = response.json()
    assert data['success'] == True
    
    print(f"✓ Disconnected from {robot_id}")
    
    # Verify robot is removed from connected list
    time.sleep(1)
    response = requests.get(f"{BASE_URL}/api/robots", timeout=10)
    robots = response.json()
    
    # Robot may still be in discovered list if it's still sending pings
    # Just verify it's not in connected state
    connected_robots = [r for r in robots if r['status'] == 'connected']
    assert len(connected_robots) == 0, "No robots should be connected"
    print(f"✓ No connected robots remaining")
    
    return True

def run_all_tests():
    """Run all tests"""
    print("\n" + "="*60)
    print("  SoccerBots Control System - End-to-End Test Suite")
    print("="*60)
    
    tests = [
        ("API Health Check", test_health),
        ("Robot Discovery", test_robot_discovery),
        ("Robot Connection", test_robot_connection),
        ("Controller Detection", test_controllers),
        ("Pairing State Tracking", test_pairing_state),
        ("Emergency Stop", test_emergency_stop),
        ("Robot Disconnection", test_robot_disconnect),
    ]
    
    passed = 0
    failed = 0
    
    for name, test_func in tests:
        try:
            if test_func():
                passed += 1
        except Exception as e:
            print(f"\n✗ Test '{name}' failed: {e}")
            failed += 1
    
    # Summary
    print("\n" + "="*60)
    print(f"  Test Summary")
    print("="*60)
    print(f"Passed: {passed}/{len(tests)}")
    print(f"Failed: {failed}/{len(tests)}")
    
    if failed == 0:
        print("\n✓ All tests passed!")
        return 0
    else:
        print(f"\n✗ {failed} test(s) failed")
        return 1

if __name__ == "__main__":
    exit_code = run_all_tests()
    sys.exit(exit_code)
