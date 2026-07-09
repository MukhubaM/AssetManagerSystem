# ATLAS AMS

A Spring Boot asset lending system: staff register items, borrowers request them
for a chosen number of days, managers approve and check them out and a mock
payment step sits in between so the loan has a real cost attached to it.

Built with Spring Boot 3, Spring Security, Spring Data JPA, Thymeleaf and
PostgreSQL.

---

## Features

**Assets**
- Create, edit and retire assets, each with a daily rental rate (startup cost)
- Photo upload per asset
- Search by title, full inventory report

**Loans**
- Borrowers choose a duration (days) when requesting an item then cost is calculated
  automatically from `duration × daily rate` and snapshotted on the loan
- Full lifecycle: request → approve/reject → pay → check out → return, with an
  automatic overdue sweep and a 48-hour-before-due warning
- Loan history and overdue reports for staff

**Payments (mock)**
- A self-contained transaction ledger, no real payment processor is involved, just mock payment code
- Initiate → confirm → rollback. Rollback deletes the transaction record rather
  than modeling a refund and is blocked once a loan has actually been checked
  out
- Checkout is gated on a completed payment existing for the loan

**Accounts**
- Self-registration as Borrower or Manager (never as the Admin, the first
  admin is created automatically on first boot), every account is
  supervised by an admin
- Profile editing (name, contact details, profile picture, password) with
  email/password changes requiring the current password and forcing re-login
- Self-service account deletion (disable + scrub personal data not a hard
  delete)
- Forgot password (emailed reset link) and forgot username (identity proven via
  member number + ID number, since the login username is the account's email)
- Every account gets a system-generated, unique member number
  (derived from its own database ID, no separate counter)

**Notifications**
- In-app notifications for loan approval/rejection, checkout, return, overdue
  and the 48-hour warning, unread badge pops in the sidebar
- Overdue and account-security notifications are also emailed (configurable
  per-user in Settings), if email delivery fails, it fails soft, SMTP is configured,
  the app keeps working and just logs the message instead

**Admin**
- Promote/demote roles, enable/disable accounts
- Audit log of who did what, across assets, loans, payments and accounts



## Roles & permissions

| | Borrower | Manager | Admin |
|---|:---:|:---:|:---:|
| Browse assets, request/pay for/return loans | ✅ | ✅ | ✅ |
| Create/edit/retire assets | | ✅ | ✅ |
| Approve/reject/check out loans | | ✅ | ✅ |
| Reports (inventory, loan history, overdue) | | ✅ | ✅ |
| Manage users (roles, enable/disable) | | | ✅ |
| View audit log | | | ✅ |

Managers can also request loans themselves (they show up under 'My Loans' same
as a borrower), the role only affects staff-side permissions, not whether
someone can borrow something.



## Tech stack

- **Java 21**, **Spring Boot 3.3.5**
- Spring Web, Spring Security, Spring Data JPA, Spring Validation, Spring Mail
- **Thymeleaf** (+ `thymeleaf-extras-springsecurity6`) for server-rendered views
- **PostgreSQL**
- **Lombok**
- Maven



## Getting started

### Prerequisites
- Java 21
- Maven (or use the included `./mvnw`)
- PostgreSQL running locally (or point at a remote instance via env vars)

### 1. Create a database
```bash
createdb postgres   # or whatever database name you like
```
The app connects to `postgres` on `localhost:5432` by default

### 2. Configure environment variables (optional for local development)

Everything has a sane default for local development except the mail
credentials, which are optional. Nothing below is
required to get the app running against a local Postgres instance with default
credentials.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/postgres` | Database URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | *(empty)* | SMTP username |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
| `MAIL_FROM` | `no-reply@assetmanager.local` | 'From' address on outgoing emails |
| `APP_BASE_URL` | `http://localhost:8080` | Used to build absolute links in emails (e.g. password reset) |
| `ADMIN_EMAIL` | `admin@example.com` | Bootstrap admin login  see below |
| `ADMIN_PASSWORD` | `ChangeMe123!` | Bootstrap admin password  **change this in production** |

### 3. Run it
```bash
./mvnw spring-boot:run
```
The app starts on **http://localhost:8080**. Hibernate creates/updates the
schema automatically on startup (`ddl-auto=update`).

### 4. Log in

On first startup, since public registration can never create an Admin account,
one is created automatically:

- **Email:** `admin@example.com`
- **Password:** `ChangeMe123!` 

Log in and change this password immediately via **Settings → Edit Profile**.
Anyone else can self-register as a Borrower or Manager from the login page, admin can 
promote a Manager to Admin from **Manage Users** if you need a second admin.

### Building a jar
```bash
./mvnw clean package
java -jar target/AssetManagementSystem-0.0.1-SNAPSHOT.jar
```

---

## Project structure

```
src/main/java/.../AssetManagementSystem/
├── config/          Startup tasks (admin bootstrap, member-number backfill), web/model-attribute configuration
├── controller/      MVC controllers (one per feature area)
├── dto/             Request/form objects (validation lives here)
├── entity/          JPA entities and enums
├── exception/       Custom exceptions + a global @ControllerAdvice handler
├── repository/      Spring Data JPA repositories
├── security/        Spring Security config, UserDetails, current-user helper
└── service/         Business logic, one interface + impl per concern

src/main/resources/
├── templates/       Thymeleaf views (mirrors the controller package layout)
│   └── fragments/   Shared sidebar/topbar/icons included on every page
├── static/          CSS design system + theme-switcher JS
└── application.properties
```

---

## Design notes

A few deliberate decisions worth knowing about before extending this:

- **Payments are a mock system.** There's no Stripe/PayPal integration, it's
  an internal ledger for practicing the initiate/confirm/rollback flow.
  'Rollback' deletes the transaction row rather than modeling a real refund.
- **Loan duration is days-only for now(until I develop it further).** 
- **Account deletion doesn't delete the row.** A user's loan/payment/audit
  history references their account and `Loan.borrower` is a `NOT NULL`
  foreign key, a hard delete would either crash or destroy history.
  'Delete account' disables the account and scrubs personal data instead.
- **Every association actually used in a template is `EAGER`(I changed it from `LAZY`).** The app runs
  with `spring.jpa.open-in-view=false` so the Hibernate session is closed
  before a view renders. If you add a field to a template that follows a new
  association, make that association `EAGER` too (or fetch it explicitly),
  otherwise you'll hit a `LazyInitializationException` that Thymeleaf reports
  as a generic template-parsing error.
- **Manager permission checkboxes at registration are informational only.**
  A manager can indicate they expect to approve loans, register assets, etc.
  at signup, but actual authorization is role-based (`SecurityConfig`), every
  Manager has the same access regardless of those checkboxes today(but development is on the roadmap).
- **Theme and light mode can be changed within the dashboard only.**
  Changing the theme and light mode applies in all the pages, even after re-running the system
---

## License


