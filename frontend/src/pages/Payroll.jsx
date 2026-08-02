import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Payroll() {
  const [records, setRecords] = useState([])
  const [employees, setEmployees] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ employeeId: '', payDate: new Date().toISOString().slice(0, 10), month: '', year: '', basic: '', hra: '', allowances: '', deductions: '' })

  const load = () => {
    api.get('/employees/payroll').then(({ data }) => setRecords(data))
    api.get('/employees').then(({ data }) => setEmployees(data))
  }

  useEffect(() => { load() }, [])

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/employees/payroll', {
        employeeId: Number(form.employeeId),
        payDate: form.payDate,
        month: form.month || undefined,
        year: form.year ? Number(form.year) : 0,
        basic: form.basic ? Number(form.basic) : null,
        hra: form.hra ? Number(form.hra) : null,
        allowances: form.allowances ? Number(form.allowances) : null,
        deductions: form.deductions ? Number(form.deductions) : 0
      })
      setMsg('Payroll processed')
      setShowForm(false)
      setForm({ employeeId: '', payDate: new Date().toISOString().slice(0, 10), month: '', year: '', basic: '', hra: '', allowances: '', deductions: '' })
      load()
    } catch (err) { setMsg(err.response?.data?.message || 'Failed') }
  }

  const markPaid = async (id) => {
    try {
      await api.put(`/employees/payroll/${id}/paid`)
      load()
    } catch (err) { alert('Failed') }
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={() => setShowForm(!showForm)}>{showForm ? 'Close' : '+ Process Payroll'}</button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: msg.includes('ailed') ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Process Monthly Payroll</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group">
                <label>Employee *</label>
                <select value={form.employeeId} onChange={(e) => setForm({ ...form, employeeId: e.target.value })} required>
                  <option value="">Select employee</option>
                  {employees.map((e) => <option key={e.id} value={e.id}>{e.name}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Pay Date</label><input type="date" value={form.payDate} onChange={(e) => setForm({ ...form, payDate: e.target.value })} /></div>
              <div className="form-group"><label>Month (MM)</label><input value={form.month} onChange={(e) => setForm({ ...form, month: e.target.value })} placeholder="e.g. 01" /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Basic (blank = 50% salary)</label><input type="number" value={form.basic} onChange={(e) => setForm({ ...form, basic: e.target.value })} /></div>
              <div className="form-group"><label>HRA (blank = 40%)</label><input type="number" value={form.hra} onChange={(e) => setForm({ ...form, hra: e.target.value })} /></div>
              <div className="form-group"><label>Allowances</label><input type="number" value={form.allowances} onChange={(e) => setForm({ ...form, allowances: e.target.value })} /></div>
              <div className="form-group"><label>Deductions</label><input type="number" value={form.deductions} onChange={(e) => setForm({ ...form, deductions: e.target.value })} /></div>
            </div>
            <button className="btn" type="submit">Process Payroll</button>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Employee</th><th>Month</th><th>Year</th><th>Pay Date</th><th>Basic</th><th>HRA</th><th>Allowances</th><th>Deductions</th><th>Net Pay</th><th>Status</th><th>Action</th></tr></thead>
            <tbody>
              {records.map((r) => (
                <tr key={r.id}>
                  <td><strong>{r.employee?.name}</strong></td>
                  <td>{r.month}</td>
                  <td>{r.year}</td>
                  <td>{r.payDate}</td>
                  <td>{fmtINR(r.basic)}</td>
                  <td>{fmtINR(r.hra)}</td>
                  <td>{fmtINR(r.allowances)}</td>
                  <td>{fmtINR(r.deductions)}</td>
                  <td><strong>{fmtINR(r.netPay)}</strong></td>
                  <td>
                    {r.status === 'PAID'
                      ? <span className="badge badge-green">Paid</span>
                      : <span className="badge badge-amber">Unpaid</span>}
                  </td>
                  <td>{r.status !== 'PAID' && <button className="btn btn-sm btn-green" onClick={() => markPaid(r.id)}>Mark Paid</button>}</td>
                </tr>
              ))}
              {records.length === 0 && <tr><td colSpan={11} className="empty">No payroll records yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

