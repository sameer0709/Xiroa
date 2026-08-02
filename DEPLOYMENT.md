# 🚀 Xiroa — Deployment Guide

This guide walks you through taking the Xiroa ERP from "runs on my laptop" to
"live on the internet so customers can use it."

---

## 1. Before you deploy (5-minute checklist)

1. **Copy `.env.example` to `.env`** and fill in real values (DB password + a
   long random `XIROA_JWT_SECRET`). Generate a secret with:
   ```
   openssl rand -base64 48
   ```
2. **Create a PostgreSQL database** (free options):
   - [Neon](https://neon.tech) — free Postgres, easy
   - [Supabase](https://supabase.com) — free Postgres + backups
     Copy the connection string — it looks like:
     `postgresql://user:password@host/dbname`
     That becomes `DB_URL=jdbc:postgresql://host/dbname` (with `user`/`password`
     in `DB_USERNAME`/`DB_PASSWORD`).
3. **Make sure you're on a Git repo** (see `README` → "Push to GitHub").

---

## 2. Option A — Managed Cloud (EASIEST, recommended to start)

### Backend → Railway (or Render)

1. Create a free account at **railway.app** (or **render.com**).
2. Push this project to a **GitHub repo** (steps in README).
3. On Railway: **New Project → Deploy from GitHub** → select the repo.
4. Set the build/start command (Railway auto-detects Maven; if not):
   - Build: `cd backend && mvn clean package -DskipTests`
   - Start: `java -jar backend/target/xiroa-backend-1.0.0.jar`
5. Add environment variables from `.env.example`:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `XIROA_JWT_SECRET`
   - `SPRING_PROFILES_ACTIVE=postgres`
6. Deploy. Railway gives you a public URL like `https://xiroa-up.railway.app`.

### Frontend → Vercel (free)

1. Create account at **vercel.com**.
2. **Add New Project → Import** your GitHub repo → root = `frontend/`.
3. Build command: `npm run build` · Output dir: `dist`.
4. Add environment variable `VITE_API_URL` if your frontend calls the backend
   directly (otherwise the Vite proxy only works locally). Set it to
   `https://xiroa-up.railway.app/api`.
   > If you use the `nginx.conf` approach (Docker compose) the frontend
   > proxies `/api` to the backend automatically — no `VITE_API_URL` needed.
5. Deploy. You get `https://xiroa-frontend.vercel.app`.

### Domain + HTTPS

- Buy a domain (GoDaddy / Hostinger / Namecheap ~₹600/yr).
- In Railway: Settings → Domains → add `api.xiroa.in`.
- In Vercel: Project → Domains → add `xiroa.in`.
- HTTPS is automatic on both.

---

## 3. Option B — Docker Compose (Single VPS, ~₹600/mo)

Use this if you prefer one server running everything.

1. Buy a VPS: **Hostinger VPS** (~₹500–600/mo) or **DigitalOcean** ($6 droplet).
2. SSH into it, install Docker + Compose:
   ```
   curl -fsSL https://get.docker.com | sh
   sudo usermod -aG docker $USER   # then re-login
   ```
3. Copy the `docker/` folder and the repo to the server.
4. Create `.env` next to `docker-compose.yml` (copy from `.env.example`).
5. Start:
   ```
   cd docker
   docker compose up -d --build
   ```
6. Everything starts: Postgres + backend (8080) + frontend (5173).
7. Point your domain to the server IP, then add free HTTPS with Caddy or Certbot.

---

## 4. After deploy (must-do)

- [ ] Open `https://yourdomain.com` → log in with `admin/admin123`
- [ ] **Change the admin password immediately**.
- [ ] Disable H2 console in production (`spring.h2.console.enabled=false`
      via `SPRING_PROFILES_ACTIVE=postgres` — the postgres profile already
      excludes it).
- [ ] Turn on **automatic DB backups** (Neon/Supabase both offer this).
- [ ] If more than one customer: add **multi-tenant companyId** isolation
      (see `SELLING_GUIDE_BEGINNER.md` Part C3).
- [ ] Add rate limiting + refresh tokens for hardened auth (future sprint).

---

## 5. Env var reference

| Variable                 | Purpose                                 | Default                                    |
| ------------------------ | --------------------------------------- | ------------------------------------------ |
| `DB_URL`                 | JDBC URL for PostgreSQL                 | `jdbc:postgresql://localhost:5432/xiroadb` |
| `DB_USERNAME`            | DB user                                 | `xiroa`                                    |
| `DB_PASSWORD`            | DB password                             | `xiroa`                                    |
| `XIROA_JWT_SECRET`       | JWT signing secret (>=32 chars, random) | dev-only fallback                          |
| `AI_PROVIDER`            | `rule-based` or `openai`                | `rule-based`                               |
| `OPENAI_API_KEY`         | Optional, for LLM insights              | empty                                      |
| `SPRING_PROFILES_ACTIVE` | `default` (H2) or `postgres`            | `default`                                  |

---

> ⚠️ General guidance, not legal/security advice. Consult a CA for GST/DPDP
> compliance and a security professional before storing real customer data.
