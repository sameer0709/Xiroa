import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Employees() {
  const [employees, setEmployees] = useState([])
  const [departments, setDepartments] = useState([])
  const [leaves, setLeaves] = useState([])
  const [tab, setTab] = useState('employees')
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ name: '', employeeCode: '', email: '', phone: '', designation: '', departmentId: '', joiningDate: '', monthlySalary: '', ctc: '' })

  const load = () => {
    api.get('/employees').then(({ data }) => setEmployees(data))
    api.get('/employees/departments/all').then(({ data }) => setDepartments(data))
    api.get('/employees/leaves').then(({ data }) => setLeaves(data))
  }

  useEffect(() => { load() }, [])

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/employees', {
        ...form,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
        monthlySalary: form.monthlySalary ? Number(form.monthlySalary) : 0,
        ctc: form.ctc ? Number(form.ctc) : 0,
        joiningDate: form.joiningDate || undefined
      })
      setMsg('Employee added')
      setShowForm(false)
      setForm({ name: '', employeeCode: '', email: '', phone: '', designation: '', departmentId: '', joiningDate: '', monthlySalary: '', ctc: '' })
      load()
    } catch (err) { setMsg(err.response?.data?.message || 'Failed') }
  }

  const updateLeave = async (id, status) => {
    try {
      await api.put(`/employees/leaves/${id}/status?status=${status}`)
      load()
    } catch (err) { alert('Failed to update leave') }
  }

  return (
    <div>
      <div className="btn-row">
        <button className={`btn ${tab === 'employees' ? '' : 'btn-outline'}`} onClick={() => setTab('employees')}>Employees</button>
        <button className={`btn ${tab === 'leaves' ? '' : 'btn-outline'}`} onClick={() => setTab('leaves')}>Leave Requests</button>
        {tab === 'employees' && <button className="btn btn-green" style={{ marginLeft: 'auto' }} onClick={() => setShowForm(!showForm)}>{showForm ? 'Close' : '+ Add Employee'}</button>}
      </div>

      {msg && <div style={{ marginBottom: 16, color: 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && tab === 'employees' && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Add Employee</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group"><label>Name *</label><input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
              <div className="form-group"><label>Employee Code</label><input value={form.employeeCode} onChange={(e) => setForm({ ...form, employeeCode: e.target.value })} /></div>
              <div className="form-group"><label>Designation</label><input value={form.designation} onChange={(e) => setForm({ ...form, designation: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
              <div className="form-group"><label>Phone</label><input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
              <div className="form-group">
                <label>Department</label>
                <select value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
                  <option value="">None</option>
                  {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Joining Date</label><input type="date" value={form.joiningDate} onChange={(e) => setForm({ ...form, joiningDate: e.target.value })} /></div>
              <div className="form-group"><label>Monthly Salary</label><input type="number" value={form.monthlySalary} onChange={(e) => setForm({ ...form, monthlySalary: e.target.value })} /></div>
              <div className="form-group"><label>CTC (annual)</label><input type="number" value={form.ctc} onChange={(e) => setForm({ ...form, ctc: e.target.value })} /></div>
            </div>
            <button className="btn" type="submit">Save Employee</button>
          </form>
        </div>
      )}

      <div className="card">
        {tab === 'employees' ? (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Code</th><th>Name</th><th>Designation</th><th>Department</th><th>Email</th><th>Monthly Salary</th><th>Status</th></tr></thead>
              <tbody>
                {employees.map((e) => (
                  <tr key={e.id}>
                    <td><code>{e.employeeCode || '—'}</code></td>
                    <td><strong>{e.name}</strong></td>
                    <td>{e.designation || '—'}</td>
                    <td>{e.department?.name || '—'}</td>
                    <td>{e.email || '—'}</td>
                    <td>{fmtINR(e.monthlySalary)}</td>
                    <td>{e.active ? <span className="badge badge-green">Active</span> : <span className="badge badge-gray">Inactive</span>}</td>
                  </tr>
                ))}
                {employees.length === 0 && <tr><td colSpan={7} className="empty">No employees yet</td></tr>}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Employee</th><th>Start</th><th>End</th><th>Days</th><th>Type</th><th>Reason</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {leaves.map((l) => (
                  <tr key={l.id}>
                    <td><strong>{l.employee?.name}</strong></td>
                    <td>{l.startDate}</td>
                    <td>{l.endDate}</td>
                    <td>{l.days}</td>
                    <td>{l.type || '—'}</td>
                    <td>{l.reason || '—'}</td>
                    <td>
                      {l.status === 'APPROVED' ? <span className="badge badge-green">Approved</span>
                        : l.status === 'REJECTED' ? <span className="badge badge-red">Rejected</span>
                        : <span className="badge badge-amber">Pending</span>}
                    </td>
                    <td>
                      {l.status === 'PENDING' && (
                        <>
                          <button className="btn btn-sm btn-green" style={{ marginRight: 6 }} onClick={() => updateLeave(l.id, 'APPROVED')}>Approve</button>
                          <button className="btn btn-sm btn-danger" onClick={() => updateLeave(l.id, 'REJECTED')}>Reject</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
                {leaves.length === 0 && <tr><td colSpan={8} className="empty">No leave requests</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

