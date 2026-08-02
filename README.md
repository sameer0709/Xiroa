# ⚡ Xiroa — Business ERP for Small Businesses

A modern, full-stack **Enterprise Resource Planning (ERP)** system for Indian SMEs — retail stores, manufacturers, and wholesalers — featuring inventory, GST billing, CRM, HR & payroll, **AI business insights**, and **sales forecasting**.

**Stack:** Spring Boot 3 (Java 21) · PostgreSQL (H2 for dev) · React 18 + Vite · Docker · JWT Auth · Recharts · OpenAPI/Swagger

---

## ✨ Features

| Module             | Highlights                                                                                            |
| ------------------ | ----------------------------------------------------------------------------------------------------- |
| 📦 Inventory       | Stock levels, reorder alerts, stock movements, warehouse tracking                                     |
| 🧾 Billing & GST   | Tax invoices with HSN codes, GST computation (CGST/SGST), partial payments, payment tracking          |
| 👥 CRM             | Customer & vendor management, GSTIN, purchase history, loyalty stats                                  |
| 👔 Employee Mgmt   | Departments, designations, leave requests & approvals                                                 |
| 💰 Payroll         | Basic + HRA + allowances − deductions = net pay, month/year processing, mark-paid flow                |
| 🚚 Purchase Orders | Vendor PO creation with line items and stock-in on receipt                                            |
| 🛒 Sales Orders    | Order management pipeline                                                                             |
| 🤖 AI Insights     | Rule-based business health engine (low stock, slow movers, receivables, sales momentum); OpenAI-ready |
| 📈 Sales Forecast  | Moving-average + linear-trend forecast for next N months                                              |
| 📊 Dashboard       | Revenue KPIs, sales trend, top products, top customers, low-stock, inventory value                    |
| 🔐 Auth & Roles    | JWT; `ADMIN` / `MANAGER` / `EMPLOYEE` role-based access; Swagger UI                                   |

---

## 🔑 Default Credentials

On a fresh database, Xiroa **always** creates a default admin account so you can log in immediately:

| Role                | Username | Password                                   |
| ------------------- | -------- | ------------------------------------------ |
| Admin (full access) | `admin`  | `admin123` (override via `ADMIN_PASSWORD`) |

> ⚠️ **Important:** Change the admin password before any real deployment (set `ADMIN_PASSWORD`).

### Demo dataset (optional)

Demo data (products, invoices, customers, payroll, manager/employee logins) is **disabled by default** for production. To load the sample dataset for sales demos:

```powershell
# PowerShell (Windows)
$env:SEED_DEMO = "true"
cd backend
..\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

When `SEED_DEMO=true` you also get:

| Role     | Username   | Password      |
| -------- | ---------- | ------------- |
| Manager  | `manager`  | `manager123`  |
| Employee | `employee` | `employee123` |

---

## 🚀 Quick Start (Local Dev, no Docker required)

### Prerequisites

- **Java 21** (Temurin/OpenJDK)
- **Node.js 18+**
- Maven optional (uses local copy at `tools/apache-maven-3.9.9`, or use `mvn`)

### 1. Start the backend (H2 in-memory DB auto-seeds demo data)

```powershell
cd e:\coding\Projects\erp\backend
..\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

- API → http://localhost:8080
- Swagger UI → http://localhost:8080/swagger-ui.html
- H2 console → http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:xiroadb`, user `sa`, blank pwd)

### 2. Start the frontend

```powershell
cd e:\coding\Projects\erp\frontend
npm install
npm run dev
```

- App → http://localhost:5173 (proxies `/api` → backend :8080)

### 3. Login with the demo credentials above.

---

## 🐳 Run with Docker + PostgreSQL (production-like)

```powershell
cd e:\coding\Projects\erp\docker
docker compose up --build
```

- Frontend → http://localhost:5173
- Backend → http://localhost:8080

> **Note:** Ensure Docker Desktop is installed and running first.

---

## 📁 Project Structure

```
erp/
├─ backend/                      # Spring Boot 3 REST API
│  └─ src/main/java/com/erp/
│     ├─ config/                 # Security, OpenAPI, DataSeeder
│     ├─ entity/                 # JPA entities
│     ├─ repository/             # Spring Data repositories
│     ├─ dto/                    # Request/response records
│     ├─ service/                # Business logic + AI insights + forecast
│     ├─ controller/             # REST endpoints
│     ├─ security/               # JWT filter, user details, token service
│     ├─ enums/                  # RoleName, PaymentStatus, etc.
│     └─ exception/              # GlobalExceptionHandler
│  ├─ src/main/resources/        # application.yml (H2), application-postgres.yml
│  └─ Dockerfile
├─ frontend/                     # React 18 + Vite SPA
│  ├─ src/api/client.js          # Axios instance w/ JWT interceptor
│  ├─ src/context/AuthContext.jsx
│  ├─ src/pages/                 # Dashboard, Inventory, Invoices, ...
│  ├─ src/components/Layout.jsx  # Sidebar + topbar
│  ├─ vite.config.js             # /api proxy
│  └─ Dockerfile                 # Nginx build
├─ docker/docker-compose.yml     # Postgres + backend + frontend
├─ STARTUP_ROADMAP.md            # 🎯 Next steps for your startup
└─ README.md
```

---

## 🔌 Main API Endpoints

| Method   | Endpoint                                        | Description                 |
| -------- | ----------------------------------------------- | --------------------------- |
| POST     | `/api/auth/login`                               | Login → JWT                 |
| POST     | `/api/auth/register`                            | Create user                 |
| GET      | `/api/dashboard`                                | KPIs + charts data          |
| GET/POST | `/api/products`                                 | Product catalog             |
| GET/POST | `/api/inventory`                                | Stock levels / adjustments  |
| GET/POST | `/api/invoices`                                 | Invoices (GST)              |
| GET/POST | `/api/customers` · `/api/customers/vendors`     | CRM                         |
| GET/POST | `/api/employees` · `/api/employees/payroll`     | HR & payroll                |
| GET/POST | `/api/purchase-orders` · `/api/sales-orders`    | Purchasing & sales pipeline |
| GET      | `/api/insights` · POST `/api/insights/generate` | AI business insights        |
| GET      | `/api/forecast?months=6`                        | Sales forecast              |

Full interactive docs at **Swagger UI**.

---

## 🧪 Verified Smoke Test Results

| Check                      | Result                                                   |
| -------------------------- | -------------------------------------------------------- |
| Backend `mvn compile`      | ✅ PASS                                                  |
| Boot + seed (H2 in-memory) | ✅ 5 products, 3 customers, 4 employees, ~18 invoices    |
| Login → JWT                | ✅ 200                                                   |
| `/api/dashboard`           | ✅ totalRevenue 18,33,179.50 · 18 invoices · trend 6 pts |
| `/api/invoices`            | ✅ 18 (JSON recursion fixed via `@JsonIgnore`)           |
| `/api/insights/generate`   | ✅ 4 insights (low stock, receivables, …)                |
| `/api/forecast`            | ✅ 9 points, moving-average + linear trend               |
| Frontend `npm run build`   | ✅ 716 modules built in ~6s                              |

---

## 🔐 Security Notes (Before Production)

- Rotate `XIROA_JWT_SECRET` env var (or `xiroa.jwt.secret` in `application.yml`).
- Switch to PostgreSQL (already supported) and set `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.
- See `DEPLOYMENT.md` and `.env.example` for production setup.
- Add HTTPS (e.g., Let's Encrypt / cloud LB).
- Add API rate limiting & refresh tokens for hardened auth.
