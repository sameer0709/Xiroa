import { useEffect, useState } from 'react'
import api from '../api/client'

const SEV = {
  CRITICAL: ['badge-red', '🔴'],
  WARNING: ['badge-amber', '🟡'],
  INFO: ['badge-blue', '🔵']
}

export default function Insights() {
  const [insights, setInsights] = useState([])
  const [loading, setLoading] = useState(true)
  const [aiResult, setAiResult] = useState('')
  const [aiLoading, setAiLoading] = useState(false)

  const load = () => api.get('/insights').then(({ data }) => setInsights(data)).finally(() => setLoading(false))

  useEffect(() => { load() }, [])

  const generate = async () => {
    setLoading(true)
    try {
      const { data } = await api.post('/insights/generate')
      setInsights(data)
    } catch (err) {
      alert('Failed to generate insights')
    } finally {
      setLoading(false)
    }
  }

  const runAi = async () => {
    setAiLoading(true)
    setAiResult('')
    try {
      const { data } = await api.get('/insights/ai')
      setAiResult(data)
    } catch (err) {
      setAiResult('AI service unavailable. Configure OPENAI_API_KEY to enable.')
    } finally {
      setAiLoading(false)
    }
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={generate} disabled={loading}>
          {loading ? 'Generating…' : '⚡ Generate AI Insights'}
        </button>
        <button className="btn btn-outline" onClick={runAi} disabled={aiLoading}>
          {aiLoading ? 'Consulting AI…' : '🤖 Ask AI'}
        </button>
      </div>

      {aiResult && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">AI Natural Language Result</div>
          <pre style={{ whiteSpace: 'pre-wrap', fontSize: 13, color: 'var(--text)', fontFamily: 'inherit' }}>{aiResult}</pre>
        </div>
      )}

      {loading && <div className="empty">Loading insights…</div>}

      {!loading && insights.length === 0 && (
        <div className="card empty">
          <div>No insights yet. Click "Generate AI Insights" to analyze your business data.</div>
        </div>
      )}

      {insights.map((ins) => {
        const [cls, icon] = SEV[ins.severity] || ['badge-gray', '⚪']
        return (
          <div className="card insight-card" key={ins.id}>
            <div className="insight-title">
              <span>{icon}</span>
              <span>{ins.title}</span>
              <span className={`badge ${cls}`} style={{ marginLeft: 'auto' }}>{ins.severity}</span>
              <span className="badge badge-purple">{ins.category}</span>
            </div>
            <div className="insight-desc">{ins.description}</div>
            {ins.recommendedAction && <div className="insight-action">💡 {ins.recommendedAction}</div>}
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 8 }}>
              Generated: {new Date(ins.generatedAt).toLocaleString()}
            </div>
          </div>
        )
      })}
    </div>
  )
}

