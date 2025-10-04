import { AlertTriangle, Play, Pause, RotateCcw } from "lucide-react";
import { Button } from "./ui/button";
import { useState, useEffect } from "react";

interface ControlPanelProps {
  onEmergencyStop: () => void;
  emergencyActive: boolean;
}

export function ControlPanel({ onEmergencyStop, emergencyActive }: ControlPanelProps) {
  const [time, setTime] = useState(0);
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (isRunning) {
      interval = setInterval(() => {
        setTime((prev) => prev + 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [isRunning]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const handleReset = () => {
    setTime(0);
    setIsRunning(false);
  };

  return (
    <div className="h-full backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 flex flex-col">
      <h2 className="text-cyan-400 mb-4 shrink-0">Control Panel</h2>
      
      <div className="flex flex-col gap-4">
      {/* Timer */}
      <div className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-lg p-4">
        <div className="text-center mb-3">
          <div className="text-sm text-gray-300 mb-2">Match Timer</div>
          <div className="text-4xl font-mono text-cyan-400">{formatTime(time)}</div>
        </div>
        <div className="flex gap-2">
          <Button
            size="sm"
            onClick={() => setIsRunning(!isRunning)}
            className="flex-1 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50"
          >
            {isRunning ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
          </Button>
          <Button
            size="sm"
            onClick={handleReset}
            className="bg-white/10 hover:bg-white/20 text-white border border-white/20"
          >
            <RotateCcw className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Emergency Stop */}
      <div className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-lg p-4">
        <div className="text-center mb-3">
          <div className="text-sm text-gray-300 mb-2">Emergency Controls</div>
        </div>
        <Button
          size="lg"
          onClick={onEmergencyStop}
          className={`w-full h-24 transition-all ${
            emergencyActive
              ? "bg-red-600 hover:bg-red-700 text-white shadow-lg shadow-red-500/50 animate-pulse"
              : "bg-red-500/20 hover:bg-red-500/30 text-red-400 border-2 border-red-500/50"
          }`}
        >
          <div className="flex flex-col items-center gap-2">
            <AlertTriangle className="h-8 w-8" />
            <span className="text-lg">EMERGENCY STOP</span>
          </div>
        </Button>
        {emergencyActive && (
          <div className="mt-3 text-center text-red-400 text-sm animate-pulse">
            ⚠️ Emergency Stop Active
          </div>
        )}
      </div>
      </div>
    </div>
  );
}
