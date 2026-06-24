# Finsight - System Specification and Architecture

This document provides a comprehensive overview of the Finsight application, detailing its architecture, domain models, database schema, core business rules, and API endpoints.

---

## 1. System Architecture Overview

Finsight is built using a clean, multi-layered architecture conforming to Spring Boot conventions. The code is structured to enforce separation of concerns, transactional integrity, and strict multi-tenant data isolation.

### Architectural Layers

- **Presentation Layer (Controllers)**: Exposes REST API endpoints. Validates incoming request payloads using Jakarta Validation constraints (`@Valid`, `@NotNull`, `@NotEmpty`). Employs OpenAPI (Swagger) annotations for interactive API documentation.
- **Service Layer (Services)**: Houses core business logic. Employs declarative transaction management (`@Transactional`) to guarantee data integrity across multiple repository calls.
- **Data Access Layer (Repositories)**: Utilizes Spring Data JPA for object-relational mapping. Integrates custom JPQL queries for complex data aggregation.
- **Database Layer**: Implements a PostgreSQL instance with schema evolution managed strictly via Flyway migration scripts.

### Data Isolation Strategy

Finsight is a multi-tenant system where each user's financial records are strictly isolated.
- Every domain entity (except Token metadata) is directly bound to a user via a `userId` field.
- Presentation layers extract the authenticated user identity from the security context using `@AuthenticationPrincipal SecurityUser`.
- Service layer methods accept `userId` as an explicit parameter and append it to all data-access queries (e.g., `findByIdAndUserId`, `existsByCategoryIdAndUserId`), preventing unauthorized cross-user access.

---

## 2. Data Models and Database Schema

The core domain model consists of six primary entities: `User`, `Account`, `Category`, `Transaction`, `Budget`, and `Token`.

### Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ ACCOUNT : owns
    USER ||--o{ CATEGORY : owns
    USER ||--o{ TRANSACTION : owns
    USER ||--o{ BUDGET : owns
    USER ||--o{ TOKEN : owns
    
    ACCOUNT ||--o{ TRANSACTION : "origin or destination"
    CATEGORY ||--o{ TRANSACTION : classifies
    CATEGORY ||--o? BUDGET : limits
```

### Domain Entities Detail

#### User
Represents a registered consumer of the application.
- `id` (UUID, Primary Key): Unique identifier.
- `email` (String, Unique, Not Null): User login identifier.
- `password` (String, Not Null): Bcrypt-encoded password.
- `createdAt` (Instant, Not Null): Record creation timestamp.
- `updatedAt` (Instant, Not Null): Record modification timestamp.

#### Account
Represents a source of funds or a savings repository.
- `id` (UUID, Primary Key): Unique identifier.
- `name` (String, Not Null): Human-readable name.
- `description` (String, Not Null): Description of the account.
- `balance` (Long, Not Null): Current account balance in minor units (e.g., cents to avoid floating-point errors).
- `type` (AccountType enum, Not Null): `REGULAR` or `AHORRO` (Savings).
- `targetAmount` (Long, Nullable): Saving goal amount. Applicable only to `AHORRO` accounts.
- `userId` (UUID, Not Null): Owner ID.
- `createdAt` / `updatedAt` (Instant): Auditing timestamps.

#### Category
Represents classifications for incoming or outgoing transactions.
- `id` (UUID, Primary Key): Unique identifier.
- `name` (String, Not Null): Classification name (unique per user and type).
- `type` (CategoryType enum, Not Null): `INGRESO` (Income) or `GASTO` (Expense).
- `userId` (UUID, Not Null): Owner ID.
- `createdAt` / `updatedAt` (Instant): Auditing timestamps.

#### Transaction
Represents a single movement of funds.
- `id` (UUID, Primary Key): Unique identifier.
- `dateIssued` (Instant, Not Null): Event date.
- `type` (TransactionType enum, Not Null): `INGRESO`, `GASTO`, or `TRANSFERENCIA`.
- `amount` (Long, Not Null): Value of the transaction in minor units (must be positive).
- `description` (String, Not Null): Human-readable descriptor.
- `originAccountId` (UUID, Nullable): Account from which funds are deducted (required for GASTO and TRANSFERENCIA).
- `destinationAccountId` (UUID, Nullable): Account into which funds are added (required for INGRESO and TRANSFERENCIA).
- `categoryId` (UUID, Nullable): Link to a classification (required for INGRESO and GASTO).
- `userId` (UUID, Not Null): Owner ID.
- `createdAt` / `updatedAt` (Instant): Auditing timestamps.

#### Budget
Represents a monthly expense constraint on a category.
- `id` (UUID, Primary Key): Unique identifier.
- `limitAmount` (Long, Not Null): Spending threshold in minor units.
- `categoryId` (UUID, Not Null): Associated expense category. Unique constraint per user.
- `userId` (UUID, Not Null): Owner ID.
- `createdAt` / `updatedAt` (Instant): Auditing timestamps.

#### Token
Represents active JWT authentication credentials.
- `id` (UUID, Primary Key): Unique identifier.
- `jti` (UUID, Unique, Not Null): JWT identifier.
- `refreshToken` (String, Unique, Not Null): Cryptographic refresh token.
- `tokenType` (TokenType, Not Null): Defaults to `BEARER`.
- `userId` (UUID, Not Null): Linked user ID.
- `revoked` (boolean, Not Null): Revocation state flag.
- `accessTokenExpiresAt` (Instant, Not Null): Expiry of the access token.
- `refreshTokenExpiresAt` (Instant, Not Null): Expiry of the refresh token.
- `createdAt` (Instant): Auditing timestamp.

---

## 3. Database Migrations History

Flyway migration scripts reside in `src/main/resources/db/migration/postgres/` and outline the schema's evolutionary history:

- **`V1__init.sql`**: Initializes core tables (`users`, `accounts`, `categories`, `transactions`) and adds standard constraints.
- **`V2__auth_tokens.sql`**: Creates the `tokens` table to handle session-based refresh token revocation.
- **`V3__drop_old_fk.sql`**: Drops redundant foreign key constraints.
- **`V4__uq_categories_user_name_type.sql`**: Introduces a unique constraint on `categories` for `(user_id, name, type)` to prevent duplicate names per category classification.
- **`V5__drop_not_null_con_accounts_involved.sql`**: Updates transaction account nullable columns to support flexible transactions (e.g. incomes have no origin account, expenses have no destination account).
- **`V6__drop_not_null_con_category_id.sql`**: Updates transaction category column to be nullable to support transfers (which do not require a category).
- **`V7__fix_transaction_type_col.sql`**: Changes transaction `type` column mapping.
- **`V8__budgets.sql`**: Creates the `budgets` table with foreign key and unique constraint on `(user_id, category_id)`.
- **`V9__savings.sql`**: Extends the `accounts` table with a `type` column (default `'REGULAR'`) and a nullable `target_amount` column.

---

## 4. Core Business Logic and Rules

### Transaction Flow and Balance Integrity

Finsight handles double-entry balance updates atomically. A transaction operation dynamically alters account balances using the following logic:

#### Creation Flow
- **INGRESO**: Verifies the target account is `REGULAR` and increases its balance by `amount`.
- **GASTO**: Verifies the origin account is `REGULAR` and decreases its balance by `amount`.
- **TRANSFERENCIA**: Checks that origin and destination accounts are different. Deducts `amount` from the origin account and adds it to the destination account. If the origin is a savings account (`AHORRO`), it blocks the transaction if the operation leads to a negative balance.

#### Deletion and Update Flow
Before modifying or removing a transaction, its original impact must be rolled back.
- Deleting an INGRESO decreases the target account's balance by `amount`.
- Deleting a GASTO increases the origin account's balance by `amount`.
- Deleting a TRANSFERENCIA adds `amount` back to the origin and deducts it from the destination.
During an update, the transaction is reverted first, the entity is modified, and the new rules are applied to update balances based on the new type, accounts, and amount.

### Category Integrity Guards

To prevent orphan records and data inconsistency, the application implements active safeguards in `CategoryService`:
- **Type Alteration Guard**: A category's type (`INGRESO` or `GASTO`) cannot be changed if there are transactions associated with it.
- **Deletion Guard**: A category cannot be deleted if there are existing transactions associated with it.
- **Budget Guard**: A category cannot be deleted if there is an active budget configured for it.

### Budget Management & Timezone Boundaries

Budgets are checked against current-month expenses.
- Aggregation is performed in `TransactionRepository.computeMonthlyExpenseByCategoryId` by summing all transactions of type `GASTO` matching the category and user ID.
- To prevent time zone shifts from misaligning monthly calculations, the query boundaries are computed dynamically based on the client's local timezone (e.g. `America/New_York` or `Europe/London`). The service computes the start and end `Instant` boundaries for the current month matching the client's offset.
- Only one budget can be configured per category per user.

### Savings Account Limitations

- Direct income and expense transactions (`INGRESO` and `GASTO`) cannot interact with savings (`AHORRO`) accounts. These operations require a `REGULAR` account.
- Savings accounts can only be funded or drawn from via transfers (`TRANSFERENCIA`).
- Savings accounts cannot be deleted if their balance is greater than zero.
- A savings account cannot have a negative balance under any circumstance.

---

## 5. API Endpoints Reference

### Authentication Module (`/auth`)

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| POST | `/auth/signup` | Register a new user | `UserDTOS.SignupRequest` | `UserDTOS.Response` |
| POST | `/auth/login` | Login to receive JWT credentials | `UserDTOS.LoginRequest` | `UserDTOS.LoginResponse` |
| POST | `/auth/refresh` | Refresh an expired access token | `TokenDTOS.RefreshRequest` | `TokenDTOS.RefreshResponse` |

### Accounts Module (`/accounts`)

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| GET | `/accounts` | List current user's accounts | None | `List<AccountDTOS.Response>` |
| GET | `/accounts/{id}` | Get account by ID | None | `AccountDTOS.Response` |
| POST | `/accounts` | Create a regular account | `AccountDTOS.CreateRequest` | `AccountDTOS.Response` |
| POST | `/accounts/savings` | Create a savings account | `AccountDTOS.CreateSavingsRequest` | `AccountDTOS.Response` |
| PATCH | `/accounts/{id}` | Update account properties | `AccountDTOS.UpdateRequest` | `AccountDTOS.Response` |
| DELETE | `/accounts/{id}` | Delete account (must have 0 balance if AHORRO) | None | 204 No Content |

### Categories Module (`/categories`)

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| GET | `/categories` | List current user's categories | None | `List<CategoryDTOS.Response>` |
| GET | `/categories/{id}` | Get category by ID | None | `CategoryDTOS.Response` |
| POST | `/categories` | Create a category | `CategoryDTOS.CreateRequest` | `CategoryDTOS.Response` |
| PATCH | `/categories/{id}` | Update category properties | `CategoryDTOS.UpdateRequest` | `CategoryDTOS.Response` |
| DELETE | `/categories/{id}` | Delete category (blocked if has transactions/budgets) | None | 204 No Content |

### Transactions Module (`/transactions`)

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| GET | `/transactions` | List current user's transactions | None | `List<TransactionDTOS.Response>` |
| GET | `/transactions/{id}` | Get transaction by ID | None | `TransactionDTOS.Response` |
| POST | `/transactions/ingresos` | Create an income entry | `TransactionDTOS.CreateIngresoRequest` | `TransactionDTOS.Response` |
| POST | `/transactions/gastos` | Create an expense entry | `TransactionDTOS.CreateGastoRequest` | `TransactionDTOS.Response` |
| POST | `/transactions/transferencias` | Create a transfer entry | `TransactionDTOS.CreateTransferenciaRequest` | `TransactionDTOS.Response` |
| PATCH | `/transactions/{id}` | Update transaction properties | `TransactionDTOS.UpdateRequest` | `TransactionDTOS.Response` |
| DELETE | `/transactions/{id}` | Delete transaction | None | 204 No Content |

### Budgets Module (`/budgets`)

| Method | Endpoint | Description | Headers / Query Params | Request Body | Response |
|---|---|---|---|---|---|
| GET | `/budgets` | List current user's budgets with monthly expenses | `X-Timezone` (Header) | None | `List<BudgetDTOS.Response>` |
| GET | `/budgets/{id}` | Get budget by ID with monthly expenses | `X-Timezone` (Header) | None | `BudgetDTOS.Response` |
| POST | `/budgets` | Create a budget | `X-Timezone` (Header) | `BudgetDTOS.CreateRequest` | `BudgetDTOS.Response` |
| PATCH | `/budgets/{id}` | Update budget threshold | `X-Timezone` (Header) | `BudgetDTOS.UpdateRequest` | `BudgetDTOS.Response` |
| DELETE | `/budgets/{id}` | Delete budget | None | None | 204 No Content |
