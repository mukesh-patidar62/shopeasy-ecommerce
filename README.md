# ShopEasy — E-commerce Website (Spring Boot)

A working e-commerce site: customers browse and buy, admins add/edit/delete
products from any device (including their phone), and payments go through
Razorpay. Customer and order data is stored in a real database.

## What's included
- **Customer side:** browse products, register/login, cart, checkout with
  delivery details, Razorpay payment, order history.
- **Admin side:** mobile-friendly dashboard to add/edit/delete products
  (with photo upload — opens your phone's camera directly), view all orders.
- **Payments:** Razorpay Checkout.js integration, with signature
  verification and payment records stored in the database.
- **Database:** runs on H2 (in-memory, zero setup) by default; MySQL config
  is ready to uncomment for real persistent storage.

## Quick start (fastest way to see it working)

1. Install **Java 17+** and **Maven** if you don't have them.
2. Open a terminal in this folder and run:
   ```
   mvn spring-boot:run
   ```
3. Open **http://localhost:8080** in your browser (or on your phone if it's
   on the same Wi-Fi — use your computer's local IP instead of localhost).
4. Log in as admin with:
   - Email: `admin@shop.com`
   - Password: `admin123`
   (This account is created automatically on first run — see `DataInitializer.java`.)
5. Go to **Admin Dashboard → + Add Product** to add your first product.
6. Log out, register a new customer account, and try buying something.

By default this uses an **H2 in-memory database** — great for testing, but
all data is wiped every time you restart the app.

## Switching to MySQL (real, persistent storage)

1. Install MySQL and make sure it's running.
2. Open `src/main/resources/application.properties`.
3. Comment out the **H2** block and uncomment the **MySQL** block, then set
   your MySQL username/password.
4. Restart the app — Spring Boot will auto-create the `ecommerce_db`
   database and all the tables for you.

## Setting up real Razorpay payments

The app ships with placeholder test keys that won't actually work. To get
real ones (free, takes 2 minutes):

1. Sign up at https://dashboard.razorpay.com
2. Go to **Settings → API Keys → Generate Test Key**
3. Copy the **Key Id** and **Key Secret**
4. Paste them into `application.properties`:
   ```
   razorpay.key.id=rzp_test_xxxxxxxxxxxx
   razorpay.key.secret=xxxxxxxxxxxxxxxxxxxx
   ```
5. Restart the app. You can now pay with Razorpay's test cards (e.g. card
   number `4111 1111 1111 1111`, any future expiry, any CVV) — see
   https://razorpay.com/docs/payments/payments/test-card-upi-details/
   for the full list of test payment methods.

When you're ready to accept real payments, complete Razorpay's KYC/activation
process and swap the test keys for live keys (`rzp_live_...`).

## Adding products from your phone

Log in as admin on your phone's browser, go to **Admin Dashboard**, tap the
floating **+** button, fill in the details, and tap the photo field — it
opens your camera directly so you can snap a product photo on the spot.

## Project structure

```
src/main/java/com/ecommerce/
  config/       Security config, default admin creation, upload folder wiring
  entity/       User, Product, Orders, OrderItem, Payment
  repository/   Spring Data JPA repositories
  service/      Business logic (users, products, orders, Razorpay payments)
  controller/   Web routes (home, auth, admin, cart, checkout, payment, orders)
src/main/resources/
  templates/    Thymeleaf pages (Bootstrap 5, mobile-responsive)
  application.properties   Database + Razorpay configuration
```

## Notes on the database design
- `users` — stores every registered customer and admin, with a `role`
  column (`ADMIN` / `CUSTOMER`) controlling what they can access.
- `products` — each has a `created_by` link back to the admin who added it,
  so if you ever add more admins you can see who added what.
- `orders` / `order_items` — one order per checkout, with line items
  snapshotting the price at time of purchase (so later price changes don't
  rewrite history).
- `payments` — one row per Razorpay transaction attempt, keeping the
  order ID, payment ID, and signature for your records.

## Known limitations / things to harden before going fully live
- Add server-side validation messages (currently minimal) and a proper
  `@ControllerAdvice` for error pages.
- Add pagination once your product catalog grows beyond a page or two.
- Consider adding a webhook endpoint (in addition to the client-side
  verification already included) so payments still get recorded even if a
  customer closes the browser right after paying — see
  https://razorpay.com/docs/webhooks/
- Move the default admin password out of code before any real deployment.
