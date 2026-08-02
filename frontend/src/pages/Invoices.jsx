import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Invoices() {
  const [invoices, setInvoices] = useState([])
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ customerId: '', amountPaid: '', paymentMode: 'UPI', notes: '', items: [{ productId: '', quantity: 1, unitPrice: '' }] })

  const load = () => api.get('/invoices').then(({ data }) => setInvoices(data))
  const loadOptions = () => {
    api.get('/customers/id-names').then(({ data }) => setCustomers(data))
    api.get('/products/active').then(({ data }) => setProducts(data))
  }

  useEffect(() => {
    load()
    loadOptions()
  }, [])

  const addItem = () => setForm({ ...form, items: [...form.items, { productId: '', quantity: 1, unitPrice: '' }] })
  const updateItem = (i, key, val) => {
    const items = [...form.items]
    items[i][key] = val
    setForm({ ...form, items })
  }

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/invoices', {
        customerId: Number(form.customerId),
        amountPaid: form.amountPaid ? Number(form.amountPaid) : 0,
        paymentMode: form.paymentMode,
        notes: form.notes,
        items: form.items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitPrice: it.unitPrice ? Number(it.unitPrice) : null
        }))
      })
      setMsg('Invoice created successfully')
      setShowForm(false)
      setForm({ customerId: '', amountPaid: '', paymentMode: 'UPI', notes: '', items: [{ productId: '', quantity: 1, unitPrice: '' }] })
      load()
    } catch (err) {
      setMsg(err.response?.data?.message || 'Failed to create invoice')
    }
  }

  const pay = async (id) => {
    const amount = prompt('Enter payment amount:')
    if (!amount) return
    try {
      await api.post(`/invoices/${id}/payments`, { amount: Number(amount), paymentMode: 'UPI' })
      load()
    } catch (err) {
      alert(err.response?.data?.message || 'Payment failed')
    }
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={() => setShowForm(!showForm)}>{showForm ? 'Close Form' : '+ Create Invoice'}</button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: msg.includes('failed') || msg.includes('Failed') ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Create GST Invoice</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group">
                <label>Customer *</label>
                <select value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} required>
                  <option value="">Select customer</option>
                  {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Amount Paid</label><input type="number" value={form.amountPaid} onChange={(e) => setForm({ ...form, amountPaid: e.target.value })} placeholder="0 = credit" /></div>
              <div className="form-group">
                <label>Payment Mode</label>
                <select value={form.paymentMode} onChange={(e) => setForm({ ...form, paymentMode: e.target.value })}>
                  <option>UPI</option><option>Cash</option><option>Bank Transfer</option><option>Card</option>
                </select>
              </div>
            </div>

            <div className="section-title" style={{ marginTop: 10 }}>Items</div>
            {form.items.map((item, i) => (
              <div className="form-row" key={i}>
                <div className="form-group">
                  <label>Product</label>
                  <select value={item.productId} onChange={(e) => updateItem(i, 'productId', e.target.value)} required>
                    <option value="">Select product</option>
                    {products.map((p) => <option key={p.id} value={p.id}>{p.name} — {fmtINR(p.sellingPrice)}</option>)}
                  </select>
                </div>
                <div className="form-group"><label>Qty</label><input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(i, 'quantity', e.target.value)} required /></div>
                <div className="form-group"><label>Unit Price (blank = default)</label><input type="number" value={item.unitPrice} onChange={(e) => updateItem(i, 'unitPrice', e.target.value)} /></div>
                {form.items.length > 1 && <div className="form-group"><label>&nbsp;</label><button type="button" className="btn btn-outline btn-sm" onClick={() => setForm({ ...form, items: form.items.filter((_, idx) => idx !== i) })}>Remove</button></div>}
              </div>
            ))}
            <button type="button" className="btn btn-outline btn-sm" onClick={addItem} style={{ marginBottom: 14 }}>+ Add Item</button>

            <div className="form-group" style={{ marginBottom: 14 }}><label>Notes</label><input value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} /></div>
            <button className="btn" type="submit">Create Invoice</button>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Invoice #</th><th>Customer</th><th>Date</th><th>Total</th><th>Paid</th><th>Balance</th><th>Status</th><th>Action</th></tr>
            </thead>
            <tbody>
              {invoices.map((inv) => (
                <tr key={inv.id}>
                  <td><code>{inv.invoiceNumber}</code></td>
                  <td><strong>{inv.customer?.name}</strong></td>
                  <td>{new Date(inv.invoiceDate).toLocaleDateString()}</td>
                  <td>{fmtINR(inv.total)}</td>
                  <td>{fmtINR(inv.amountPaid)}</td>
                  <td>{fmtINR(inv.balanceDue)}</td>
                  <td>
                    {inv.paymentStatus === 'PAID'
                      ? <span className="badge badge-green">Paid</span>
                      : inv.paymentStatus === 'PARTIALLY_PAID'
                        ? <span className="badge badge-amber">Partial</span>
                        : <span className="badge badge-red">Unpaid</span>}
                  </td>
                  <td>
                    {inv.paymentStatus !== 'PAID' && (
                      <button className="btn btn-sm btn-green" onClick={() => pay(inv.id)}>Receive</button>
                    )}
                  </td>
                </tr>
              ))}
              {invoices.length === 0 && <tr><td colSpan={8} className="empty">No invoices yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

