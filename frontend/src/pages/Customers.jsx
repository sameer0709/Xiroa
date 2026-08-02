import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Customers() {
  const [customers, setCustomers] = useState([])
  const [vendors, setVendors] = useState([])
  const [tab, setTab] = useState('customers')
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ name: '', gstin: '', email: '', phone: '', city: '', state: '', address: '', company: '' })
  const [vendorForm, setVendorForm] = useState({ name: '', gstin: '', contactPerson: '', phone: '', email: '', address: '', city: '', state: '' })

  const load = () => {
    api.get('/customers').then(({ data }) => setCustomers(data))
    api.get('/customers/vendors/all').then(({ data }) => setVendors(data))
  }

  useEffect(() => { load() }, [])

  const submitCustomer = async (e) => {
    e.preventDefault()
    try {
      await api.post('/customers', form)
      setMsg('Customer added')
      setShowForm(false)
      setForm({ name: '', gstin: '', email: '', phone: '', city: '', state: '', address: '', company: '' })
      load()
    } catch (err) { setMsg(err.response?.data?.message || 'Failed') }
  }

  const submitVendor = async (e) => {
    e.preventDefault()
    try {
      await api.post('/customers/vendors', vendorForm)
      setMsg('Vendor added')
      setShowForm(false)
      setVendorForm({ name: '', gstin: '', contactPerson: '', phone: '', email: '', address: '', city: '', state: '' })
      load()
    } catch (err) { setMsg(err.response?.data?.message || 'Failed') }
  }

  return (
    <div>
      <div className="btn-row">
        <button className={`btn ${tab === 'customers' ? '' : 'btn-outline'}`} onClick={() => setTab('customers')}>Customers</button>
        <button className={`btn ${tab === 'vendors' ? '' : 'btn-outline'}`} onClick={() => setTab('vendors')}>Vendors</button>
        <button className="btn btn-green" style={{ marginLeft: 'auto' }} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Close' : `+ Add ${tab === 'customers' ? 'Customer' : 'Vendor'}`}
        </button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && tab === 'customers' && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Add Customer (CRM)</div>
          <form onSubmit={submitCustomer}>
            <div className="form-row">
              <div className="form-group"><label>Name *</label><input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
              <div className="form-group"><label>GSTIN</label><input value={form.gstin} onChange={(e) => setForm({ ...form, gstin: e.target.value })} /></div>
              <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Phone</label><input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
              <div className="form-group"><label>City</label><input value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} /></div>
              <div className="form-group"><label>State</label><input value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} /></div>
            </div>
            <div className="form-group" style={{ marginBottom: 14 }}><label>Address</label><input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} /></div>
            <button className="btn" type="submit">Save Customer</button>
          </form>
        </div>
      )}

      {showForm && tab === 'vendors' && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Add Vendor</div>
          <form onSubmit={submitVendor}>
            <div className="form-row">
              <div className="form-group"><label>Name *</label><input value={vendorForm.name} onChange={(e) => setVendorForm({ ...vendorForm, name: e.target.value })} required /></div>
              <div className="form-group"><label>GSTIN</label><input value={vendorForm.gstin} onChange={(e) => setVendorForm({ ...vendorForm, gstin: e.target.value })} /></div>
              <div className="form-group"><label>Contact Person</label><input value={vendorForm.contactPerson} onChange={(e) => setVendorForm({ ...vendorForm, contactPerson: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Phone</label><input value={vendorForm.phone} onChange={(e) => setVendorForm({ ...vendorForm, phone: e.target.value })} /></div>
              <div className="form-group"><label>Email</label><input type="email" value={vendorForm.email} onChange={(e) => setVendorForm({ ...vendorForm, email: e.target.value })} /></div>
              <div className="form-group"><label>City</label><input value={vendorForm.city} onChange={(e) => setVendorForm({ ...vendorForm, city: e.target.value })} /></div>
            </div>
            <button className="btn" type="submit">Save Vendor</button>
          </form>
        </div>
      )}

      <div className="card">
        {tab === 'customers' ? (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Name</th><th>GSTIN</th><th>Contact</th><th>City</th><th>Orders</th><th>Total Purchases</th><th>Status</th></tr></thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id}>
                    <td><strong>{c.name}</strong></td>
                    <td><code>{c.gstin || '—'}</code></td>
                    <td>{c.email}<br /><span style={{ color: 'var(--text-muted)', fontSize: 12 }}>{c.phone}</span></td>
                    <td>{c.city || '—'}</td>
                    <td>{c.totalOrders}</td>
                    <td>{fmtINR(c.totalPurchases)}</td>
                    <td>{c.active ? <span className="badge badge-green">Active</span> : <span className="badge badge-gray">Inactive</span>}</td>
                  </tr>
                ))}
                {customers.length === 0 && <tr><td colSpan={7} className="empty">No customers yet</td></tr>}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Name</th><th>GSTIN</th><th>Contact</th><th>Phone</th><th>City</th><th>State</th></tr></thead>
              <tbody>
                {vendors.map((v) => (
                  <tr key={v.id}>
                    <td><strong>{v.name}</strong></td>
                    <td><code>{v.gstin || '—'}</code></td>
                    <td>{v.contactPerson || '—'}</td>
                    <td>{v.phone || '—'}</td>
                    <td>{v.city || '—'}</td>
                    <td>{v.state || '—'}</td>
                  </tr>
                ))}
                {vendors.length === 0 && <tr><td colSpan={6} className="empty">No vendors yet</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

