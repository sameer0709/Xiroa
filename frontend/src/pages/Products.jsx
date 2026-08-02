import { useEffect, useState } from 'react'
import api from '../api/client'

const fmtINR = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

export default function Products() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({
    sku: '', name: '', description: '', categoryId: '', costPrice: '', sellingPrice: '',
    gstRate: '18', hsnCode: '', reorderLevel: '5', openingStock: '0'
  })

  const load = () => api.get('/products').then(({ data }) => setProducts(data))
  const loadCategories = () => api.get('/products/categories').then(({ data }) => setCategories(data))

  useEffect(() => {
    load()
    loadCategories()
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/products', {
        ...form,
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        costPrice: Number(form.costPrice),
        sellingPrice: Number(form.sellingPrice),
        gstRate: Number(form.gstRate),
        reorderLevel: Number(form.reorderLevel),
        openingStock: Number(form.openingStock)
      })
      setMsg('Product created successfully')
      setShowForm(false)
      setForm({ sku: '', name: '', description: '', categoryId: '', costPrice: '', sellingPrice: '', gstRate: '18', hsnCode: '', reorderLevel: '5', openingStock: '0' })
      load()
    } catch (err) {
      setMsg(err.response?.data?.message || 'Failed to create product')
    }
  }

  return (
    <div>
      <div className="btn-row">
        <button className="btn" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Close Form' : '+ Add Product'}
        </button>
      </div>

      {msg && <div style={{ marginBottom: 16, color: msg.includes('Failed') || msg.includes('failed') ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>{msg}</div>}

      {showForm && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="section-title">Add New Product</div>
          <form onSubmit={submit}>
            <div className="form-row">
              <div className="form-group"><label>SKU *</label><input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} required /></div>
              <div className="form-group"><label>Name *</label><input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
              <div className="form-group"><label>Category</label>
                <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
                  <option value="">Uncategorized</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Cost Price</label><input type="number" value={form.costPrice} onChange={(e) => setForm({ ...form, costPrice: e.target.value })} /></div>
              <div className="form-group"><label>Selling Price *</label><input type="number" value={form.sellingPrice} onChange={(e) => setForm({ ...form, sellingPrice: e.target.value })} required /></div>
              <div className="form-group"><label>GST Rate %</label><input type="number" value={form.gstRate} onChange={(e) => setForm({ ...form, gstRate: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>HSN Code</label><input value={form.hsnCode} onChange={(e) => setForm({ ...form, hsnCode: e.target.value })} /></div>
              <div className="form-group"><label>Reorder Level</label><input type="number" value={form.reorderLevel} onChange={(e) => setForm({ ...form, reorderLevel: e.target.value })} /></div>
              <div className="form-group"><label>Opening Stock</label><input type="number" value={form.openingStock} onChange={(e) => setForm({ ...form, openingStock: e.target.value })} /></div>
            </div>
            <div className="form-group" style={{ marginBottom: 14 }}><label>Description</label><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={2} /></div>
            <button className="btn" type="submit">Save Product</button>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>SKU</th><th>Name</th><th>Category</th><th>Cost</th><th>Selling Price</th><th>GST</th><th>HSN</th><th>Reorder Level</th><th>Status</th></tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td><code>{p.sku}</code></td>
                  <td><strong>{p.name}</strong></td>
                  <td>{p.category?.name || '—'}</td>
                  <td>{p.costPrice ? fmtINR(p.costPrice) : '—'}</td>
                  <td>{fmtINR(p.sellingPrice)}</td>
                  <td>{p.gstRate}%</td>
                  <td>{p.hsnCode || '—'}</td>
                  <td>{p.reorderLevel}</td>
                  <td>{p.active ? <span className="badge badge-green">Active</span> : <span className="badge badge-gray">Inactive</span>}</td>
                </tr>
              ))}
              {products.length === 0 && <tr><td colSpan={9} className="empty">No products yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

