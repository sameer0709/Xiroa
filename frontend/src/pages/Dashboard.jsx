import { useEffect, useState } from 'react'
import api from '../api/client'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, Legend
} from 'recharts'

const COLORS = ['#6366f1', '#a855f7', '#3b82f6', '#22c55e', '#f59e0b', '#ef4444']

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/dashboard')
      .then(({ data }) => setData(data))
      .catch(() => setError('Failed to load dashboard. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="empty">Loading dashboard…</div>
  if (error) return <div className="empty" style={{ color: 'var(--red)' }}>{error}</div>

  const kpis = [
    { label: 'Total Revenue', value: fmtINR(data.totalRevenue), sub: 'All time', cls: 'green' },
    { label: 'This Month', value: fmtINR(data.monthlyRevenue), sub: `${data.totalInvoices} invoices`, cls: 'blue' },
    { label: 'Outstanding', value: fmtINR(data.outstandingAmount), sub: `${data.outstandingInvoices} unpaid`, cls: 'red' },
    { label: 'Inventory Value', value: fmtINR(data.inventoryValue), sub: `${data.lowStockProducts} low stock`, cls: 'amber' },
    { label: 'Products', value: data.totalProducts, sub: `${data.lowStockProducts} need reorder`, cls: 'purple' },
    { label: 'Customers', value: data.totalCustomers, sub: 'Active accounts', cls: 'green' },
    { label: 'Employees', value: data.totalEmployees, sub: 'On payroll', cls: 'blue' },
    { label: 'Payroll (Month)', value: fmtINR(data.payrollThisMonth), sub: 'This month', cls: 'purple' }
  ]

  const topProducts = (data.topProducts || []).map((p) => ({ name: p.name, revenue: Number(p.revenue) }))
  const topCustomers = (data.topCustomers || []).map((c) => ({ name: c.name, revenue: Number(c.revenue) }))

  return (
    <div>
      <div className="grid grid-4" style={{ marginBottom: 20 }}>
        {kpis.map((k) => (
          <div key={k.label} className={`card kpi ${k.cls}`}>
            <div className="label">{k.label}</div>
            <div className="value">{k.value}</div>
            <div className="sub">{k.sub}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-2" style={{ marginBottom: 20 }}>
        <div className="card">
          <div className="section-title">Sales Trend (6 months)</div>
          <ResponsiveContainer width="100%" height={260}>
            <AreaChart data={data.salesTrend}>
              <defs>
                <linearGradient id="rev" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="month" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip formatter={(v) => fmtINR(v)} contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
              <Area type="monotone" dataKey="revenue" stroke="#6366f1" fill="url(#rev)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="section-title">Top Products by Revenue</div>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={topProducts}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="name" stroke="#94a3b8" tick={{ fontSize: 11 }} />
              <YAxis stroke="#94a3b8" />
              <Tooltip formatter={(v) => fmtINR(v)} contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
              <Bar dataKey="revenue" radius={[6, 6, 0, 0]}>
                {topProducts.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="section-title">Top Customers</div>
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie data={topCustomers} dataKey="revenue" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                {topCustomers.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip formatter={(v) => fmtINR(v)} contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
              <Legend wrapperStyle={{ fontSize: 12 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="section-title">Quick Summary</div>
          <div style={{ display: 'grid', gap: 12 }}>
            {[
              ['Total invoices generated', data.totalInvoices],
              ['Outstanding (unpaid) invoices', data.outstandingInvoices],
              ['Total products in catalog', data.totalProducts],
              ['Products below reorder level', data.lowStockProducts]
            ].map(([label, val]) => (
              <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid rgba(51,65,85,0.5)' }}>
                <span style={{ color: 'var(--text-muted)' }}>{label}</span>
                <span style={{ fontWeight: 700 }}>{val}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

