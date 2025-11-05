import { ScrollArea } from "./ui/scroll-area";
import { useEffect, useRef } from "react";

interface TerminalMonitorProps {
  lines: string[];
}

export function TerminalMonitor({ lines }: TerminalMonitorProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Scroll to bottom when lines change
    if (bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [lines]);

  return (
    <div className="h-full backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 flex flex-col overflow-hidden">
      <h2 className="text-cyan-400 mb-4 shrink-0">Terminal Monitor</h2>
      <div className="flex-1 backdrop-blur-sm bg-black/40 border border-white/10 rounded-md p-4 font-mono text-sm overflow-hidden min-h-0">
        <ScrollArea className="h-full terminal-scroll">
          <div className="space-y-1">
            {lines.map((line, index) => (
              <div key={index} className="flex gap-3">
                <span className="text-cyan-500/50 select-none">{index + 1}</span>
                <span className="text-green-400">{line}</span>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}
