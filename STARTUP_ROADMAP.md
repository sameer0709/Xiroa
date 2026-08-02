# 🚀 Xiroa — Business ERP for Small Businesses — Startup Roadmap

You've built the MVP. Now here is the **step-by-step playbook** to turn this into a real startup.

---

## 1. Validate Before You Scale (Weeks 1–3)

### 1.1 Talk to Your Target Customers (25–50 interviews)

Get **problem interviews**, not feature pitches, with:

- Retail store owners (kirana → mid-size)
- Wholesalers & distributors
- Small manufacturers (10–200 employees)

Ask:

1. "How do you manage inventory + billing today?" (Excel? books? Zoho? Tally?)
2. "What's the single biggest waste of time/money in your operations?"
3. "How much would you pay per month for software that fixes it?"

> Target: find **1 painful, must-solve problem**. Solve it better than anyone else.

### 1.2 Pick Your Beachhead

Do NOT sell "ERP to everyone." Pick ONE:

- Beachhead A: **GST billing + inventory** for wholesalers (fastest to value)
- Beachhead B: **Small manufacturer job-work / stock tracking**
- Beachhead C: **Retail chain (2–10 stores) multi-location stock**

### 1.3 Competitive & Pricing Research

| Competitor           | Price (₹/mth)       | Weakness you can exploit               |
| -------------------- | ------------------- | -------------------------------------- |
| TallyPrime           | ~₹18–25k (one-time) | Old UX, no AI, desktop-first           |
| Zoho Inventory/Books | ₹1.4k–5k            | Too generic, US-centric                |
| Vyapar (mobile)      | Free–₹3k            | Mobile-first, weak desktop/back-office |
| Busy                 | Perpetual license   | No AI insights                         |
| Odoo Community       | free SW, ₹ custom   | Complex, needs SI                      |

**Sensible pricing wedge:** ₹999–₹1,999 per business/month ($12–24), tiered:

- Starter ₹999/mo — billing + inventory (5 users)
- Growth ₹1,999/mo — + payroll + CRM + reports
- Pro ₹3,999/mo — + AI insights + forecast + priority support

### 1.4 Build a No-Code Landing Page

Tools: Framer / Webflow / Carrd.

- Headline: _"Xiroa — inventory, GST billing & payroll for Indian SMEs in one place."_
- Include: screenshots, pricing, "Get early access", email capture form, WhatsApp CTA.

---

## 2. Turn the MVP into a Sellable Product (Weeks 4–8)

1. **Multi-tenant SaaS** — add `tenantId`/company schema to all entities (critical before first customer).
2. **Subscription billing** — Razorpay/Stripe integration + usage limits.
3. **Email + WhatsApp notifications** — low-stock alerts, invoice sent, payroll processed.
4. **Role-based onboarding wizard** — new company setup in < 5 minutes.
5. **Import wizards** — Excel/CSV import for products, customers, opening stock.
6. **GST-ready reports** — GSTR-1, GSTR-3B export, e-invoice JSON (IRN) via GSP partner.
7. **Polish** — empty states, loading skeletons, error toasts, keyboard shortcuts.
8. **Telemetry** — log feature usage (no PII) to know what to improve.
9. **Unit/integration tests** — backfill critical paths (auth, billing, payroll).

> ⚠️ **India compliance** (get advice from a CA): GST registration, e-invoicing above ₹5Cr, data residency (host on Indian cloud regions), DPDP Act privacy.

---

## 3. Go to Market (Months 2–3)

### 3.1 Channels (most → least effort-efficient for B2B SME SaaS)

1. **Offline partnerships**: CA firms, accountants, GST practitioners (_they own the customer_). Revenue-share 10–15%.
2. **WhatsApp + community**: SME owner groups, trade associations (e.g., local wholesale mandi associations).
3. **Content/SEO**: "GST billing software for wholesalers", "inventory software for kirana", "payroll software India" — long-tail keywords.
4. **YouTube demos**: 60–90s screen recordings showing GST invoice → inventory update → AI insight.
5. **Cold outreach**: phone/WhatsApp (ASV, not email) — restock with response.

### 3.2 Sales Motion

- **Free 30-day trial** → convert via live onboarding call.
- Simplify first 90 days: _"Get 3 paying customers who love you."_
- Measure **NPS + churn monthly**; keep a log of every failed logout/showstopper.

---

## 4. Monetization Precision

- **Trial**: 30-days free, full features, 1 business.
- **Annual prepay discount** (2 months free) to improve cashflow.
- **Fair use caps**: invoice count/month per tier (e.g., 500/3000/unlimited).
- Add-ons: AI Insights Pro (₹499/mo), extra users, WhatsApp alerts.

---

## 5. Fundraising (Only If Needed)

**Bootstrapping** is best at this stage — SME SaaS is naturally profitable. Raise external capital only to:

- Hire senior engineers / sales team
- Expand beyond your beachhead geography

If raising angels (India):

- Valuation reference: 5–10× ARR for early-stage SaaS
- Prepare: 12-mo ARR model, CAC/LTV, churn, 3 customer testimonials, working demo.

---

## 6. Recommended Tech & Product Evolutions

| Now (MVP done)            | Next 6 months                                                            | Later                                               |
| ------------------------- | ------------------------------------------------------------------------ | --------------------------------------------------- |
| Spring Boot + H2/Postgres | Multi-tenant PostgreSQL + Redis cache                                    | Event-driven (Kafka), microservices if truly needed |
| Rule-based AI insights    | **OpenAI / Gemini integration** for natural-language "ask your business" | Fine-tuned ML for demand forecasting                |
| JWT auth                  | Refresh tokens, 2FA, SSO                                                 | RBAC matrix + audit logs (DPDP)                     |
| Vite SPA                  | PWA offline for shop floors                                              | Native mobile apps                                  |
| Nginx container           | AWS Mumbai / GCP Mumbai, CI/CD (GitHub Actions)                          | EKS / managed k8s                                   |

---

## 7. Milestone Checkpoints

- [ ] **M1** — 30 customer interviews done → documented #1 pain
- [ ] **M2** — Landing page live with email capture
- [ ] **M3** — Multi-tenant + subscriptions live
- [ ] **M4** — 5 paying customers @ avg ₹1,500/mo = ₹7,500 MRR
- [ ] **M5** — 3 CA partnerships signed
- [ ] **M6** — ₹50k MRR (~33 customers) — now consider hiring
- [ ] **M7** — ₹5L MRR (~330 customers) — ready for scale/raise

---

## 8. Founder 90-Day Playbook

**Days 1–10** — Do 10 customer interviews/week; record everything.
**Days 11–20** — Prioritize features by interview themes; cut scope ruthlessly.
**Days 21–30** — Ship multi-tenant + payments; land first pilot customer (free or discounted).
**Days 31–60** — Sell 3 paying customers; run weekly demo sessions.
**Days 61–90** — Double down on the channel that works; write the first case study.

> **Rule of thumb:** If you are not embarrassed by the product you ship in month 2, you shipped too late. Speed > perfection in a startup.

---

## 9. Key Metrics to Track (weekly)

| Metric                                | Target         |
| ------------------------------------- | -------------- |
| MRR (monthly recurring revenue)       | +10% WoW early |
| Gross churn / logo churn              | < 5%/mo        |
| Trial → Paid conversion               | > 20%          |
| CAC payback                           | < 12 months    |
| NPS                                   | > 40           |
| Time-to-value (first invoice created) | < 15 min       |
