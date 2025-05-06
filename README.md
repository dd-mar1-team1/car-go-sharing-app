# Car-Go: Car Sharing Service
___

## Overview
Car-Go is a next-gen car sharing platform designed to make urban transportation smarter, greener, and more accessible. Whether you're a city explorer, a weekend adventurer, or an operations manager—Car-Go empowers you to rent, manage, and pay for vehicles with just a few clicks.

Built using modern Java and Spring Boot, Car-Go combines the power of Stripe payments, Telegram notifications, and AWS deployment into a sleek and scalable web application that can serve real-world demands—from casual users to professional fleet operators.

💡 __Why Car-Go?__
+ 🚘 Rent cars instantly with real-time availability
+ 🧑‍💼 Manage your rentals and payments in one place
+ 🔒 Stay secure with JWT authentication and role-based access
+ 📲 Get updates directly via Telegram
+ 💳 Pay easily through Stripe, with fines calculated automatically
+ ☁️ Deployed on AWS, ready to scale

## Features

### ✅ Functional Features

+ __User Authentication & Authorization__(JWT-based)
+ __Car Inventory Management__(CRUD)
+ __Rental Management__ with active/returned status
+ __Payment Integration via Stripe__ (session-based)
+ __Telegram Bot Notifications:__
  + New rental created
  + Overdue rental reminders
  + Payment confirmations

### 🛠️ Admin Features

+ Full access to __car__, __user__, __rental__, and payment management
+ Role assignment and management
+ Filtering rentals by users, status, and activity

### 📦 Tech Stack

__Backend__:  Java 21, Spring Boot 3, Spring Security, Spring Data JPA

__Database__: MySQL (RDS), Liquibase

__Payment__: Stripe API (Test Mode)

__Notifications__: Telegram Bot API

__Auth__: JWT (Access + Refresh Tokens)

__Build & Tools__: Maven, Docker, Docker Compose, Checkstyle

__CI/CD__: GitHub Actions

__Documentation__: Swagger (OpenAPI 3)

## API Endpoints

### Authentication
+ __Login:__ ```POST /auth/login```
+ __Register:__ ```POST /auth/register```

### Users Controller
+ __Update user role__: ```PUT: /users/{id}/role```
+ __Get my profile info__: ```GET: /users/me```
+ __update profile info__: ```PUT/PATCH: /users/me```

### Cars Controller
+ __Add a new car__: ```POST: /cars```
+ __Get a list of cars__: ```GET: /cars```
+ __Get car's detailed information__: ```GET: /cars/```
+ __Update car (also manage inventory)__: ```PUT/PATCH: /cars/```
+ __Delete car__: ```DELETE: /cars/```

### Rentals Controller
+ __Add a new rental__: ```POST: /rentals```
+ __Get rentals by user ID and whether the rental is still active or not__: ```GET: /rentals/?user_id=...&is_active=...```
+ __Get specific rental__: ```GET: /rentals/ ```
+ __Get actual return date__: ```POST: /rentals//return```

### Payments Controller (Stripe)
+ __get payments__: ```GET: /payments/?user_id=...```
+ __create payment session__: ```POST: /payments/```
+ __check successful Stripe payments__: ```GET: /payments/success/ ```
+ __return payment paused message__: ```GET: /payments/cancel/```

### Notifications Service (Telegram)
+ Notifications about new rentals created, overdue rentals, and successful payments
+ Other services interact with it to send notifications to car sharing service administrators
+ Uses Telegram API, Telegram Chats, and Bots

## 🧩 Architecture

+ Microservice-oriented monolith
+ REST API layer + Service layer + Repository layer
+ Environment-based configuration via .env files
+ Scheduled jobs for overdue rental checks
+ External integrations: Stripe & Telegram

## 🧪 Tests & Quality
+ 70%+ code coverage (JUnit & MockMvc)
+ Maven Checkstyle Plugin included
+ Meaningful commit messages and PR-based development

## 🔐 Security
+ Secure password hashing with BCrypt
+ JWT tokens with refresh mechanism
+ Role-based access control

## 📅 Future Improvements

+ Expired Stripe session checker
+ Session renewal endpoint
+ Email notifications

## ⚙️ Technologies & Versions

- __Java: 21__
- __Spring Boot: 3.4.4__
- __Spring Context: 6.2.1__
- __MySQL Connector: 9.1.0__
- __JUnit: 4.13.2__
- __MapStruct: 1.6.3__
- __JWT: 0.12.6__
- __Testcontainers: 1.20.4__
- __Maven Compiler Plugin: 3.11.0__
- __Maven Checkstyle Plugin: 3.3.0__
- __Docker: 27.5.1__
- __Telegram Bots Java SDK: 6.9.7.0__
- __Stripe Java SDK: 29.0.0__

### Step 1: Clone the Repository

```html
git clone https://github.com/your-repo/car-go-sharing-app.git
cd car-go-sharing-app
```

### Step 2: Configure Database
If using Docker, you can run MySQL using the provided ```docker-compose.yml``` file:

```docker-compose up -d```

Make sure the database container is up and running.

Also, if you are using a local MySQL instance, make sure it matches the configuration specified in the file: ```application.properites```

- Create a database named books using your MySQL client:

```sql
CREATE DATABASE cargo;
```

### Step 3: Build the Project

Use Maven to build the application:
```
mvn clean install
```
### Step 4: Run the Application

Start the application using the following command:

```
mvn spring-boot:run
```

The application will be accessible at:

- __API:__ ```http://localhost:8080```
- __Swagger UI:__ ```http://localhost:8080/swagger-ui.html```

### Step 3: Test the Application

You can use tools like Postman to test the API endpoints.
Additionally, Swagger UI provides an interactive way to test all endpoints.

