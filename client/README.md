# Drift — Car Pooling Frontend

A React + Vite frontend for the **Car Pooling System** (CS6652 — University of Limerick).
Communicates with the Spring Boot microservices backend via the API Gateway on `localhost:8080`.

## Tech Stack

- **React 18** + **Vite 6** (lightning-fast dev server)
- **React Router 6** for navigation
- **Axios** for HTTP with JWT interceptor
- **Plain CSS** (no framework) — editorial design system with custom tokens
- **Google Fonts**: Fraunces, Instrument Serif, Inter Tight, JetBrains Mono

## Pages

| Route | Description | Access |
|-------|-------------|--------|
| `/` | Landing page (hero, manifesto, stats) | Public |
| `/login` | Login | Public |
| `/register` | 3-step registration wizard | Public |
| `/rides` | Search & browse available rides | Authenticated |
| `/rides/:id` | Ride detail + book + pay | Authenticated |
| `/host` | Create/manage rides | Driver only |
| `/bookings` | Trip history + rate drivers | Authenticated |
| `/profile` | View/edit profile | Authenticated |

## Architecture Notes

- All requests go through `http://localhost:8080` (API Gateway) — set in `src/api/client.js`
- JWT token stored in `localStorage` under key `drift_token`
- Token attached automatically as `Authorization: Bearer <token>` to every request
- Auth state managed by `AuthContext`, restoring sessions on page reload
- Toast notifications via `ToastContext`

## Quick Start

```bash
npm install
npm run dev
```

Frontend runs at **http://localhost:3000**. See [HOW_TO_RUN.md](HOW_TO_RUN.md) for the full setup including IntelliJ.
