# PriceWatchdog API

> A Spring Boot-powered price tracking and notification service that monitors product prices and alerts users when
> target prices are reached.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [How It Works](#how-it-works)
- [Key Components](#key-components)
- [Message Queue Flow](#message-queue-flow)
- [Database Schema](#database-schema)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**PriceWatchdog API** is a backend service that allows users to register products they want to track. The system
periodically scrapes product prices from the web, stores the price history, and sends email notifications when prices
drop below a defined threshold.

**Main Features:**

- Automated product price scraping
- Price history tracking and storage
- Email alerts on price drops
- Scheduled background price checks
- Asynchronous processing via RabbitMQ

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client / Frontend                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP REST
┌──────────────────────────▼──────────────────────────────────────┐
│                    ProductController                             │
│                  (REST API Layer)                                │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                     ProductService                               │
│                  (Business Logic Layer)                          │
└────────────┬─────────────────────────────┬───────────────────────┘
             │                             │
┌────────────▼──────────┐    ┌─────────────▼──────────────────────┐
│  PriceUpdateScheduler │    │         RabbitMQ                   │
│  (Scheduled Tasks)    │───►│    (Message Queue)                 │
└───────────────────────┘    └─────────────┬──────────────────────┘
                                           │
                             ┌─────────────▼──────────────────────┐
                             │       PriceCheckWorker             │
                             │    (Async Message Consumer)        │
                             └──────┬──────────────┬──────────────┘
                                    │              │
                     ┌──────────────▼───┐  ┌───────▼──────────────┐
                     │ PriceScraper     │  │   EmailService       │
                     │ Service          │  │ (Notifications)      │
                     └──────────────────┘  └──────────────────────┘
                                    │
                     ┌──────────────▼──────────────────────────────┐
                     │              Database (JPA)                  │
                     │   Product Table │ PriceHistory Table         │
                     └─────────────────────────────────────────────┘
```

---

## Project Structure

```
src/
└── main/
    ├── java/
    │   └── com/meminksr/pricewatchdogapi/
    │       ├── config/
    │       │   └── RabbitMQConfig.java          # RabbitMQ queues, exchanges & bindings
    │       ├── controller/
    │       │   └── ProductController.java        # REST endpoints
    │       ├── entity/
    │       │   ├── Product.java                  # Product JPA entity
    │       │   └── PriceHistory.java             # Price history JPA entity
    │       ├── repository/
    │       │   ├── ProductRepository.java        # Product data access
    │       │   └── PriceHistoryRepository.java   # Price history data access
    │       ├── service/
    │       │   ├── ProductService.java           # Core product business logic
    │       │   ├── PriceScraperService.java      # Web scraping logic
    │       │   ├── PriceCheckWorker.java         # RabbitMQ message consumer
    │       │   ├── PriceUpdateScheduler.java     # Cron-based scheduler
    │       │   └── EmailService.java             # Email notification sender
    │       └── PriceWatchdogApiApplication.java  # Application entry point
    └── resources/
        ├── static/                               # Static assets
        ├── templates/                            # Email / view templates
        └── application.properties               # App configuration
```

---

## ️ Tech Stack

| Layer                  | Technology                           |
|------------------------|--------------------------------------|
| Framework              | Spring Boot 3.x                      |
| Language               | Java 17+                             |
| ORM                    | Spring Data JPA / Hibernate          |
| Message Broker         | RabbitMQ (Spring AMQP)               |
| Email                  | Spring Mail (JavaMailSender)         |
| Scheduling             | Spring `@Scheduled`                  |
| Web Scraping           | Jsoup (or similar)                   |
| Database (Development) | H2 In-Memory (default)               |
| Database (Production)  | PostgreSQL / MySQL (user-configured) |
| Build Tool             | Maven                                |
| API Testing            | HTTP Client (test.http)              |

---

## Prerequisites

Before running this project, make sure you have the following installed:

- **Java 17+**
- **Maven 3.8+**
- **RabbitMQ** (running locally or via Docker)
- An **SMTP email account** (Gmail, etc.)
- **PostgreSQL / MySQL** *(only required for production — H2 is used automatically in development)*

**Quick start with Docker (RabbitMQ):**

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

Access RabbitMQ dashboard at: `http://localhost:15672` (guest / guest)

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/meminksr/price-watchdog-api.git
cd price-watchdog-api
```

### 2. Configure Application Properties

Copy the template and fill in your own values:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Edit `application.properties` (see [Configuration](#configuration) section below).

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the built JAR directly:

```bash
java -jar target/pricewatchdog-api-*.jar
```

The API will be available at `http://localhost:8080`.

---

## Configuration

### Development (Default — H2)

The project works out of the box with an **H2 in-memory database**. No extra setup required — tables are created
automatically when the application starts.

Default `src/main/resources/application.properties`:

```properties
# ── Server ──────────────────────────────────────────
server.port=8080
# ── Database (H2 — Development) ─────────────────────
spring.datasource.url=jdbc:h2:mem:pricewatchdog
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
# Access H2 console at: http://localhost:8080/h2-console
# ── RabbitMQ ─────────────────────────────────────────
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
# ── Email (SMTP) ─────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
# ── Price Check Scheduler ────────────────────────────
price.check.cron=0 0 * * * *
```

> **What is H2?** H2 is a lightweight embedded in-memory database that runs inside the JVM. It's ideal for
> development and testing, but all data is lost when the application stops. Use a persistent database for production.

---

### 🚀 Production — Add Your Own Database

If you're deploying the app or need persistent data, **remove the H2 configuration** and replace it with one of the
options below.

#### PostgreSQL

**1. Add the dependency to `pom.xml`:**

```xml

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**2. Update `application.properties`:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pricewatchdog
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

#### MySQL

**1. Add the dependency to `pom.xml`:**

```xml

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**2. Update `application.properties`:**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pricewatchdog?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

#### Quick PostgreSQL via Docker

Don't want to install PostgreSQL locally? Spin it up in seconds with Docker:

```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=pricewatchdog \
  -e POSTGRES_USER=your_db_user \
  -e POSTGRES_PASSWORD=your_db_password \
  -p 5432:5432 \
  postgres:15
```

---

> **Security Note:** Never commit credentials to version control. Use environment variables or a secrets manager in
> production.
>
> ```properties
> # Example: using environment variables
> spring.datasource.password=${DB_PASSWORD}
> spring.mail.password=${MAIL_PASSWORD}
> ```

---

## API Endpoints

### Base URL: `http://localhost:8080/api`

#### Products

| Method   | Endpoint         | Description                           |
|----------|------------------|---------------------------------------|
| `GET`    | `/products`      | List all tracked products             |
| `GET`    | `/products/{id}` | Get a single product by ID            |
| `POST`   | `/products`      | Add a new product to track            |
| `PUT`    | `/products/{id}` | Update product details / target price |
| `DELETE` | `/products/{id}` | Remove a product from tracking        |

#### Price History

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| `GET`  | `/products/{id}/price-history`        | Get full price history for a product |
| `GET`  | `/products/{id}/price-history/latest` | Get the most recent price entry      |

---

### Example Requests

**Add a product to track:**

```http
POST /api/products
Content-Type: application/json

{
  "name": "Sony WH-1000XM5",
  "url": "https://example.com/product/sony-wh1000xm5",
  "targetPrice": 250.00,
  "notificationEmail": "user@example.com"
}
```

**Response:**

```json
{
  "id": 1,
  "name": "Sony WH-1000XM5",
  "url": "https://example.com/product/sony-wh1000xm5",
  "currentPrice": 299.99,
  "targetPrice": 250.00,
  "notificationEmail": "user@example.com",
  "createdAt": "2025-05-13T10:00:00"
}
```

**Get price history:**

```http
GET /api/products/1/price-history
```

```json
[
  {
    "price": 320.00,
    "checkedAt": "2025-05-10T08:00:00"
  },
  {
    "price": 299.99,
    "checkedAt": "2025-05-11T08:00:00"
  },
  {
    "price": 280.00,
    "checkedAt": "2025-05-12T08:00:00"
  }
]
```

---

## How It Works

```
1. User registers a product URL with a target price
         │
         ▼
2. PriceUpdateScheduler triggers on schedule (e.g. every hour)
         │
         ▼
3. Scheduler publishes a price-check message to RabbitMQ
         │
         ▼
4. PriceCheckWorker consumes the message asynchronously
         │
         ▼
5. PriceScraperService fetches & parses the product page
         │
         ▼
6. New price is saved to PriceHistory table
         │
         ├── [Price ≤ Target] ──► EmailService sends alert to user 
         │
         └── [Price > Target] ──► No action, wait for next cycle
```

---

## Key Components

### `RabbitMQConfig`

Declares the exchange, queue, and binding used by the price-check workflow. Enables asynchronous, decoupled
communication between the scheduler and worker.

### `ProductController`

Exposes REST endpoints for CRUD operations on tracked products and their price histories. Delegates business logic to
`ProductService`.

### `ProductService`

Orchestrates product registration, update, and deletion. Coordinates with repositories for persistence.

### `PriceScraperService`

Fetches the HTML of a product page and extracts the current price using CSS selectors or XPath. Handles retries and HTTP
errors gracefully.

### `PriceUpdateScheduler`

A `@Scheduled` component that runs at a configured interval and publishes a job message to RabbitMQ for every active
tracked product.

### `PriceCheckWorker`

A RabbitMQ message listener (`@RabbitListener`) that consumes price-check jobs. Calls the scraper, persists the result,
and triggers email notifications when appropriate.

### `EmailService`

Sends HTML-formatted notification emails via JavaMailSender when a product's price falls at or below the user's target
price.

---

## Message Queue Flow

```
[PriceUpdateScheduler]
        │
        │  publish: { productId: 42 }
        ▼
[RabbitMQ Exchange: price.check.exchange]
        │
        │  routing key: price.check
        ▼
[Queue: price.check.queue]
        │
        ▼
[PriceCheckWorker] ──► scrape ──► save ──► notify (if needed)
```

Exchange Type: `Direct`
Queue: `price.check.queue`
Routing Key: `price.check`

---

## Database Schema

### `product` table

| Column               | Type        | Description                  |
|----------------------|-------------|------------------------------|
| `id`                 | BIGINT (PK) | Auto-generated ID            |
| `name`               | VARCHAR     | Product display name         |
| `url`                | TEXT        | Product page URL             |
| `current_price`      | DECIMAL     | Last scraped price           |
| `target_price`       | DECIMAL     | User-defined alert threshold |
| `notification_email` | VARCHAR     | Recipient for alerts         |
| `created_at`         | TIMESTAMP   | Registration time            |
| `updated_at`         | TIMESTAMP   | Last update time             |

### `price_history` table

| Column       | Type        | Description             |
|--------------|-------------|-------------------------|
| `id`         | BIGINT (PK) | Auto-generated ID       |
| `product_id` | BIGINT (FK) | Reference to `product`  |
| `price`      | DECIMAL     | Scraped price value     |
| `checked_at` | TIMESTAMP   | When price was recorded |

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to your branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

Please follow the existing code style and write unit tests for new functionality.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ☕ and Spring Boot by <a href="https://github.com/meminksr">meminksr</a>
</p>