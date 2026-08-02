import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function PurchaseOrders() {
  const [orders, setOrders] = useState([])
  const [vendors, setVendors] = useState([])
  const [products, setProducts] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ vendorId: '', notes: '', items: [{ productId: '', quantity: 1, unitCost: '' }] })

  const load = () => api.get('/purchase-orders').then(({ data }) => setOrders(data))
  const loadOptions = () => {
    api.get('/customers/vendors').then(({ data }) => setVendors(data))
    api.get('/products/active').then(({ data }) => setProducts(data))
  }

  useEffect(() => {
    load()
    loadOptions()
  }, [])

  const addItem = () => setForm({ ...form, items: [...form.items, { productId: '', quantity: 1, unitCost: '' }] })
  const updateItem = (i, key, val) => {
    const items = [...form.items]
    items[i][key] = val
    setForm({ ...form, items })
  }

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/purchase-orders', {
        vendorId: Number(form.vendorId),
        notes: form.notes,
        items: form.items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitCost: it.unitCost ? Number(it.unitCost) : null
        }))
      })
      setMsg('Purchase order created')
      setShowForm(false)
      setForm({ vendorId: '', notes: '', items: [{ productId: '', quantity: 1, unitCost: '' }] })
      load()
    } catch (err) { setMsg(err.response?.data?.message || 'Failed') }
  }

  const receive = async (id) => {
    if (!confirm('Mark this PO as received? Stock will be added to inventory.')) return
    try {
      await api.post(`/purchase-orders/${id}/receive`)
      load()
    } catch (err) { alert(err.response?.data?.message || 'Failed to receive PO') }
  }

  const statusBadge = (s) => {
    const map = {
      DRAFT: ['badge-gray', 'Draft'],
      PENDING: ['badge-amber', 'Pending'],
      CONFIRMED: ['badge-blue', 'Confirmed'],
      SHIPPED: ['badge-purple', 'Shipped'],
      DELIVERED: ['badge-green', 'Delivered'],
      CANCELLED: ['badge-red', 'Cancelled']
    }
    const [cls, label] = map[s] || ['badge-gray', s]
    return <span className={`badge ${cls}`}>{label}</span>
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={() => setShowForm(!showForm)}>{showForm ? 'Close' : '+ Create Purchase Order'}</button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: msg.includes('ailed') ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Create Purchase Order</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group">
                <label>Vendor *</label>
                <select value={form.vendorId} onChange={(e) => setForm({ ...form, vendorId: e.target.value })} required>
                  <option value="">Select vendor</option>
                  {vendors.map((v) => <option key={v.id} value={v.id}>{v.name}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Notes</label><input value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} /></div>
            </div>

            <div className="section-title" style={{ marginTop: 10 }}>Items</div>
            {form.items.map((item, i) => (
              <div className="form-row" key={i}>
                <div className="form-group">
                  <label>Product</label>
                  <select value={item.productId} onChange={(e) => updateItem(i, 'productId', e.target.value)} required>
                    <option value="">Select product</option>
                    {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                </div>
                <div className="form-group"><label>Qty</label><input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(i, 'quantity', e.target.value)} required /></div>
                <div className="form-group"><label>Unit Cost</label><input type="number" value={item.unitCost} onChange={(e) => updateItem(i, 'unitCost', e.target.value)} /></div>
                {form.items.length > 1 && <div className="form-group"><label>&nbsp;</label><button type="button" className="btn btn-outline btn-sm" onClick={() => setForm({ ...form, items: form.items.filter((_, idx) => idx !== i) })}>Remove</button></div>}
              </div>
            ))}
            <button type="button" className="btn btn-outline btn-sm" onClick={addItem} style={{ marginBottom: 14 }}>+ Add Item</button>
            <div><button className="btn" type="submit">Create PO</button></div>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>PO #</th><th>Vendor</th><th>Date</th><th>Items</th><th>Total</th><th>Status</th><th>Action</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td><code>{o.poNumber}</code></td>
                  <td><strong>{o.vendor?.name}</strong></td>
                  <td>{new Date(o.orderDate).toLocaleDateString()}</td>
                  <td>{o.items?.length || 0}</td>
                  <td>{fmtINR(o.totalAmount)}</td>
                  <td>{statusBadge(o.status)}</td>
                  <td>
                    {o.status !== 'DELIVERED' && o.status !== 'CANCELLED' && (
                      <button className="btn btn-sm btn-green" onClick={() => receive(o.id)}>Receive</button>
                    )}
                  </td>
                </tr>
              ))}
              {orders.length === 0 && <tr><td colSpan={7} className="empty">No purchase orders yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

