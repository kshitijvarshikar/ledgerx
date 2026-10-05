LedgerX

LedgerX is a Spring Boot-based financial transaction platform designed to demonstrate secure account management, transactional money transfers, concurrency control, idempotency, transaction history, audit logging, ledger entries, asynchronous processing, and reporting.

Key Features

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

Architecture

LedgerX follows a modular monolith architecture built with Spring Boot.

The application separates responsibilities into controllers, services, repositories, entities, DTOs, security components, events, and configuration.

System Architecture

"LedgerX System Architecture" (images/LedgerX-Architecture.png)

Core Transaction Flow

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

Technology Stack

Backend

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- JDBC
- REST APIs

Database

- MySQL

Security

- Spring Security
- JWT
- BCrypt password hashing

Testing

- JUnit
- Mockito
- Spring Boot Integration Testing

API Documentation & Monitoring

- OpenAPI
- Swagger UI
- Spring Boot Actuator

Build & Version Control

- Maven
- Git
- GitHub

Security

LedgerX uses Spring Security with stateless JWT authentication.

Passwords are stored using BCrypt hashing, while authenticated API requests are protected through JWT bearer tokens.

Sensitive configuration values such as the database password and JWT secret are provided through environment variables rather than being stored directly in source code.

Concurrency Control

Money transfers use database-level pessimistic locking when retrieving accounts for update.

This prevents concurrent transactions from modifying the same account balance without proper synchronization.

The transfer operation is executed within a database transaction so that balance updates and transaction records remain atomic.

Idempotency

Transfers require an idempotency key.

If the same idempotency key is submitted again, LedgerX detects the existing transaction instead of processing the transfer a second time.

This protects against duplicate financial operations caused by retries or repeated client requests.

Ledger & Audit

Each successful transfer produces:

- Transaction record
- Debit ledger entry
- Credit ledger entry
- Audit record

This provides a traceable history of financial operations.

Asynchronous Processing

After a successful transaction commits, LedgerX publishes a transaction-completed event.

The event is processed asynchronously using a dedicated task executor.

This keeps post-transaction processing separate from the main transfer request.

Reporting

LedgerX uses JDBC for selected reporting operations where direct SQL aggregation is appropriate.

The reporting layer supports:

- Total amount transferred
- Transaction counts
- Successful transactions
- Failed transactions
- Date-range summaries

Transaction History

Authenticated users can retrieve their transaction history with:

- Pagination
- Status filtering
- Date-range filtering
- Newest-first ordering

Repository queries use entity graphs where required to avoid lazy-loading problems when transaction account information is mapped into API responses.

API Documentation

Swagger UI is available locally at:

"http://localhost:8080/swagger-ui/index.html"

The OpenAPI configuration documents the LedgerX REST API and supports Bearer JWT authentication.

Health Monitoring

Spring Boot Actuator provides application health information.

Health endpoint:

"http://localhost:8080/actuator/health"

Project Structure

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

Package Responsibilities

Package| Responsibility
"controller"| REST API endpoints
"service"| Business logic
"repository"| Database access
"entity"| JPA entities
"dto"| API request/response models
"security"| JWT authentication and security components
"event"| Transaction events and asynchronous processing
"exception"| Global exception handling
"config"| Security, async, and OpenAPI configuration

Running Locally

Prerequisites

- Java 21+
- MySQL
- Maven Wrapper

Database

Create the MySQL database:

CREATE DATABASE ledgerx;

Environment Variables

Set the following environment variables:

DB_PASSWORD=<your-mysql-password>
JWT_SECRET=<your-jwt-secret>

Do not commit these values to Git.

Run the Application

Windows PowerShell:

.\mvnw.cmd spring-boot:run

Run Tests

.\mvnw.cmd clean test

Testing

The project includes unit, controller, security, and integration tests covering authentication, account operations, transfers, idempotency, concurrency-related behavior, beneficiaries, reporting, ledger/audit persistence, and transaction history.

API Overview

Authentication

POST /api/auth/register
POST /api/auth/login

Accounts

POST /api/accounts
GET  /api/accounts

Transactions

