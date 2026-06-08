# ExpenseTracker

A full-stack expense tracking application built with Spring Boot and Vanilla JavaScript. Track expenses by category, set monthly budgets, and view spending analytics through an interactive dashboard.

## Tech Stack

- Java 17, Spring Boot 3.2, Spring Security, JWT
- Spring Data JPA, MySQL
- HTML, CSS, Vanilla JavaScript, Chart.js
- JUnit 5, Mockito, MockMvc

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.0+

## Getting Started

```bash
git clone https://github.com/singharchitt/ExpenseTracker.git
cd ExpenseTracker
```

Set your environment variables (see below), then run:

```bash
./mvnw spring-boot:run
```

Open `http://localhost:8080` in your browser. You will be redirected to the login page.

## Environment Variables

Copy `.env.example` to `.env` and fill in your values.

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Random string, minimum 32 characters |
| `JWT_EXPIRATION_MS` | Token expiry in ms, default `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Allowed origins, default `http://localhost:3000` |

Generate a JWT secret:
```bash
# Mac/Linux
openssl rand -base64 32

# Windows PowerShell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

## API Overview

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register |
| POST | `/api/auth/login` | Login |
| GET/POST | `/api/expenses` | List / create expenses |
| PUT/DELETE | `/api/expenses/{id}` | Update / delete expense |
| GET/POST | `/api/budgets` | List / create budgets |
| GET | `/api/analytics/dashboard` | Dashboard summary |
| GET | `/api/analytics/categories` | Category breakdown |
| GET | `/api/analytics/monthly` | Monthly trends |

## Running Tests

```bash
./mvnw test
```
