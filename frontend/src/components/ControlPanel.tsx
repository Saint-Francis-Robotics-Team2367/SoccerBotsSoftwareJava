import { AlertTriangle, Play, Pause, RotateCcw, Plus, Minus, Clock } from "lucide-react";
import { Button } from "./ui/button";
import { useState, useEffect } from "react";
import { apiService } from "../services/api";
import { toast } from "sonner";

interface ControlPanelProps {
  onEmergencyStop: () => void;
  emergencyActive: boolean;
}

export function ControlPanel({ onEmergencyStop, emergencyActive }: ControlPanelProps) {
  const [matchDuration, setMatchDuration] = useState(120); // Match duration in seconds
  const [timeRemaining, setTimeRemaining] = useState(matchDuration);
  const [isRunning, setIsRunning] = useState(false);

  // Local timer countdown logic
  useEffect(() => {
    let timer: NodeJS.Timeout | undefined;

    if (isRunning && timeRemaining > 0) {
      timer = setInterval(() => {
        setTimeRemaining((prevTime) => prevTime - 1);
      }, 1000);
    } else if (timeRemaining === 0 && isRunning) {
      setIsRunning(false);
      toast.error("⏱️ TIME EXPIRED! Emergency stop activated. Press E-STOP button to release.", {
        duration: 10000, // Show for 10 seconds
      });
      apiService.activateEmergencyStop(); // Activate emergency stop when timer runs out
    }

    return () => {
      if (timer) {
        clearInterval(timer);
      }
    };
  }, [isRunning, timeRemaining]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const handleStartStop = () => {
    if (isRunning) {
      setIsRunning(false);
      toast.info("Match stopped");
    } else {
      setIsRunning(true);
      toast.success("Match started!");
    }
  };

  const handleReset = () => {
    setIsRunning(false);
    setTimeRemaining(matchDuration);
    toast.info("Match reset");
  };

  const adjustDuration = (seconds: number) => {
    if (isRunning) {
      toast.warning("Cannot adjust duration while match is running");
      return;
    }

    const newDuration = Math.max(1, matchDuration + seconds);
    setMatchDuration(newDuration);
    toast.success(`Duration set to ${formatTime(newDuration)}`);
  };

  return (
    <div className="h-full backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 flex flex-col">
      <h2 className="text-cyan-400 mb-4 shrink-0">Control Panel</h2>
      
      <div className="flex flex-col gap-4">
      {/* Timer */}
      <div className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-lg p-4">
        <div className="text-center mb-3">
          <div className="text-sm text-gray-300 mb-2">Match Timer</div>
          <div className="text-4xl font-mono text-cyan-400">{formatTime(timeRemaining)}</div>
        </div>

        {/* Timer Controls */}
        <div className="flex gap-2 mb-3">
          <Button
            size="sm"
            onClick={handleStartStop}
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

        {/* Duration Adjustment */}
        {!isRunning && (
          <div className="space-y-2">
            <div className="flex items-center justify-center gap-1 text-xs text-gray-400">
              <Clock className="h-3 w-3" />
              <span>Adjust Duration</span>
            </div>

            {/* Minutes adjustment */}
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-400 w-12">Min:</span>
              <Button
                size="sm"
                onClick={() => adjustDuration(-60)}
                disabled={matchDuration <= 60}
                className="flex-1 h-7 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 disabled:opacity-30"
              >
                <Minus className="h-3 w-3" />
              </Button>
              <Button
                size="sm"
                onClick={() => adjustDuration(60)}
                className="flex-1 h-7 bg-green-500/20 hover:bg-green-500/30 text-green-400 border border-green-500/50"
              >
                <Plus className="h-3 w-3" />
              </Button>
            </div>

            {/* Seconds adjustment */}
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-400 w-12">Sec:</span>
              <Button
                size="sm"
                onClick={() => adjustDuration(-10)}
                disabled={matchDuration <= 10}
                className="flex-1 h-7 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 disabled:opacity-30"
              >
                <Minus className="h-3 w-3" />
              </Button>
              <Button
                size="sm"
                onClick={() => adjustDuration(10)}
                className="flex-1 h-7 bg-green-500/20 hover:bg-green-500/30 text-green-400 border border-green-500/50"
              >
                <Plus className="h-3 w-3" />
              </Button>
            </div>
          </div>
        )}
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
