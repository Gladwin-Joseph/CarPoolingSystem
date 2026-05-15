# 🚗Car Pooling System

A full-stack carpooling platform connecting drivers and passengers to reduce traffic congestion, lower travel costs, and promote sustainable transportation.

---

## 📋 Quick Overview

This Car Pooling System enables drivers to post ride offers and passengers to search and book rides on shared routes. The system prioritizes simplicity, security, and extensibility through proper software architecture and design patterns.

### Key Features

- **User Registration & Verification** — Secure registration for drivers and passengers
- **Ride Management** — Host drivers can post rides with route, time, and seat details
- **Ride Search & Booking** — Passengers can search and book available rides
- **Ratings & Reviews** — Users can rate each other after completing rides
- **Payment Integration** — Digital payment support and receipts
- **Notifications** — Push notifications for booking confirmations and updates

---

## How to Wake Up the Application

Follow these steps in order. Each link opens a backend service. You should wait until the page loads (not just shows a "loading" state) before moving to the next one.

### Step 1: Wake up the Discovery Server

Open this link in a new tab:

[https://carpoolingsystem-discovery-server.onrender.com](https://carpoolingsystem-discovery-server.onrender.com)

You should see the **Eureka dashboard**, which is a page that lists registered services. When the page finishes loading and you can see the heading "System Status", the Discovery Server is awake.

**Why first:** The other services register themselves with the Discovery Server when they start. If this service is not up first, the other services have nowhere to register.

**Expected wait time:** 60 to 90 seconds on the first visit.

### Step 2: Wake up the User Service

Open this link in a new tab:

[https://carpoolingsystem-user-service.onrender.com](https://carpoolingsystem-user-service.onrender.com)

You may see a blank page or a "Whitelabel Error Page" message. That is fine. As long as the page loads (even with an error), the service is awake. The service does not have a public homepage; it only responds to specific API endpoints.

**Expected wait time:** 60 to 90 seconds on the first visit.

### Step 3: Wake up the Ride Service

Open this link in a new tab:

[https://carpoolingsystem-ride-service.onrender.com](https://carpoolingsystem-ride-service.onrender.com)

Same as the User Service, you may see a blank page or an error page. That is expected. The service is awake once the page loads.

**Expected wait time:** 60 to 90 seconds on the first visit.

### Step 4: Wake up the Payment Service

Open this link in a new tab:

[https://carpoolingsystem-payment-service.onrender.com](https://carpoolingsystem-payment-service.onrender.com)

Same again, a blank page or error page means the service is awake.

**Expected wait time:** 60 to 90 seconds on the first visit.

### Step 5: Wake up the API Gateway

Open this link in a new tab:

[https://carpoolingsystem-api-gateway.onrender.com](https://carpoolingsystem-api-gateway.onrender.com)

This is the entry point that the React frontend talks to. Once it loads, all backend services are ready to handle requests.

**Expected wait time:** 60 to 90 seconds on the first visit.

### Step 6: Open the React App

Now you can open the actual application:

[https://carpoolingsystem.onrender.com](https://carpoolingsystem.onrender.com)

If the React app is also asleep, wait around 30 seconds for it to load. The frontend should now be fully connected to the backend and you can use the app normally.

---

## Quick Wake-Up Checklist

Open these links one by one, in this order, before opening the React app:

1. [Discovery Server](https://carpoolingsystem-discovery-server.onrender.com)
2. [User Service](https://carpoolingsystem-user-service.onrender.com)
3. [Ride Service](https://carpoolingsystem-ride-service.onrender.com)
4. [Payment Service](https://carpoolingsystem-payment-service.onrender.com)
5. [API Gateway](https://carpoolingsystem-api-gateway.onrender.com)
6. [React App](https://carpoolingsystem.onrender.com)

---

## What to Test

Once the app is awake, here are some things you can try:

### As a passenger
1. Click **Register** and create a passenger account (any test email works, for example `passenger@test.com`).
2. Log in.
3. Search for rides between Irish cities.
4. Book a seat on a ride.
5. Pay using the Stripe test card: `4242 4242 4242 4242`, any future expiry date (for example 12/30), any 3-digit CVC, any postcode.
6. Cancel the booking and observe the refund flow.

### As a driver
1. Register a new account with the role set to **Driver**.
2. Log in.
3. Publish a ride with a source city, destination city, departure time, price, and seat count.
4. The ride should appear in passenger search results immediately.

### Stripe test cards

For testing card payments, use the following Stripe test cards:

| Scenario | Card number |
|----------|-------------|
| Successful payment | 4242 4242 4242 4242 |
| Card declined | 4000 0000 0000 0002 |
| Insufficient funds | 4000 0000 0000 9995 |

Use any future expiry date, any 3-digit CVC, and any postcode.

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Frontend** | React.js|
| **Backend** | Java, Spring Boot |
| **Database** | PostgreSQL |
| **Server** | Embedded Tomcat |
| **Architecture** | MVC + Microservices |
| **Version Control** | GitHub |
| **Collaboration & Documentation** | JIRA & Confluence |
---

## 🏗️ Architecture Overview

### Two-Layer Architecture

**MVC Pattern** — Separates concerns at the application level (Model, View, Controller)

**Microservices** — Three independent services for scalability and extensibility:

| Service | Responsibility |
|---------|---------------|
| **User Service** | Registration, authentication, profile management |
| **Ride Service** | Ride posting, searching, booking, matching |
| **Payment Service** | Transaction processing, billing, receipts |

**React.js Frontend** — A web app application that communicates with the backend microservices via RESTful APIs, providing a responsive and dynamic user experience.

**API Gateway** — Routes all client requests to appropriate microservices, centralizing authentication and security.

The system follows a microservices architecture with five backend services and a React frontend.

```
┌──────────────────┐
│  React Frontend  │
└────────┬─────────┘
         │ HTTPS
         ▼
┌──────────────────┐
│   API Gateway    │ (Spring Cloud Gateway)
└────────┬─────────┘
         │ load-balanced lookup
         ▼
┌──────────────────┐
│ Discovery Server │ (Eureka)
└────────┬─────────┘
         │ resolves
         ▼
┌────────────┬────────────┬────────────────┐
│ User       │ Ride       │ Payment        │
│ Service    │ Service    │ Service        │
└────┬───────┴────┬───────┴────┬───────────┘
     │            │            │
     ▼            ▼            ▼
┌────────────────────────────────────┐
│      Supabase PostgreSQL DB        │
└────────────────────────────────────┘
                                │
                                ▼
                          ┌──────────┐
                          │  Stripe  │
                          └──────────┘
```

---

## 🔒 Security & Quality

- **Authentication** — Secure user verification via email/phone
- **Authorization** — Role-based access control
- **Data Validation** — Input validation at controller level
- **Extensibility** — Modular design allows easy feature additions
- **Maintainability** — Clear separation of concerns

---

## 📋 Non-Functional Requirements

- **Scalability** — Handles growing users and concurrent requests
- **Availability** — Redundancy ensures minimal downtime
- **Extensibility** — Modular architecture for new features
- **Serviceability** — Simplified debugging and maintenance

---

## 👥 Team

**Module Name:** CS6652 — Advanced Software Architectures

| Name | Student ID | Role |
|------|-----------|------|
| Chirag Singh | 25009885 | Project Manager & Systems Analyst |
| Ganesh Sudhir Kotalwar | 25142682 | Technical Lead & Tester |
| Gladwin Dominic Joseph | 25040758 | Architect & Designer |
| Kiran Kidecha | 25030965 | Business Analyst & DevOps |

**Instructor:** Professor Salim Saay