import { useEffect, useState } from 'react'
import api from '../api/client'
import {
  ComposedChart, Line, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })

export default function Forecast() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [months, setMonths] = useState(6)

  useEffect(() => {
    setLoading(true)
    api.get(`/forecast?months=${months}`)
      .then(({ data }) => setData(data))
      .finally(() => setLoading(false))
  }, [months])

  const chartData = (data?.points || []).map((p) => ({
    period: p.period,
    Actual: p.actual !== null && p.actual !== undefined ? Number(p.actual) : null,
    Forecast: p.forecast !== null && p.forecast !== undefined ? Number(p.forecast) : null
  }))

  return (
    <div>
      <div className="btn-row" style={{ alignItems: 'center' }}>
        <div className="section-title" style={{ margin: 0 }}>Sales Forecast</div>
        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 10 }}>
          <label style={{ fontSize: 13, color: 'var(--text-muted)' }}>History months:</label>
          <select value={months} onChange={(e) => setMonths(Number(e.target.value))} style={{ width: 90 }}>
            {[3, 4, 6, 8, 12].map((m) => <option key={m} value={m}>{m}</option>)}
          </select>
        </div>
      </div>

      {loading && <div className="empty">Calculating forecast…</div>}

      {data && (
        <>
          <div className="grid grid-3" style={{ marginBottom: 20 }}>
            <div className="card kpi green">
              <div className="label">Next Period Forecast</div>
              <div className="value">{fmtINR(data.nextPeriodForecast)}</div>
              <div className="sub">Projected revenue</div>
            </div>
            <div className="card kpi blue">
              <div className="label">Method</div>
              <div className="value" style={{ fontSize: 18, paddingTop: 6 }}>{data.method}</div>
              <div className="sub">Statistical model</div>
            </div>
            <div className="card kpi purple">
              <div className="label">Confidence</div>
              <div className="value">{data.confidence}%</div>
              <div className="sub">Model confidence</div>
            </div>
          </div>

          <div className="card">
            <div className="section-title">Revenue: Actual vs Forecast</div>
            <ResponsiveContainer width="100%" height={340}>
              <ComposedChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="period" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip formatter={(v) => (v == null ? '—' : fmtINR(v))} contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
                <Legend />
                <Bar dataKey="Actual" fill="#6366f1" radius={[6, 6, 0, 0]} />
                <Line type="monotone" dataKey="Forecast" stroke="#22c55e" strokeWidth={3} dot={{ r: 5 }} connectNulls />
              </ComposedChart>
            </ResponsiveContainer>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 10 }}>
              💡 Forecast uses moving average + linear trend of actual monthly revenue. In production this can be upgraded to an ML model (e.g., Prophet or scikit-learn) or an AI API.
            </div>
          </div>
        </>
      )}
    </div>
  )
}

