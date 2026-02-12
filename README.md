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
