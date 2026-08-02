# 🧭 COMPLETE BEGINNER'S GUIDE

## How to Manage, Prepare, Deploy & SELL Your Xiroa ERP

_(Written for a first-time founder — no tech experience assumed)_

---

# PART A — WHAT EXACTLY DO YOU HAVE?

Your project folder `erp/` contains 4 useful things:

| Folder / File               | What it is (plain English)                                                                             |
| --------------------------- | ------------------------------------------------------------------------------------------------------ |
| `backend/`                  | The **brain** of the app. Stores data, handles login, GST math, payroll, AI insights. Written in Java. |
| `frontend/`                 | The **face** of the app. The screens your customers see in the browser. Written in React.              |
| `docker/` + `Dockerfile`s   | "Boxes" that package the app so it can run on any computer/server. You'll use this when deploying.     |
| `README.md`                 | Instructions for running the app on your own computer.                                                 |
| `STARTUP_ROADMAP.md`        | High-level business plan.                                                                              |
| `SELLING_GUIDE_BEGINNER.md` | ← This file. Your step-by-step playbook.                                                               |
| `TODO.md`                   | Checklist of what was built and verified.                                                              |

**Key mental model:** The app has TWO parts that must run together:

- **Backend** at `http://localhost:8080` (the brain / API)
- **Frontend** at `http://localhost:5173` (the screens)

Right now both are running on YOUR computer. That's fine for demoing. Before selling, you must move it to an **online server** (Part E) so customers can use it from anywhere.

---

# PART B — HOW TO MANAGE / OPERATE IT (Your Daily Routine)

### B1. Starting & Stopping (if your computer reboots)

**Start backend:**

```
cd e:\coding\Projects\erp\backend
..\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

**Start frontend (in a second terminal):**

```
cd e:\coding\Projects\erp\frontend
npm run dev
```

Then open `http://localhost:5173` and log in.

### B2. Where is the DATA?

- **Demo mode (current):** H2 in-memory database. Data is **wiped every time backend restarts**. Fine for demos. NOT for real customers.
- **Production (before selling):** switch to **PostgreSQL** — data is saved permanently. (Instructions in Part C.)

### B3. Back up your work (VERY IMPORTANT — do this today)

1. Create a free account at **github.com**
2. Install **GitHub Desktop** (easy visual app)
3. In GitHub Desktop: File → Add Local Repository → choose `e:\coding\Projects\erp`
4. Press **Commit** then **Push** (upload to the cloud)
5. Now your whole project is safe online. Do this after every change.

### B4. Admin logins (keep safe, change before selling)

- Admin: `admin / admin123`
- Manager: `manager / manager123`
- Employee: `employee / employee123`

---

# PART C — WHAT TO DO **BEFORE** SELLING (Readiness Checklist)

Do these IN ORDER. Don't skip.

## C1. Business & Legal (India focus — check with a CA)

- [ ] **Registrations.** Start as **Sole Proprietorship** (free/simplest) or **OPC/Pvt Ltd** if you want to raise money later.
- [ ] **GST registration** — selling SaaS software = service, GST ~18% (your CA confirms).
- [ ] **Bank account** for your business.
- [ ] **Terms of Service + Privacy Policy** pages (can copy templates from any SaaS site and adapt).
- [ ] **Software subscription agreement** — even a 1-page simple contract per customer.

## C2. Technical "Go Live" Checklist (code-level fixes)

- [ ] **1. Secret key.** Set the env var `XIROA_JWT_SECRET` (or edit `xiroa.jwt.secret` in `backend/src/main/resources/application.yml`) to a long random string. This is like a password that signs logins. (Keep it secret!) Generate with `openssl rand -base64 48`.
- [ ] **2. Switch to PostgreSQL** (real database). Create a free database at **neon.tech** or **supabase.com** (both free tiers). Then run backend with:
  ```
  set SPRING_PROFILES_ACTIVE=postgres
  set DB_URL=jdbc:postgresql://<your-neon-host>/xiroadb
  set DB_USERNAME=...
  set DB_PASSWORD=...
  ..\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
  ```
  Or use the included `docker-compose.yml` which sets up Postgres automatically.
