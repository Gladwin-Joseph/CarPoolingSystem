# How to Run — Complete Setup Guide

This guide walks you through running the **entire Car Pooling System** end-to-end:
backend (Spring Boot microservices + Supabase) + frontend (React).

---

## Prerequisites

Before you start, make sure you have these installed:

| Tool | Version | Check |
|------|---------|-------|
| **JDK 21+** (or 25, since your parent POM targets 25) | Latest | `java --version` |
| **Maven 3.9+** | Latest | `mvn --version` |
| **Node.js 18+** | LTS or higher | `node --version` |
| **npm** (comes with Node) | 9+ | `npm --version` |
| **IntelliJ IDEA** | Ultimate or Community | — |
| **Supabase project** | (already set up) | — |

---

## Part 1 — Run the Backend in IntelliJ

### Step 1.1: Open the Backend Project

1. Open **IntelliJ IDEA**
2. Click **File → Open**
3. Navigate to your `car-pooling-system/` folder (the multi-module Maven project)
4. Click **Open**
5. When prompted "Trust and Open Maven Project?" → **Trust Project**
6. Wait for Maven to download all dependencies (this may take 2–5 minutes the first time)
   - Watch the bottom-right progress bar
   - You'll know it's done when "Indexing" disappears

### Step 1.2: Verify Java SDK

1. **File → Project Structure** (Ctrl+Alt+Shift+S / Cmd+;)
2. Under **Project**, set:
   - **SDK**: 21 or 25
   - **Language level**: 21 or 25
3. Click **OK**

### Step 1.3: Update Backend application.properties Files

Make sure these 3 files have your real Supabase credentials:

- `user-service/src/main/resources/application.properties`
- `ride-service/src/main/resources/application.properties`
- `payment-service/src/main/resources/application.properties`

In each file, replace:
```properties
spring.datasource.url=jdbc:postgresql://db.<YOUR_PROJECT_REF>.supabase.co:5432/postgres
spring.datasource.password=<YOUR_DATABASE_PASSWORD>
```

In `user-service`, also generate and paste a JWT secret:
```bash
openssl rand -base64 32
```

```properties
jwt.secret=<paste_generated_key_here>
```

### Step 1.4: Run Services in Order (CRITICAL — Order Matters)

You'll create **5 run configurations** in IntelliJ. Open each application class and click the green ▶ next to `main()`, **in this exact order**, waiting for each to fully start before the next:

| Order | Service | Main Class | Port | Wait For |
|-------|---------|-----------|------|----------|
| **1st** | Discovery Server | `DiscoveryServerApplication.java` | 8761 | "Started DiscoveryServerApplication" |
| **2nd** | User Service | `UserServiceApplication.java` | 8081 | "Registered with Eureka" |
| **3rd** | Ride Service | `RideServiceApplication.java` | 8082 | "Registered with Eureka" |
| **4th** | Payment Service | `PaymentServiceApplication.java` | 8083 | "Registered with Eureka" |
| **5th** | API Gateway | `ApiGatewayApplication.java` | 8080 | "Started ApiGatewayApplication" |

**Tip — Run multiple services simultaneously:**
- IntelliJ has a **"Services"** tool window (View → Tool Windows → Services)
- Or use the **Run** dropdown to switch between configs
- You can also enable "Allow multiple instances" if you ever need duplicates

### Step 1.5: Verify Backend is Running

Open these in your browser:

1. **Eureka Dashboard:** http://localhost:8761
   - You should see all 4 services registered: `USER-SERVICE`, `RIDE-SERVICE`, `PAYMENT-SERVICE`, `API-GATEWAY`

2. **Health checks:**
   - http://localhost:8081/actuator/health (User Service)
   - http://localhost:8082/actuator/health (Ride Service)
   - http://localhost:8083/actuator/health (Payment Service)

3. **Circuit breaker status:**
   - http://localhost:8082/actuator/circuitbreakers
   - http://localhost:8083/actuator/circuitbreakers

If any service fails to start, check:
- Did the Discovery Server start first?
- Are your Supabase credentials correct?
- Did you run the `supabase-setup.sql` script?

---

## Part 2 — Run the Frontend

### Step 2.1: Install Node.js (if not installed)

Download from https://nodejs.org/ (LTS version recommended).
Verify with `node --version` and `npm --version`.

### Step 2.2: Open the Frontend in IntelliJ

You have two choices:

**Option A — Same window as backend (recommended)**
1. In IntelliJ, **File → Open**
2. Navigate to `carpool-frontend/`
3. When asked "How would you like to open this project?" choose **"Attach"**
4. Both backend and frontend will be visible in the project pane

**Option B — Separate IntelliJ window**
1. **File → Open** → select `carpool-frontend/` → **New Window**

### Step 2.3: Install Frontend Dependencies

Open IntelliJ's **Terminal** (Alt+F12 / View → Tool Windows → Terminal):

```bash
cd carpool-frontend
npm install
```

This installs React, Vite, Axios, React Router, etc. Takes about 30 seconds.

### Step 2.4: (Optional) Set Up Run Configuration in IntelliJ

For one-click frontend startup:

1. **Run → Edit Configurations…**
2. Click **+** → **npm**
3. Configure:
   - **Name:** `Frontend Dev`
   - **package.json:** point to `carpool-frontend/package.json`
   - **Command:** `run`
   - **Scripts:** `dev`
4. Click **OK**
5. Now you can run the frontend with the green ▶ button

### Step 2.5: Start the Frontend

