import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Inventory() {
  const [items, setItems] = useState([])
  const [products, setProducts] = useState([])
  const [msg, setMsg] = useState('')
  const [adjust, setAdjust] = useState({ productId: '', quantity: '', reason: '' })
  const [showAdjust, setShowAdjust] = useState(false)

  const load = () => api.get('/inventory').then(({ data }) => setItems(data))
  const loadProducts = () => api.get('/products/active').then(({ data }) => setProducts(data))

  useEffect(() => {
    load()
    loadProducts()
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/inventory/adjust', {
        productId: Number(adjust.productId),
        quantity: Number(adjust.quantity),
        reason: adjust.reason
      })
      setMsg('Stock adjusted')
      setShowAdjust(false)
      setAdjust({ productId: '', quantity: '', reason: '' })
      load()
    } catch (err) {
      setMsg(err.response?.data?.message || 'Adjustment failed')
    }
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={() => setShowAdjust(!showAdjust)}>{showAdjust ? 'Close' : '+ Adjust Stock'}</button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: msg.includes('failed') || msg.includes('Failed') ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showAdjust && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Stock Adjustment</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group">
                <label>Product</label>
                <select value={adjust.productId} onChange={(e) => setAdjust({ ...adjust, productId: e.target.value })} required>
                  <option value="">Select product</option>
                  {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Quantity (+/-)</label>
                <input type="number" value={adjust.quantity} onChange={(e) => setAdjust({ ...adjust, quantity: e.target.value })} required placeholder="e.g. 10 or -5" />
              </div>
              <div className="form-group">
                <label>Reason</label>
                <input value={adjust.reason} onChange={(e) => setAdjust({ ...adjust, reason: e.target.value })} placeholder="e.g. Damaged goods, stock count" />
              </div>
            </div>
            <button className="btn" type="submit">Apply Adjustment</button>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>SKU</th><th>Product</th><th>Category</th><th>On Hand</th><th>Available</th><th>Reorder Level</th><th>Stock Value</th><th>Status</th></tr>
            </thead>
            <tbody>
              {items.map((it) => (
                <tr key={it.productId}>
                  <td><code>{it.sku}</code></td>
                  <td><strong>{it.productName}</strong></td>
                  <td>{it.category}</td>
                  <td>{it.quantityOnHand}</td>
                  <td>{it.availableQuantity}</td>
                  <td>{it.reorderLevel}</td>
                  <td>{fmtINR(it.stockValue)}</td>
                  <td>
                    {it.lowStock
                      ? <span className="badge badge-red">Low Stock</span>
                      : <span className="badge badge-green">Healthy</span>}
                  </td>
                </tr>
              ))}
              {items.length === 0 && <tr><td colSpan={8} className="empty">No inventory records</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

