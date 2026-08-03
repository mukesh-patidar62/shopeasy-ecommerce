# 🛍️ ShopEasy — Full-Stack E-Commerce Platform

A complete e-commerce web application built from scratch with Spring Boot — featuring role-based storefronts, real payment processing, cloud-hosted media, and AI-powered shopping tools.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Aiven%20Cloud-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Render](https://img.shields.io/badge/Deployed%20on-Render-46E3B7?logo=render&logoColor=white)
![License](https://img.shields.io/badge/License-Educational-lightgrey)

---

## 🔗 Live Demo

**[shopeasy-ecommerce.onrender.com](https://shopeasy-ecommerce-hgpl.onrender.com)**
*(Free-tier hosting — first load may take 30–60s if the server has been idle)*

---

## ✨ Overview

ShopEasy is a two-sided marketplace platform: **customers** browse a live product catalog, add items to cart, check out, and pay securely — while **admins** manage the entire product catalog from a mobile-first dashboard, including snapping product photos directly from their phone's camera. Every part of the stack — auth, payments, image storage, AI features, and deployment — was built, debugged, and deployed as a real production system rather than a local-only demo.

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Security** | Spring Security 6 — BCrypt password hashing, role-based access control, CSRF protection |
| **Persistence** | Spring Data JPA (Hibernate) |
| **Database** | MySQL, hosted on **Aiven** (free managed cloud DB) — H2 in-memory fallback for local dev |
| **Frontend** | Thymeleaf server-side templates + Bootstrap 5 |
| **Build Tool** | Maven |
| **Payments** | Razorpay Java SDK + Checkout.js, with server-side signature verification |
| **Image Hosting** | Cloudinary (persistent cloud storage for product photos) |
| **AI** | Google Gemini API — AI-generated product descriptions + AI shopping assistant chatbot |
| **Containerization** | Docker (multi-stage build: Maven build stage → lightweight JRE runtime stage) |
| **Hosting** | Render (free-tier web service) |
| **Version Control** | Git + GitHub |

---

## 🎯 Features

### 🛒 Customer Experience
- Browse a live product catalog with category filtering and a rotating hero banner
- Secure registration and login (BCrypt-hashed passwords)
- Shopping cart with quantity management (session-based)
- Checkout flow collecting real delivery details
- **Razorpay payment integration** — real test-mode transactions with signature verification before an order is marked paid
- Order history with live status tracking (Pending → Paid)
- **AI shopping assistant chatbot** — answers product questions using the store's actual live inventory as context (never invents products or prices)

### 🛠️ Admin Experience
- Mobile-responsive dashboard — add, edit, and delete products from any device
- **Direct camera capture** for product photos on mobile (opens the phone's camera, not just a file picker)
- **AI-generated product descriptions** — Gemini writes a description from just a product name + category, saving manual writing time
- Real discount pricing (optional "original price" field — no fabricated fake discounts)
- View and manage all customer orders in one place
- Role-based access control separates admin and customer capabilities cleanly

### 🔒 Security & Engineering Practices
- Passwords hashed with BCrypt — never stored in plaintext
- All secrets (database credentials, Razorpay keys, Gemini API key, admin credentials) pulled from **environment variables**, never hardcoded or committed to version control
- CSRF protection enabled sitewide, with scoped exceptions only for verified API endpoints (payment webhook, AI endpoints)
- Payment verification happens **server-side** — the client never determines whether a payment succeeded

### 🎨 Design
- Custom visual identity (not a default template): deep indigo + marigold/saffron + teal palette, Fraunces serif headlines paired with Inter body text
- A signature rotated "price-tag" UI motif used consistently across the site
- Fully responsive — built mobile-first specifically because the admin workflow depends on phone usability

---

## 🏗️ Architecture

```
com.ecommerce
├── config/       Spring Security config, default admin seeding, static resource handling
├── entity/       User, Product, Orders, OrderItem, Payment (JPA entities)
├── repository/   Spring Data JPA repositories
├── service/      Business logic — users, products, orders, payments, Cloudinary uploads, Gemini AI
└── controller/   Web + REST controllers (storefront, admin, cart, checkout, payment, chat)

resources/
├── templates/    Thymeleaf views (Bootstrap 5, mobile-responsive)
└── static/       Custom CSS design system
```

**Request flow example (checkout → payment):**
`Cart (session)` → `Checkout (delivery details)` → `Order created (PENDING)` → `Razorpay order created server-side` → `Checkout.js opens on client` → `Payment completes` → `Signature verified server-side` → `Order marked PAID + stock reduced`

---

## 🗄️ Database Schema

| Table | Purpose |
|---|---|
| `users` | Customers and admins, with role-based permissions (`ADMIN` / `CUSTOMER`) |
| `products` | Catalog items — price, stock, category, Cloudinary image URL, optional discount price |
| `orders` / `order_items` | One row per checkout, with line-item price snapshots (price changes later don't rewrite history) |
| `payments` | Razorpay transaction records — order ID, payment ID, signature, status |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL (optional — H2 in-memory works out of the box for local testing)

### Setup

```bash
git clone https://github.com/mukesh-patidar62/shopeasy-ecommerce.git
cd shopeasy-ecommerce/ecommerce-app
```

Set the following environment variables (or rely on H2 + placeholder defaults for a quick local run):

```
SPRING_DATASOURCE_URL, SPRING_DATASOURCE_DRIVER, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET
GEMINI_API_KEY
ADMIN_EMAIL, ADMIN_PASSWORD
```

Run it:

```bash
mvn spring-boot:run
```

Open **http://localhost:8080**.

### Deploying it yourself
The included `Dockerfile` builds a production-ready container in two stages (Maven build → slim JRE runtime), making it deployable as-is to any container-based host (Render, Railway, Fly.io, etc.) — just supply the environment variables above.

---

## 🧪 What This Project Demonstrates

- End-to-end ownership of a production system: coding, debugging real deployment errors (SSL handshakes, CSRF misconfiguration, ephemeral filesystem storage, model deprecation), and shipping to a live URL
- Secure-by-default engineering habits: environment-variable secrets, hashed passwords, server-side payment verification
- Practical, responsible use of AI in a product — generative features that assist (descriptions, chat) without fabricating trust signals (no fake reviews/ratings)
- Full request lifecycle understanding: from a Thymeleaf form submission through Spring MVC, JPA, a third-party payment gateway, and back

---

## 📌 Roadmap

- [ ] Genuine customer review system (ratings tied to verified purchases)
- [ ] Razorpay webhook handling for payment reliability beyond client-side confirmation
- [ ] Product search and pagination
- [ ] Admin analytics dashboard

---

## 📄 License

Built for educational and portfolio purposes.     