In the terminal:
```bash
npm run dev
```

You should see:
```
VITE v6.x.x  ready in xxx ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

The browser should auto-open. If not, visit **http://localhost:3000**.

---

## Part 3 — End-to-End Test Flow

Now that **everything is running**, let's verify the full flow works.

### 1. Register as a Driver
- Click **"Get started"** on the landing page
- **Step 1:** Fill in name, username, email, password, phone
- **Step 2:** Choose **Driver**
- **Step 3:** Fill in license number, plate, car model, capacity
- Click **"Create account →"**
- ✅ You should land on `/rides` with a "Welcome to Drift" toast

### 2. Host a Ride
- Click **"Host"** in the nav
- Fill in: source, destination, departure (must be in future), price, seats
- Click **"Publish ride →"**
- ✅ Auto-switches to the **Manage** tab showing your new ride

### 3. Sign Out, Register as a Passenger
- Click **"Sign out"** in the top nav
- Click **"Get started"** → register a different user as **Passenger**

### 4. Search & Book the Ride
- Once logged in, the `/rides` page lists all available rides
- Use the search bar (or just browse)
- Click any ride → ride detail page
- Choose seats with the +/− picker
- Click **"Confirm booking →"**
- ✅ The widget transforms into a payment form

### 5. Pay
- Choose a payment method (CARD, WALLET, BANK_TRANSFER, CASH)
- Click **"Pay €X.XX →"**
- ✅ Redirected to **/bookings** with success toast

### 6. Rate the Driver
- On the bookings page, click **"Rate driver"** on the confirmed booking
- Pick a star rating (1–5)
- Optionally add a comment
- Click **"Submit rating →"**
- ✅ Rating saved

### 7. Edit Profile
- Click **Profile** in the nav
- Click **"Edit profile"**
- Change phone number, save
- ✅ Profile updated

---

## Troubleshooting

### Frontend Issues

| Problem | Cause | Solution |
|---------|-------|----------|
| `npm install` fails | Old Node version | Upgrade Node to v18+ |
| Page is blank | JS error | Open DevTools (F12) → Console |
| "Network Error" on every request | Backend not running | Verify all services on Eureka dashboard |
| CORS error in browser console | API Gateway CORS config | See "CORS" section below |
| 401 Unauthorized everywhere | JWT token expired | Sign out & log back in |
| Pages flash then redirect to login | Session restore failed | Clear localStorage, log in again |

### Backend Issues

| Problem | Solution |
|---------|----------|
| Service won't start | Check Discovery Server is running first |
| `Connection refused` to Supabase | Verify URL & password in `application.properties` |
| `relation "users" does not exist` | Run `supabase-setup.sql` in Supabase SQL Editor |
| Feign call fails | Check both services are registered with Eureka |
| Circuit breaker keeps opening | Target service likely down — check logs |

### CORS

If you see CORS errors in the browser console, verify your `api-gateway/src/main/resources/application.yml` has this section (it should already, from earlier):

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

If you change frontend port (e.g., to 5173), update `allowedOrigins` to match.

### Port Conflicts

If a port is in use:
- **macOS/Linux:** `lsof -i :8080` then `kill -9 <PID>`
- **Windows:** `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`

---

## Project File Structure

```
carpool-frontend/
├── public/
│   └── favicon.svg
├── src/
│   ├── api/
│   │   ├── client.js          # Axios instance + JWT interceptor
│   │   ├── userApi.js         # User Service endpoints
│   │   ├── rideApi.js         # Ride Service endpoints
│   │   └── paymentApi.js      # Payment Service endpoints
│   ├── components/
│   │   ├── Navbar.jsx         # Sticky top nav
│   │   ├── Footer.jsx
│   │   ├── ProtectedRoute.jsx # Auth guard
│   │   └── RideCard.jsx       # Ride list item
│   ├── context/
│   │   ├── AuthContext.jsx    # Login state + JWT decode
│   │   └── ToastContext.jsx   # Notifications
│   ├── pages/
│   │   ├── Landing.jsx        # Public hero page
│   │   ├── Login.jsx
│   │   ├── Register.jsx       # 3-step wizard
│   │   ├── Rides.jsx          # Search / browse
│   │   ├── RideDetail.jsx     # View / book / pay
│   │   ├── Host.jsx           # Driver: create / manage
│   │   ├── Bookings.jsx       # Trip history / rate
│   │   ├── Profile.jsx        # View / edit profile
│   │   └── NotFound.jsx       # 404
│   ├── styles/
│   │   └── global.css         # Design tokens + base styles
│   ├── App.jsx                # Router setup
│   └── main.jsx               # Entry + providers
├── index.html                 # Vite entry HTML
├── vite.config.js             # Dev server on port 3000
├── package.json
└── .env.example               # API URL override
```

---

## Production Build (Optional)

To build a production bundle:

```bash
npm run build
```

Output goes to `dist/`. To preview:
```bash
npm run preview
```

For real deployment (e.g., Render, Vercel, Netlify):
- Build the `dist/` folder
- Update `VITE_API_URL` in `.env.production` to your deployed gateway URL
- Update API Gateway CORS to include your production frontend URL

---

## Daily Workflow

After initial setup, your daily startup is:

1. Open IntelliJ
2. Run the 5 backend services in order (Discovery → User → Ride → Payment → Gateway)
3. Open terminal in `carpool-frontend/` → `npm run dev`
4. Visit http://localhost:3000

That's it. Happy carpooling! 🚗
