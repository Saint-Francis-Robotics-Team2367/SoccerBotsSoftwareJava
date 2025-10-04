import { useState, useEffect } from "react";
import { ConnectionPanel } from "./components/ConnectionPanel";
import { NetworkAnalysis } from "./components/NetworkAnalysis";
import { ControlPanel } from "./components/ControlPanel";
import { ServiceLog } from "./components/ServiceLog";
import { TerminalMonitor } from "./components/TerminalMonitor";
import { Activity } from "lucide-react";
import { toast, Toaster } from "sonner@2.0.3";

export default function App() {
  const [robots, setRobots] = useState([
    { id: "1", name: "Robot Alpha", status: "connected" as const, ipAddress: "192.168.1.101", signal: 87, disabled: false },
    { id: "2", name: "Robot Beta", status: "disconnected" as const, ipAddress: "192.168.1.102", signal: 0, disabled: false },
    { id: "3", name: "Robot Gamma", status: "disconnected" as const, ipAddress: "192.168.1.103", signal: 0, disabled: false },
  ]);

  const [networkData, setNetworkData] = useState([
    { time: "0s", latency: 12, bandwidth: 45 },
    { time: "5s", latency: 15, bandwidth: 48 },
    { time: "10s", latency: 10, bandwidth: 52 },
    { time: "15s", latency: 18, bandwidth: 47 },
    { time: "20s", latency: 14, bandwidth: 50 },
    { time: "25s", latency: 11, bandwidth: 55 },
  ]);

  const [logs, setLogs] = useState([
    { id: "1", timestamp: "10:23:15", level: "success" as const, message: "Robot Alpha connected successfully" },
    { id: "2", timestamp: "10:23:17", level: "info" as const, message: "Network analysis started" },
    { id: "3", timestamp: "10:23:45", level: "warning" as const, message: "High latency detected on Robot Alpha" },
    { id: "4", timestamp: "10:24:02", level: "info" as const, message: "System health check completed" },
  ]);

  const [terminalLines, setTerminalLines] = useState([
    "$ Robot Control System v3.2.1 initialized",
    "$ Loading driver modules...",
    "$ Network interface eth0 configured",
    "$ Scanning for available robots...",
    "$ Robot Alpha detected at 192.168.1.101",
    "$ Establishing secure connection...",
    "$ Connection established. Status: Active",
  ]);

  const [emergencyActive, setEmergencyActive] = useState(false);

  // Simulate network data updates
  useEffect(() => {
    const interval = setInterval(() => {
      setNetworkData((prev) => {
        const newData = [...prev.slice(1)];
        const lastTime = parseInt(prev[prev.length - 1].time);
        newData.push({
          time: `${lastTime + 5}s`,
          latency: Math.floor(Math.random() * 10) + 10,
          bandwidth: Math.floor(Math.random() * 10) + 45,
        });
        return newData;
      });
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleConnect = (id: string) => {
    setRobots((prev) =>
      prev.map((robot) => {
        if (robot.id === id) {
          const newStatus = robot.status === "connected" ? "disconnected" : "connected";
          const newSignal = newStatus === "connected" ? Math.floor(Math.random() * 30) + 70 : 0;
          
          const timestamp = new Date().toLocaleTimeString();
          const message = newStatus === "connected" 
            ? `${robot.name} connected successfully`
            : `${robot.name} disconnected`;
          
          setLogs((prevLogs) => [
            ...prevLogs,
            {
              id: Date.now().toString(),
              timestamp,
              level: newStatus === "connected" ? "success" : "info",
              message,
            },
          ]);

          setTerminalLines((prev) => [
            ...prev,
            `$ ${newStatus === "connected" ? "Connecting to" : "Disconnecting from"} ${robot.name}...`,
            `$ ${message}`,
          ]);

          toast.success(message);

          return { ...robot, status: newStatus, signal: newSignal };
        }
        return robot;
      })
    );
  };

  const handleRefresh = () => {
    toast.info("Scanning for robots...");
    setTerminalLines((prev) => [...prev, "$ Refreshing robot list...", "$ Scan complete"]);
    
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prevLogs) => [
      ...prevLogs,
      {
        id: Date.now().toString(),
        timestamp,
        level: "info",
        message: "Robot scan completed",
      },
    ]);
  };

  const handleEmergencyStop = () => {
    setEmergencyActive(!emergencyActive);
    const timestamp = new Date().toLocaleTimeString();
    
    if (!emergencyActive) {
      toast.error("EMERGENCY STOP ACTIVATED");
      setTerminalLines((prev) => [...prev, "$ !!! EMERGENCY STOP ACTIVATED !!!", "$ All robot operations halted"]);
      setLogs((prevLogs) => [
        ...prevLogs,
        {
          id: Date.now().toString(),
          timestamp,
          level: "error",
          message: "EMERGENCY STOP ACTIVATED - All operations halted",
        },
      ]);
    } else {
      toast.success("Emergency stop deactivated");
      setTerminalLines((prev) => [...prev, "$ Emergency stop deactivated", "$ Systems resuming normal operation"]);
      setLogs((prevLogs) => [
        ...prevLogs,
        {
          id: Date.now().toString(),
          timestamp,
          level: "success",
          message: "Emergency stop deactivated - Systems operational",
        },
      ]);
    }
  };

  const handleDisable = (id: string) => {
    setRobots((prev) =>
      prev.map((robot) => {
        if (robot.id === id) {
          const newDisabledState = !robot.disabled;
          const timestamp = new Date().toLocaleTimeString();
          const message = newDisabledState
            ? `${robot.name} has been disabled`
            : `${robot.name} has been enabled`;

          setLogs((prevLogs) => [
            ...prevLogs,
            {
              id: Date.now().toString(),
              timestamp,
              level: newDisabledState ? "warning" : "info",
              message,
            },
          ]);

          setTerminalLines((prev) => [
            ...prev,
            `$ ${newDisabledState ? "Disabling" : "Enabling"} ${robot.name}...`,
            `$ ${message}`,
          ]);

          toast(message, { 
            description: newDisabledState ? "Robot operations suspended" : "Robot ready for operations"
          });

          // If we're disabling and it's connected, disconnect it
          if (newDisabledState && robot.status === "connected") {
            return { ...robot, disabled: newDisabledState, status: "disconnected" as const, signal: 0 };
          }

          return { ...robot, disabled: newDisabledState };
        }
        return robot;
      })
    );
  };

  const handleClearLogs = () => {
    setLogs([]);
    toast.success("Service log cleared");
    setTerminalLines((prev) => [...prev, "$ Service log cleared"]);
  };

  return (
    <div className="min-h-screen p-6">
      <Toaster position="top-right" theme="dark" />
      
      {/* Header */}
      <div className="mb-6 backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-cyan-500/20 rounded-lg border border-cyan-500/50">
              <Activity className="h-6 w-6 text-cyan-400" />
            </div>
            <div>
              <h1 className="text-cyan-400">Robot Control Station</h1>
              <p className="text-sm text-gray-300">Advanced Driver Interface v3.2.1</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <div className="h-2 w-2 rounded-full bg-green-400 animate-pulse" />
              <span className="text-sm text-gray-300">System Online</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-12 gap-6">
        {/* Left Column - Connection Panel */}
        <div className="col-span-3 h-[calc(100vh-180px)] min-h-0">
          <ConnectionPanel robots={robots} onConnect={handleConnect} onRefresh={handleRefresh} onDisable={handleDisable} />
        </div>

        {/* Center Column */}
        <div className="col-span-6 flex flex-col gap-6 h-[calc(100vh-180px)] min-h-0">
          {/* Network Analysis */}
          <div className="h-64 min-h-0 shrink-0">
            <NetworkAnalysis data={networkData} />
          </div>

          {/* Terminal Monitor */}
          <div className="flex-1 min-h-0">
            <TerminalMonitor lines={terminalLines} />
          </div>
        </div>

        {/* Right Column */}
        <div className="col-span-3 flex flex-col gap-6 h-[calc(100vh-180px)] min-h-0">
          {/* Control Panel */}
          <div className="shrink-0">
            <ControlPanel onEmergencyStop={handleEmergencyStop} emergencyActive={emergencyActive} />
          </div>

          {/* Service Log */}
          <div className="flex-1 min-h-0">
            <ServiceLog logs={logs} onClear={handleClearLogs} />
          </div>
        </div>
      </div>
    </div>
  );
}
