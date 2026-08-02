import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const navItems = [
  { section: 'Overview', items: [
    { to: '/', label: 'Dashboard', icon: '📊', end: true },
    { to: '/insights', label: 'AI Insights', icon: '🤖' },
    { to: '/forecast', label: 'Forecast', icon: '📈' }
  ]},
  { section: 'Operations', items: [
    { to: '/products', label: 'Products', icon: '📦' },
    { to: '/inventory', label: 'Inventory', icon: '🗃️' },
    { to: '/invoices', label: 'Billing & Invoices', icon: '🧾' },
    { to: '/purchase-orders', label: 'Purchase Orders', icon: '🛒' }
  ]},
  { section: 'CRM & People', items: [
    { to: '/customers', label: 'Customers & Vendors', icon: '👥' },
    { to: '/employees', label: 'Employees', icon: '👔' },
    { to: '/payroll', label: 'Payroll', icon: '💰' }
  ]}
]

export default function Layout() {
  const { user, logout } = useAuth()

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="logo">
          <div className="icon">⚡</div>
          <span>Xiroa</span>
        </div>
        {navItems.map((group) => (
          <div className="nav-group" key={group.section}>
            <div className="nav-group-title">{group.section}</div>
            {group.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <span className="nav-icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </aside>
      <main className="main">
        <div className="topbar">
          <div className="page-title">Xiroa — Business ERP for Small Businesses</div>
          <div className="user-chip">
            <div className="avatar">{(user?.fullName || 'U').charAt(0)}</div>
            <div>
              <div style={{ fontSize: 13, fontWeight: 600 }}>{user?.fullName}</div>
              <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{user?.role}</div>
            </div>
            <button className="logout-btn" onClick={logout}>Logout</button>
          </div>
        </div>
        <Outlet />
      </main>
    </div>
  )
}