POST /api/transactions/transfer
GET  /api/transactions

Beneficiaries

POST   /api/beneficiaries
GET    /api/beneficiaries
DELETE /api/beneficiaries/{id}

Reports

GET /api/reports/total-transferred
GET /api/reports/summary
GET /api/reports/summary/date-range

Monitoring

GET /actuator/health

Swagger UI — Complete Example Workflow

After downloading or cloning the project and starting the application, open:

"http://localhost:8080/swagger-ui/index.html"

Swagger UI provides an interactive interface for exploring and testing the LedgerX REST API.

The following workflow demonstrates the complete application flow from user registration to transfers, transaction history, reporting, and health monitoring.

1. Register the Sender User

Open:

POST /api/auth/register

Click Try it out and use:

{
  "name": "Alice Johnson",
  "email": "alice@ledgerx.com",
  "password": "Password123"
}

Click Execute.

A new user will be created.

---

2. Register the Receiver User

Open:

POST /api/auth/register

Use:

{
  "name": "Bob Williams",
  "email": "bob@ledgerx.com",
  "password": "Password123"
}

Click Execute.

You now have two users who can participate in a transfer.

---

3. Login as the Sender

Open:

POST /api/auth/login

Use:

{
  "email": "alice@ledgerx.com",
  "password": "Password123"
}

A successful response will contain a JWT token:

{
  "token": "<JWT_TOKEN>",
  "tokenType": "Bearer"
}

Copy the value of "token".

---

4. Authorize Swagger

At the top of Swagger UI, click:

Authorize

Enter:

Bearer <JWT_TOKEN>

Click Authorize and then Close.

Protected LedgerX endpoints can now be tested using the sender's authentication token.

---

5. Create the Sender Account

Open:

POST /api/accounts

Click Try it out and execute the request.

The application generates the account number automatically.

Example response:

{
  "id": 1,
  "accountNumber": "6193283507",
  "balance": 0.00,
  "currency": "INR",
  "status": "ACTIVE"
}

Save the generated "accountNumber".

Your account number will be different from the example above.

---

6. Login as the Receiver

Open:

POST /api/auth/login

Use:

{
  "email": "bob@ledgerx.com",
  "password": "Password123"
}

Copy the returned JWT token.

Click Authorize and replace the existing token with the receiver's token.

---

7. Create the Receiver Account

Open:

POST /api/accounts

Click Try it out and execute.

Example response:

{
  "id": 2,
  "accountNumber": "7321180738",
  "balance": 0.00,
  "currency": "INR",
  "status": "ACTIVE"
}

Save the generated "accountNumber".

Your account number will be different from the example above.

---

8. Authorize Swagger as the Sender

Login again using:

{
  "email": "alice@ledgerx.com",
  "password": "Password123"
}

Copy the returned JWT token.

Click Authorize and enter:

Bearer <JWT_TOKEN>

The sender must be authenticated to create beneficiaries and initiate transfers from their account.

---

9. Add the Receiver as a Beneficiary

Open:

POST /api/beneficiaries

Use the receiver account number generated by your application.

Example:

{
  "accountNumber": "7321180738",
  "nickname": "Bob"
}

Replace "7321180738" with the actual receiver account number generated by your application.

---

10. Transfer Money

Open:

POST /api/transactions/transfer

Use this complete request body:

{
  "senderAccountNumber": "6193283507",
  "receiverAccountNumber": "7321180738",
  "amount": 2500.00,
  "idempotencyKey": "ledgerx-demo-transfer-001"
}

Replace:

6193283507

with the actual sender account number.

Replace:

7321180738

with the actual receiver account number.

The sender must have sufficient balance for the transfer.

A successful response will look similar to:

{
  "id": 1,
  "transactionReference": "TXN-XXXXXXXX",
  "senderAccountNumber": "6193283507",
  "receiverAccountNumber": "7321180738",
  "amount": 2500.00,
  "status": "SUCCESS",
  "createdAt": "2026-10-05T10:35:00"
}

The actual transaction ID, transaction reference, and timestamp are generated by the application.

---

11. Test Idempotency

