# CarPooling-System
# 🚗 RideShare - Car Pooling System

A full-stack carpooling platform connecting drivers and passengers to reduce traffic congestion, lower travel costs, and promote sustainable transportation.

---

## 📋 Quick Overview

RideShare enables drivers to post ride offers and passengers to search and book rides on shared routes. The system prioritizes simplicity, security, and extensibility through proper software architecture and design patterns.

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
| **Frontend** | React.js, JavaScript, HTML5, CSS3 |
| **Backend** | Java, Spring Boot |
| **Database** | PostgreSQL |
| **Server** | Embedded Tomcat |
| **Architecture** | MVC + Microservices |
| **Design Patterns** | Observer, Factory Method, Singleton, Decorator, Strategy, Interceptor, Command |
| **Version Control** | GitHub |

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

**React.js Frontend** — A single-page application that communicates with the backend microservices via RESTful APIs, providing a responsive and dynamic user experience.

**API Gateway** — Routes all client requests to appropriate microservices, centralizing authentication and security.

---

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Node.js 18+ and npm
- PostgreSQL 14 or higher
- Maven 3.6+
- Git

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/chiragrawat12/RideShare.git
cd RideShare

# Configure PostgreSQL connection in application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/rideshare
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# Build and run the backend
mvn clean install
mvn spring-boot:run
```

### Frontend Setup

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The React app will be available at `http://localhost:3000` and the backend API at `http://localhost:8080`.

### Database Setup

```bash
# Create the PostgreSQL database
psql -U postgres
CREATE DATABASE rideshare;
```

Spring Boot will auto-generate the schema via JPA/Hibernate on first run.

---

## 📊 Database Schema

Key entities managed in PostgreSQL:

| Entity | Description |
|--------|-------------|
| **User** | Stores driver and passenger information |
| **HostDriver** | Driver-specific details (license, verification status) |
| **Ride** | Ride offers posted by drivers |
| **Booking** | Passenger bookings for rides |
| **Payment** | Transaction records |
| **Rating** | User ratings and reviews |

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

**Project:** SOLID Four (CS6451 — Advanced Software Design)

| Name | Student ID | Role |
|------|-----------|------|
| Chirag Singh | 25009885 | Project Manager & Systems Analyst |
| Ganesh Sudhir Kotalwar | 25142682 | Technical Lead & Tester |
| Gladwin Dominic Joseph | 25040758 | Architect & Designer |
| Kiran Kidecha | 25030965 | Business Analyst & DevOps |

**Instructor:** Professor J.J Collins

---

## 🚀 Future Enhancements

- Advanced driver-passenger matching algorithm
- Real-time GPS tracking with live map updates
- Referral program and loyalty rewards
- Multiple payment gateway integration
- SOS/Emergency contact system
- Ride prioritization and preferences
- Mobile app (iOS/Android)

---

## 📄 License

This project is a university assignment and is available for educational purposes.

---

## 📧 Contact

For questions or contributions, please reach out through the GitHub repository.

**Repository:** [https://github.com/chiragrawat12/RideShare](https://github.com/chiragrawat12/RideShare)
