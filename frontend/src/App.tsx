import { useState, useEffect } from "react";
import { ConnectionPanel } from "./components/ConnectionPanel";
import { NetworkAnalysis } from "./components/NetworkAnalysis";
import { ControlPanel } from "./components/ControlPanel";
import { ServiceLog } from "./components/ServiceLog";
import { TerminalMonitor } from "./components/TerminalMonitor";
import { Activity } from "lucide-react";
import { toast, Toaster } from "sonner";
import { apiService, Robot, LogEntry } from "./services/api";

export default function App() {
  const [robots, setRobots] = useState<Robot[]>([]);
  const [networkData, setNetworkData] = useState<any[]>([]);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [terminalLines, setTerminalLines] = useState<string[]>([
    "$ Robot Control System v3.2.1 initialized",
    "$ Loading driver modules...",
    "$ Network interface configured",
    "$ Connecting to backend API...",
  ]);
  const [emergencyActive, setEmergencyActive] = useState(false);

  // Initialize API connection
  useEffect(() => {
    console.log("[App] Initializing API connection...");
    apiService.connectWebSocket();

    // Subscribe to logs
    const unsubscribe = apiService.subscribeToLogs((newLogs) => {
      setLogs(newLogs);
    });

    // Set up real-time event listeners
    const unsubRobotConnected = apiService.on("robot_connected", (data) => {
      console.log("[WebSocket] Robot connected:", data);
      fetchRobots();
    });

    const unsubRobotDisconnected = apiService.on("robot_disconnected", (data) => {
      console.log("[WebSocket] Robot disconnected:", data);
      fetchRobots();
    });

    const unsubEmergency = apiService.on("emergency_stop", (data) => {
      console.log("[WebSocket] Emergency stop:", data);
      setEmergencyActive(data.active);
    });

    addTerminalLine("$ WebSocket connection established");
    addTerminalLine("$ System ready");

    // Initial data fetch
    fetchRobots();
    startNetworkPolling();

    return () => {
      unsubscribe();
      unsubRobotConnected();
      unsubRobotDisconnected();
      unsubEmergency();
    };
  }, []);

  const addTerminalLine = (line: string) => {
    setTerminalLines((prev) => [...prev, line]);
  };

  const fetchRobots = async () => {
    try {
      const robotsData = await apiService.getRobots();
      setRobots(robotsData);
      console.log("[App] Fetched robots:", robotsData);
    } catch (error) {
      console.error("[App] Failed to fetch robots:", error);
      addTerminalLine("$ Error: Failed to fetch robot list");
    }
  };

  const startNetworkPolling = () => {
    // Initial network data
    const initialData = Array.from({ length: 6 }, (_, i) => ({
      time: `${i * 5}s`,
      latency: Math.floor(Math.random() * 10) + 10,
      bandwidth: Math.floor(Math.random() * 10) + 45,
    }));
    setNetworkData(initialData);

    // Poll network stats every 5 seconds
    const interval = setInterval(async () => {
      try {
        const stats = await apiService.getNetworkStats();
        setNetworkData((prev) => {
          const newData = [...prev.slice(1)];
          const lastTime = parseInt(prev[prev.length - 1].time);
          newData.push({
            time: `${lastTime + 5}s`,
            latency: stats.latency,
            bandwidth: stats.bandwidth,
          });
          return newData;
        });
      } catch (error) {
        console.error("[App] Failed to fetch network stats:", error);
      }
    }, 5000);

    return () => clearInterval(interval);
  };

  const handleConnect = async (id: string) => {
    try {
      const robot = robots.find((r) => r.id === id);
      if (!robot) return;

      const isConnecting = robot.status === "disconnected";

      if (isConnecting) {
        await apiService.connectRobot(id);
        toast.success(`${robot.name} connected successfully`);
        addTerminalLine(`$ Connecting to ${robot.name}...`);
        addTerminalLine(`$ ${robot.name} connected successfully`);
      } else {
        await apiService.disconnectRobot(id);
        toast.success(`${robot.name} disconnected`);
        addTerminalLine(`$ Disconnecting from ${robot.name}...`);
        addTerminalLine(`$ ${robot.name} disconnected`);
      }

      // Refresh robot list
      await fetchRobots();
    } catch (error) {
      console.error("[App] Failed to toggle connection:", error);
      toast.error("Failed to toggle connection");
    }
  };

  const handleRefresh = async () => {
    try {
      toast.info("Scanning for robots...");
      addTerminalLine("$ Refreshing robot list...");
      await apiService.refreshRobots();

      // Wait a bit for discovery, then fetch
      setTimeout(async () => {
        await fetchRobots();
        addTerminalLine("$ Scan complete");
      }, 2000);
    } catch (error) {
      console.error("[App] Failed to refresh robots:", error);
      toast.error("Failed to refresh robots");
    }
  };

  const handleEmergencyStop = async () => {
    try {
      if (!emergencyActive) {
        await apiService.activateEmergencyStop();
        setEmergencyActive(true);
        toast.error("EMERGENCY STOP ACTIVATED");
        addTerminalLine("$ !!! EMERGENCY STOP ACTIVATED !!!");
        addTerminalLine("$ All robot operations halted");
      } else {
        await apiService.deactivateEmergencyStop();
        setEmergencyActive(false);
        toast.success("Emergency stop deactivated");
        addTerminalLine("$ Emergency stop deactivated");
        addTerminalLine("$ Systems resuming normal operation");
      }
    } catch (error) {
      console.error("[App] Failed to toggle emergency stop:", error);
      toast.error("Failed to toggle emergency stop");
    }
  };

  const handleDisable = async (id: string) => {
    try {
      const robot = robots.find((r) => r.id === id);
      if (!robot) return;

      const isEnabling = robot.disabled;

      if (isEnabling) {
        await apiService.enableRobot(id);
        toast.success(`${robot.name} has been enabled`);
        addTerminalLine(`$ Enabling ${robot.name}...`);
        addTerminalLine(`$ ${robot.name} has been enabled`);
      } else {
        await apiService.disableRobot(id);
        toast.info(`${robot.name} has been disabled`);
        addTerminalLine(`$ Disabling ${robot.name}...`);
        addTerminalLine(`$ ${robot.name} has been disabled`);
      }

      // Refresh robot list
      await fetchRobots();
    } catch (error) {
      console.error("[App] Failed to toggle robot state:", error);
      toast.error("Failed to toggle robot state");
    }
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
