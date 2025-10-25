import { Gamepad2, Link, Unlink, Power, Ban, Circle, RefreshCw } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { ScrollArea } from "./ui/scroll-area";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import { useState } from "react";

interface Controller {
  id: string;
  name: string;
  connected: boolean;
  pairedRobotId?: string;
  enabled?: boolean;
  type?: string;
}

interface Robot {
  id: string;
  name: string;
}

interface ControllersPanelProps {
  controllers: Controller[];
  robots: Robot[];
  onPair: (controllerId: string, robotId: string) => void;
  onUnpair: (controllerId: string) => void;
  onEnable: (controllerId: string) => void;
  onDisable: (controllerId: string) => void;
  onRefresh: () => void;
}

export function ControllersPanel({
  controllers,
  robots,
  onPair,
  onUnpair,
  onEnable,
  onDisable,
  onRefresh,
}: ControllersPanelProps) {
  const [pairingController, setPairingController] = useState<string | null>(null);

  const handlePair = (controllerId: string, robotId: string) => {
    onPair(controllerId, robotId);
    setPairingController(null);
  };

  const getControllerIcon = (type?: string) => {
    switch (type) {
      case "ps5":
        return "🎮"; // PS5 DualSense
      case "ps4":
        return "🕹️"; // PS4 DualShock
      case "xbox":
        return "🎯"; // Xbox
      default:
        return "🎮";
    }
  };

  return (
    <div className="h-full flex flex-col backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 overflow-hidden">
      <div className="flex items-center justify-between mb-4 shrink-0">
        <div>
          <h2 className="text-cyan-400">Controllers</h2>
          <p className="text-xs text-gray-400">{controllers.length} detected</p>
        </div>
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
          {controllers.length === 0 ? (
            <div className="text-center text-gray-400 text-sm py-8">
              No controllers detected
              <br />
              <span className="text-xs">Connect a USB controller</span>
            </div>
          ) : (
            controllers.map((controller) => (
              <div
                key={controller.id}
                className={`backdrop-blur-sm bg-white/5 border rounded-md p-3 hover:bg-white/10 transition-all ${
                  controller.enabled === false
                    ? "opacity-50 border-gray-500/30"
                    : "border-white/10"
                }`}
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{getControllerIcon(controller.type)}</span>
                    {controller.connected ? (
                      <Gamepad2 className="h-4 w-4 text-green-400" />
                    ) : (
                      <Gamepad2 className="h-4 w-4 text-gray-400" />
                    )}
                    <span className="text-sm text-white truncate max-w-[120px]">
                      {controller.name}
                    </span>
                    {controller.pairedRobotId && controller.enabled !== false && (
                      <Circle className="h-2 w-2 text-cyan-400 fill-cyan-400 animate-pulse" />
                    )}
                  </div>
                  <Badge
                    variant="outline"
                    className={
                      controller.enabled === false
                        ? "bg-gray-500/20 text-gray-300 border-gray-500/50"
                        : controller.pairedRobotId
                        ? "bg-cyan-500/20 text-cyan-300 border-cyan-500/50"
                        : controller.connected
                        ? "bg-green-500/20 text-green-300 border-green-500/50"
                        : "bg-red-500/20 text-red-300 border-red-500/50"
                    }
                  >
                    {controller.enabled === false
                      ? "disabled"
                      : controller.pairedRobotId
                      ? "paired"
                      : controller.connected
                      ? "ready"
                      : "offline"}
                  </Badge>
                </div>

                {controller.pairedRobotId && (
                  <div className="text-xs text-cyan-400 mb-2">
                    → {robots.find((r) => r.id === controller.pairedRobotId)?.name || "Unknown"}
                  </div>
                )}

                {pairingController === controller.id ? (
                  <div className="space-y-2">
                    <Select onValueChange={(value) => handlePair(controller.id, value)}>
                      <SelectTrigger className="h-7 text-xs">
                        <SelectValue placeholder="Select robot" />
                      </SelectTrigger>
                      <SelectContent>
                        {robots.map((robot) => (
                          <SelectItem key={robot.id} value={robot.id}>
                            {robot.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => setPairingController(null)}
                      className="w-full h-6 text-xs"
                    >
                      Cancel
                    </Button>
                  </div>
                ) : (
                  <div className="flex gap-2">
                    {controller.pairedRobotId ? (
                      <Button
                        size="sm"
                        onClick={() => onUnpair(controller.id)}
                        disabled={!controller.connected || controller.enabled === false}
                        className="flex-1 h-7 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 disabled:opacity-50"
                      >
                        <Unlink className="h-3 w-3 mr-1" />
                        Unpair
                      </Button>
                    ) : (
                      <Button
                        size="sm"
                        onClick={() => setPairingController(controller.id)}
                        disabled={!controller.connected || controller.enabled === false}
                        className="flex-1 h-7 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50 disabled:opacity-50"
                      >
                        <Link className="h-3 w-3 mr-1" />
                        Pair
                      </Button>
                    )}
                    <Button
                      size="sm"
                      onClick={() =>
                        controller.enabled === false
                          ? onEnable(controller.id)
                          : onDisable(controller.id)
                      }
                      className={`h-7 w-7 p-0 ${
                        controller.enabled === false
                          ? "bg-green-500/20 hover:bg-green-500/30 text-green-400 border border-green-500/50"
                          : "bg-orange-500/20 hover:bg-orange-500/30 text-orange-400 border border-orange-500/50"
                      }`}
                    >
                      {controller.enabled === false ? (
                        <Power className="h-4 w-4" />
                      ) : (
                        <Ban className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </ScrollArea>
    </div>
  );
}