Send the exact same request again:

{
  "senderAccountNumber": "6193283507",
  "receiverAccountNumber": "7321180738",
  "amount": 2500.00,
  "idempotencyKey": "ledgerx-demo-transfer-001"
}

LedgerX detects the existing idempotency key and does not process another transfer.

This demonstrates protection against duplicate financial operations.

For a new transfer, use a different idempotency key:

{
  "senderAccountNumber": "6193283507",
  "receiverAccountNumber": "7321180738",
  "amount": 1000.00,
  "idempotencyKey": "ledgerx-demo-transfer-002"
}

---

12. Check Account Balances

Open:

GET /api/accounts

The balances should reflect successful transfers.

For example, if the sender had "10000.00" before transferring "2500.00":

Sender
10000.00 - 2500.00 = 7500.00

Receiver
0.00 + 2500.00 = 2500.00

The actual balances depend on the data in your local database.

---

13. View Transaction History

Open:

GET /api/transactions

Example:

GET /api/transactions?page=0&size=10

Filter by Status

GET /api/transactions?page=0&size=10&status=SUCCESS

Filter by Date Range

GET /api/transactions?page=0&size=10&from=2026-10-01&to=2026-10-05

Example transaction response:

{
  "content": [
    {
      "id": 1,
      "transactionReference": "TXN-XXXXXXXX",
      "senderAccountNumber": "6193283507",
      "receiverAccountNumber": "7321180738",
      "amount": 2500.00,
      "status": "SUCCESS",
      "createdAt": "2026-10-05T10:35:00"
    }
  ]
}

---

14. View Beneficiaries

Open:

GET /api/beneficiaries

Example response:

[
  {
    "id": 1,
    "accountNumber": "7321180738",
    "nickname": "Bob",
    "createdAt": "2026-10-05T10:30:00"
  }
]

---

15. Delete a Beneficiary

Open:

DELETE /api/beneficiaries/{id}

Example:

DELETE /api/beneficiaries/1

The beneficiary must belong to the authenticated user.

---

16. View Total Amount Transferred

Open:

GET /api/reports/total-transferred

Example response:

{
  "totalAmountTransferred": 3500.00
}

The actual value depends on the transactions stored in the database.

---

17. View Transaction Summary

Open:

GET /api/reports/summary

Example response:

{
  "totalTransactions": 2,
  "totalAmountTransferred": 3500.00,
  "successfulTransactions": 2,
  "failedTransactions": 0
}

---

18. View Date-Range Transaction Summary

Open:

GET /api/reports/summary/date-range?from=2026-10-01&to=2026-10-05

Example response:

{
  "totalTransactions": 2,
  "totalAmountTransferred": 3500.00,
  "successfulTransactions": 2,
  "failedTransactions": 0
}

The actual values depend on the transactions stored in the database.

---

19. Check Application Health

Open:

GET /actuator/health

Example response:

{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}

A status of "UP" indicates that the application health endpoint is reporting normally.

---

Complete Demo Flow

The complete LedgerX demonstration can be performed in this order:

1. Start MySQL
        ↓
2. Create the ledgerx database
        ↓
3. Configure DB_PASSWORD and JWT_SECRET
        ↓
4. Start the Spring Boot application
        ↓
5. Open Swagger UI
        ↓
6. Register Sender User
        ↓
7. Register Receiver User
        ↓
8. Login as Sender
        ↓
9. Create Sender Account
        ↓
10. Login as Receiver
        ↓
11. Create Receiver Account
        ↓
12. Authorize Swagger as Sender
        ↓
13. Add Receiver as Beneficiary
        ↓
14. Transfer Money
        ↓
15. Repeat the Same Transfer
        ↓
16. Verify Idempotency
        ↓
17. Check Account Balances
        ↓
18. View Transaction History
        ↓
19. View Beneficiaries
        ↓
20. View Reports
        ↓
21. Check Application Health

Project Status

LedgerX currently provides a working backend implementation with authentication, transactional transfers, idempotency, concurrency control, ledger and audit persistence, asynchronous event processing, reporting, API documentation, monitoring, and automated tests.
