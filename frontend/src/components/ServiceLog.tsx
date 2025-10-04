import { ScrollArea } from "./ui/scroll-area";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Trash2 } from "lucide-react";

interface LogEntry {
  id: string;
  timestamp: string;
  level: "info" | "warning" | "error" | "success";
  message: string;
}

interface ServiceLogProps {
  logs: LogEntry[];
  onClear: () => void;
}

export function ServiceLog({ logs, onClear }: ServiceLogProps) {
  const getLevelColor = (level: string) => {
    switch (level) {
      case "error":
        return "bg-red-500/20 text-red-400 border-red-500/50";
      case "warning":
        return "bg-yellow-500/20 text-yellow-400 border-yellow-500/50";
      case "success":
        return "bg-green-500/20 text-green-400 border-green-500/50";
      default:
        return "bg-blue-500/20 text-blue-400 border-blue-500/50";
    }
  };

  return (
    <div className="h-full backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 flex flex-col overflow-hidden">
      <div className="flex items-center justify-between mb-4 shrink-0">
        <h2 className="text-cyan-400">Service Log</h2>
        <Button
          size="sm"
          variant="ghost"
          onClick={onClear}
          className="h-8 w-8 p-0 hover:bg-red-500/20 hover:text-red-400"
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
      <ScrollArea className="flex-1 min-h-0">
        <div className="space-y-2 pr-4">
          {logs.map((log) => (
            <div
              key={log.id}
              className="backdrop-blur-sm bg-white/5 border border-white/10 rounded-md p-3 hover:bg-white/10 transition-all"
            >
              <div className="flex items-start gap-2 mb-1">
                <Badge variant="outline" className={getLevelColor(log.level)}>
                  {log.level}
                </Badge>
                <span className="text-xs text-gray-400">{log.timestamp}</span>
              </div>
              <p className="text-sm text-gray-200">{log.message}</p>
            </div>
          ))}
        </div>
      </ScrollArea>
    </div>
  );
}