- [ ] **3. Real branding.** The app is pre-branded "Xiroa". Change colors (look in `frontend/src/styles.css`), add your logo (edit `frontend/index.html` and `Layout.jsx`).
- [ ] **4. Payment collection.** Integrate **Razorpay** (Indian SME-friendly) so customers can pay you online. Before that, collect via **UPI QR / bank transfer** manually — fine to start.
- [ ] **5. Remove demo data for real customers.** The app auto-seeds demo products/invoices. For paying customers, add a "per-company" separation (multi-tenant) OR reset data after their trial. (See C3.)
- [ ] **6. HTTPS + domain.** Buy a domain (~₹600/yr on GoDaddy/Hostinger) and enable free HTTPS. (Part E explains.)
- [ ] **7. Daily backups.** Set up automatic PostgreSQL backups. Neon/Supabase both offer this free.
- [ ] **8. WhatsApp/Email alerts (recommended).** Free for first version: use email (SMTP). Later add WhatsApp Business API.

## C3. ⚠️ CRITICAL: Build Multi-Tenant (One Code → Many Companies)

Right now the app = ONE company's data. If Customer A and Customer B both log in, **they'd see each other's data — that's a disaster.**

Before having more than 1 paying customer, add a **`companyId`/`tenantId`** to every table and every API call, so each business only sees its own data. This is the #1 technical task. (Ask your developer, or I can add it in a future round.)

**Shortcut for your FIRST customer:** deploy a fresh copy for them on a separate server/database. That's more work per customer but 0 coding. Only OK for the first 1–2 customers, then build multi-tenant.

## C4. 🚫 DO NOT GIVE AWAY YOUR SOURCE CODE!

- Selling **software as a service (SaaS)** = customers pay monthly, THEY NEVER GET the code. They get a **login link**.
- Selling the **source code** = you hand over your whole brain. People will copy it, resell it, or stop paying. **Don't do this** unless someone pays a huge one-time license fee (₹5–20 lakh+) with a legal contract.

---

# PART D — HOW TO SELL IT (Step-by-Step)

## D1. Pick ONE type of customer (beachhead)

Examples:

- Wholesalers/distributors in your city
- Kirana/grocery stores that bill digitally
- Small furniture/electronics manufacturers
  Pick ONE. Sell to them first. You'll learn their exact problems.

## D2. Price it simply

| Plan    | Price        | What they get                               |
| ------- | ------------ | ------------------------------------------- |
| Starter | ₹999/month   | Invoices + inventory, 3 users               |
| Growth  | ₹1,999/month | + employees/payroll/CRM, 10 users           |
| Pro     | ₹3,999/month | + AI insights + forecast + priority support |

Give **30-day free trial** at the start.

## D3. Your 5-minute demo script

1. Log in (show a clean new company, not your demo data)
2. **Create an invoice in 30 seconds** (impress them here)
3. Show inventory updating automatically after the sale
4. Show the **Dashboard** (revenue, low stock alerts)
5. Show AI Insights → "Generate Insights"
6. Say: _"This replaces your Excel books + billing + payroll. One login for everything. I'll set it up for you."_

## D4. Where to find customers (cheapest first)

1. **CA / accountants in your city** — offer 15% commission. They tell every SME client "use this software."
2. **WhatsApp groups** of local traders/wholesalers — post a 60-second video demo.
3. **Visit shops personally** — "Tirupati Hardware" style retail stores. 10 visits/day.
4. **YouTube/Instagram** — 60-sec vertical videos: "How to make a GST invoice in 60 seconds."
5. **Google Ads/SEO later** — once the first 5 customers validate it.

## D5. Payment → Onboarding → Support loop

1. Customer agrees → send a simple **invoice** ("Signup fee ₹0, monthly ₹1,999").
2. Take payment: **UPI QR** (start) or **Razorpay** (later).
3. **Set them up**: create their company login, import their products/customers from Excel if needed.
4. **Train them**: 1-hour video call. Record it and reuse.
5. **Weekly check-in** for first month. Fix any complaint within 24 hours.
6. Ask for a **Google review** + referral to one friend business.

## D6. Sell "outcomes," not software

Weak pitch: _"It's an ERP with modules."_
Strong pitch: _"You'll stop losing sales because you ran out of stock, cut billing mistakes, and know who hasn't paid you — all automatically. ₹1,999 a month."_

---

# PART E — SHOULD YOU DEPLOY IT? (YES — Here's How)

**What "deploy" means:** putting your app on an internet server with a permanent database so customers can use it 24/7 from anywhere, not just from your computer.

## Option 1 — EASIEST (Managed Cloud) ⭐ Recommended to start

| What                  | Where                                     | Cost            |
| --------------------- | ----------------------------------------- | --------------- |
| Database (PostgreSQL) | **neon.tech** or **supabase.com**         | Free tier       |
| Backend (API)         | **railway.app** or **render.com**         | ~$5–7/mo (₹500) |
| Frontend              | **vercel.com** (free) or same Railway app | Free            |
| Email for support     | Gmail business (₹69/mo) or free           | Free            |

