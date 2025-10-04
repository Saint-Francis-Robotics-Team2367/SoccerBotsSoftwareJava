import { RefreshCw, Wifi, WifiOff, Ban, Power } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { ScrollArea } from "./ui/scroll-area";

interface Robot {
  id: string;
  name: string;
  status: "connected" | "disconnected" | "connecting";
  ipAddress: string;
  signal?: number;
  disabled?: boolean;
}

interface ConnectionPanelProps {
  robots: Robot[];
  onConnect: (id: string) => void;
  onRefresh: () => void;
  onDisable: (id: string) => void;
}

export function ConnectionPanel({ robots, onConnect, onRefresh, onDisable }: ConnectionPanelProps) {
  return (
    <div className="h-full flex flex-col backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 overflow-hidden">
      <div className="flex items-center justify-between mb-4 shrink-0">
        <h2 className="text-cyan-400">Robot Connections</h2>
        <Button
          size="sm"
          variant="ghost"
          onClick={onRefresh}
          className="h-8 w-8 p-0 hover:bg-cyan-500/20 hover:text-cyan-400"
        >
          <RefreshCw className="h-4 w-4" />
        </Button>
      </div>

      <ScrollArea className="flex-1 min-h-0">
        <div className="space-y-2">
          {robots.map((robot) => (
            <div
              key={robot.id}
              className={`backdrop-blur-sm bg-white/5 border border-white/10 rounded-md p-3 hover:bg-white/10 transition-all ${
                robot.disabled ? "opacity-50" : ""
              }`}
            >
              <div className="flex items-start justify-between mb-2">
                <div className="flex items-center gap-2">
                  {robot.disabled ? (
                    <Ban className="h-4 w-4 text-gray-400" />
                  ) : robot.status === "connected" ? (
                    <Wifi className="h-4 w-4 text-green-400" />
                  ) : (
                    <WifiOff className="h-4 w-4 text-red-400" />
                  )}
                  <span className="text-sm text-white">{robot.name}</span>
                </div>
                <Badge
                  variant="outline"
                  className={
                    robot.disabled
                      ? "bg-gray-500/20 text-gray-300 border-gray-500/50"
                      : robot.status === "connected"
                      ? "bg-green-500/20 text-green-300 border-green-500/50"
                      : robot.status === "connecting"
                      ? "bg-yellow-500/20 text-yellow-300 border-yellow-500/50"
                      : "bg-red-500/20 text-red-300 border-red-500/50"
                  }
                >
                  {robot.disabled ? "disabled" : robot.status}
                </Badge>
              </div>
              <div className="text-xs text-gray-300 mb-2">{robot.ipAddress}</div>
              {robot.signal && !robot.disabled && (
                <div className="flex items-center gap-2 mb-2">
                  <div className="flex-1 h-1 bg-white/10 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-cyan-500 to-blue-500"
                      style={{ width: `${robot.signal}%` }}
                    />
                  </div>
                  <span className="text-xs text-gray-300">{robot.signal}%</span>
                </div>
              )}
              <div className="flex gap-2">
                <Button
                  size="sm"
                  onClick={() => onConnect(robot.id)}
                  disabled={robot.status === "connecting" || robot.disabled}
                  className="flex-1 h-7 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50 disabled:opacity-50"
                >
                  {robot.status === "connected" ? "Disconnect" : "Connect"}
                </Button>
                <Button
                  size="sm"
                  onClick={() => onDisable(robot.id)}
                  className={`h-7 w-7 p-0 ${
                    robot.disabled
                      ? "bg-green-500/20 hover:bg-green-500/30 text-green-400 border border-green-500/50"
                      : "bg-orange-500/20 hover:bg-orange-500/30 text-orange-400 border border-orange-500/50"
                  }`}
                >
                  {robot.disabled ? <Power className="h-4 w-4" /> : <Ban className="h-4 w-4" />}
                </Button>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>
    </div>
  );
}
