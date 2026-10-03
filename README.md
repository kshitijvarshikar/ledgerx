# LedgerX

LedgerX is a Spring Boot-based financial transaction platform designed to demonstrate secure account management, transactional money transfers, concurrency control, idempotency, transaction history, audit logging, ledger entries, asynchronous processing, and reporting.

## Key Features

- User registration and authentication
- JWT-based authentication and authorization
- Account creation and management
- Secure money transfers
- Database transactions with atomic balance updates
- Pessimistic locking for concurrent transfers
- Idempotency protection against duplicate transfers
- Double-entry style ledger records
- Transaction audit logging
- Beneficiary management
- Paginated transaction history
- Transaction filtering by status and date range
- JDBC-based transaction reporting
- Asynchronous post-transaction notification processing
- Global exception handling and validation
- OpenAPI / Swagger API documentation
- Actuator health monitoring
- Unit and integration testing

## Architecture

LedgerX follows a modular monolith architecture built with Spring Boot.

The application separates responsibilities into controllers, services, repositories, entities, DTOs, security components, events, and configuration.

The architecture diagram is provided below.

## Core Transaction Flow

```text
Client
  ↓
REST API
  ↓
JWT Authentication
  ↓
Request Validation
  ↓
Idempotency Check
  ↓
Account Ownership & Status Validation
  ↓
Pessimistic Account Lock
  ↓
Balance Validation
  ↓
Debit Sender
  ↓
Credit Receiver
  ↓
Transaction Record
  ↓
Ledger Entries
  ↓
Audit Log
  ↓
Database Commit
  ↓
Asynchronous Event Processing
```

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- JDBC
- REST APIs

### Database

- MySQL

### Security

- Spring Security
- JWT
- BCrypt password hashing

### Testing

- JUnit
- Mockito
- Spring Boot Integration Testing

### API Documentation & Monitoring

- OpenAPI
- Swagger UI
- Spring Boot Actuator

### Build & Version Control

- Maven
- Git
- GitHub

## Security

LedgerX uses Spring Security with stateless JWT authentication.

Passwords are stored using BCrypt hashing, while authenticated API requests are protected through JWT bearer tokens.

Sensitive configuration values such as the database password and JWT secret are provided through environment variables rather than being stored directly in source code.

## Concurrency Control

Money transfers use database-level pessimistic locking when retrieving accounts for update.

This prevents concurrent transactions from modifying the same account balance without proper synchronization.

The transfer operation is executed within a database transaction so that balance updates and transaction records remain atomic.

## Idempotency

Transfers require an idempotency key.

If the same idempotency key is submitted again, LedgerX detects the existing transaction instead of processing the transfer a second time.

This protects against duplicate financial operations caused by retries or repeated client requests.

## Ledger & Audit

Each successful transfer produces:

- Transaction record
- Debit ledger entry
- Credit ledger entry
- Audit record

This provides a traceable history of financial operations.

## Asynchronous Processing

After a successful transaction commits, LedgerX publishes a transaction-completed event.

The event is processed asynchronously using a dedicated task executor.

This keeps post-transaction processing separate from the main transfer request.

## Reporting

LedgerX uses JDBC for selected reporting operations where direct SQL aggregation is appropriate.

The reporting layer supports:

- Total amount transferred
- Transaction counts
- Successful transactions
- Failed transactions
- Date-range summaries

## Transaction History

Authenticated users can retrieve their transaction history with:

- Pagination
- Status filtering
- Date-range filtering
- Newest-first ordering

Repository queries use entity graphs where required to avoid lazy-loading problems when transaction account information is mapped into API responses.

## API Documentation

Swagger UI is available locally at:

`http://localhost:8080/swagger-ui/index.html`

The OpenAPI configuration documents the LedgerX REST API and supports Bearer JWT authentication.

## Health Monitoring

Spring Boot Actuator provides application health information.

Health endpoint:

`http://localhost:8080/actuator/health`

## Project Structure

```text
src/main/java/com/ledgerx
├── config
├── controller
├── dto
├── entity
├── event
├── exception
├── repository
├── security
└── service
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `controller` | REST API endpoints |
| `service` | Business logic |
| `repository` | Database access |
| `entity` | JPA entities |
| `dto` | API request/response models |
| `security` | JWT authentication and security components |
| `event` | Transaction events and asynchronous processing |
| `exception` | Global exception handling |
| `config` | Security, async, and OpenAPI configuration |

## Running Locally

### Prerequisites

- Java 21+
- MySQL
- Maven Wrapper

### Database

Create the MySQL database:

```sql
CREATE DATABASE ledgerx;
```

### Environment Variables

Set:

```text
DB_PASSWORD=<your-mysql-password>
JWT_SECRET=<your-jwt-secret>
```

Do not commit these values to Git.

### Run the Application

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### Run Tests

```powershell
.\mvnw.cmd clean test
```

## Testing

The project includes unit, controller, security, and integration tests covering authentication, account operations, transfers, idempotency, concurrency-related behavior, beneficiaries, reporting, ledger/audit persistence, and transaction history.

## API Overview

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

### Accounts

```text
POST /api/accounts
GET  /api/accounts
```

### Transactions

```text
POST /api/transactions/transfer
GET  /api/transactions
```

### Beneficiaries

```text
POST   /api/beneficiaries
GET    /api/beneficiaries
DELETE /api/beneficiaries/{id}
```

### Reports

```text
GET /api/reports/total-transferred
GET /api/reports/summary
GET /api/reports/summary/date-range
```

### Monitoring

```text
GET /actuator/health
```

## Project Status

LedgerX currently provides a working backend implementation with authentication, transactional transfers, idempotency, concurrency control, ledger and audit persistence, asynchronous event processing, reporting, API documentation, monitoring, and automated tests.