Steps (for Railway/Render):

1. Create the free Postgres DB first → copy its connection URL.
2. Push your `backend/` folder to a GitHub repo (Part B3).
3. On Railway.app → **New Project → Deploy from GitHub** → select backend repo.
4. Add environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and your new JWT secret.
5. Deploy frontend on Vercel → point it to your backend URL.
6. Add your domain → HTTPS is automatic.

## Option 2 — VPS + Docker (More Control, ~₹600/mo)

1. Buy a small VPS: **Hostinger VPS ₹499–600/mo** or DigitalOcean $6 droplet (Ubuntu).
2. Install Docker, copy your `docker/` folder to the server.
3. Run `docker compose up -d` → Postgres + backend + frontend all start together automatically.
4. Point your domain to the server IP, install free HTTPS via Certbot.

## E3. Domain + HTTPS

- Buy domain: GoDaddy / Hostinger / Namecheap (~₹600–900/yr).
- HTTPS (the little padlock) is FREE and automatic on Vercel/Railway; on VPS use Certbot.
- NEVER make customers type "http://" or an IP address. Buy a domain.

---

# PART F — WHAT FILES DO YOU GIVE A CUSTOMER?

## For SaaS subscription (RECOMMENDED — what you should do):

**You give them almost nothing:**

- ✅ The website URL (`https://yourbrand.com`)
- ✅ Their username + temporary password
- ✅ A 2-page "How to use" PDF (I can help you create one)
- ✅ A 15-minute recorded demo video

**You keep:**

- 🔒 All source code (backend + frontend)
- 🔒 The database
- 🔒 The server

## If you somehow sell the source code (NOT recommended):

They'd get:

- `backend/` and `frontend/` folders → zip them
- `README.md` + `docker/` for setup
- A signed license agreement saying "one customer, no resale, no copying"

But again — **SaaS is better**: recurring money, you keep control, customers can't steal it.

---

# PART G — YOUR 90-DAY LAUNCH PLAN

| Days  | Goal                                                                                                  |
| ----- | ----------------------------------------------------------------------------------------------------- |
| 1–5   | Do Part C1 (register business, bank, GST) + C2 items (secret key, Postgres, branding) + GitHub backup |
| 6–8   | Deploy with Option 1 (free/cheap cloud) so you have a live public URL                                 |
| 9–15  | Add Razorpay (or start UPI QR). Build "How to use" PDF + demo video                                   |
| 16–30 | Talk to 20 local businesses (CAs, WhatsApp, shop visits). Get **3 free-trial signups**                |
| 31–60 | Convert trials → **3 paying customers**. Help them import data + train them                           |
| 61–90 | Monthly revenue from recurring fees. Ask for referrals + reviews. Fix top 3 complaints                |

**Milestone that matters most:** 5 paying customers at ~₹1,500 avg/month = ₹7,500 MRR. Then double down on what worked.

---

# PART H — QUICK ANSWERS / COMMON MISTAKES

**"Do they need my code?"** NO. They need a login link. Never share code.

**"What if they want a custom feature?"** Say "yes, I'll add it" and either raise their plan or charge a one-time fee. Keep a list of all requests — that's your product roadmap.

**"Can I sell before registering a company?"** You can start taking money as a proprietor, but register + get GST before it grows. Get a CA — worth the small fee.

**"What's the biggest risk?"** Customers A and B seeing each other's data (multi-tenant issue, Part C3). Fix/plan it before customer #2.

**"How much should I invest to start?"** Under ₹3,000: domain (~₹700) + cheapest cloud/hosting (~₹500–600/mo) + CA consult. Everything else is your time.

**"What if I can't do the technical parts (Postgres, deploy)?"** Options: (a) I can do these steps with you in the next round, (b) hire a freelancer for ~₹5,000–15,000 to do Part C2 + Part E once, (c) watch YouTube (search "deploy Spring Boot Railway", "deploy React Vercel").

---

> ⚠️ **Disclaimer:** This guide is general business education, not legal/tax advice. For India GST, company registration, contracts and DPDP (data privacy) compliance, consult a Chartered Accountant (CA). Never store customer financial data without a proper privacy policy and security practices.

---

**Your single most important action this week:** Get this project into a **GitHub repository** (Part B3) so your work can never be lost. Then book 5 conversations with local shop owners. Everything else can wait.
