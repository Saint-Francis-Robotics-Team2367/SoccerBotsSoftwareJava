import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

interface NetworkAnalysisProps {
  data: Array<{ time: string; latency: number; bandwidth: number }>;
}

export function NetworkAnalysis({ data }: NetworkAnalysisProps) {
  return (
    <div className="h-full backdrop-blur-md bg-black/30 border border-white/10 rounded-lg p-4 flex flex-col overflow-hidden">
      <h2 className="text-cyan-400 mb-4 shrink-0">Network Analysis</h2>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
          <XAxis 
            dataKey="time" 
            stroke="rgba(255,255,255,0.5)"
            style={{ fontSize: '12px' }}
          />
          <YAxis 
            stroke="rgba(255,255,255,0.5)"
            style={{ fontSize: '12px' }}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: 'rgba(0,0,0,0.8)',
              border: '1px solid rgba(255,255,255,0.1)',
              borderRadius: '8px',
              backdropFilter: 'blur(10px)',
            }}
          />
          <Line
            type="monotone"
            dataKey="latency"
            stroke="#06b6d4"
            strokeWidth={2}
            dot={false}
            name="Latency (ms)"
          />
          <Line
            type="monotone"
            dataKey="bandwidth"
            stroke="#8b5cf6"
            strokeWidth={2}
            dot={false}
            name="Bandwidth (Mbps)"
          />
        </LineChart>
      </ResponsiveContainer>
      </div>
    </div>
  );
}
