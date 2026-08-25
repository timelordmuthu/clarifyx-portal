# ClarifyX Portal

A full-stack **Sales Approval & Document Management** web application built with **Spring Boot** (backend) and **React + Vite** (frontend).

> [!NOTE]
> **Project History Disclosure:** This project was originally built during a private internship. To comply with NDA and confidentiality agreements prior to open-sourcing, all proprietary company information, internal codenames, and employee data have been scrubbed from the codebase. The project was generically rebranded to "ClarifyX", and the original Git commit history was wiped to ensure no confidential data remained in previous snapshots. 

## Overview

ClarifyX Portal streamlines the sales document approval lifecycle — from initial request submission by Sales Representatives, through CFO review and approval, to final invoicing by the Finance team.

## Features

- **Role-Based Access Control** — Four distinct roles: System Admin, Sales Rep, CFO Approver, Finance Officer
- **Multi-Stage Approval Workflow** — SUBMITTED → APPROVED/REJECTED/NEED_MORE_INFO → CLOSED
- **File Attachments** — Secure cloud storage for PO emails, MSA/SLA documents, and invoices
- **Email Notifications** — Automated email alerts at every workflow stage
- **Real-Time Notifications** — In-app notification bell for pending actions
- **Dashboard & Analytics** — CFO and Finance dashboards with status summaries
- **Closed Requests Archive** — Dedicated view for all completed requests per role

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Tailwind CSS, Axios |
| Backend | Spring Boot 3.3, Spring Security, Hibernate JPA |
| Database | PostgreSQL (Supabase) |
| Storage | Supabase Storage Buckets |
| Auth | JWT (HTTP-only cookies) |
| Email | JavaMail (SMTP) |
| Deployment | Render (backend), Vercel (frontend) |

## Project Structure

```
/
├── backend/          # Spring Boot REST API
│   └── src/main/java/com/clarifyx/portal/
│       ├── config/       # Security, JWT
│       ├── controller/   # REST Controllers
│       ├── entity/       # JPA Entities
│       ├── enums/        # Status, Role, Zone enums
│       ├── repository/   # Spring Data JPA Repositories
│       └── service/      # Business Logic
├── frontend/         # React SPA
│   └── src/
│       ├── components/   # All UI components
│       └── api.js        # Axios instance
└── database/         # PostgreSQL schema & triggers
```

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL database (or Supabase project)

### Environment Variables

**Backend** (`application.properties` / Render env):
```
SUPABASE_DB_URL=
SUPABASE_DB_USER=
SUPABASE_DB_PASSWORD=
SUPABASE_URL=
SUPABASE_SERVICE_KEY=
JWT_SECRET=
SMTP_HOST=
SMTP_PORT=
SMTP_USERNAME=
SMTP_PASSWORD=
MAIL_FROM=
```

**Frontend** (`.env` / Vercel env):
```
VITE_API_BASE_URL=https://your-backend-url/api
```

### Running Locally

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

## User Roles

| Role | Permissions |
|---|---|
| `SYSTEM_ADMIN` | Create users, manage product groups & products |
| `SALES_REP` | Submit, resubmit, view own requests |
| `CFO_APPROVER` | Approve, reject, request more info, view dashboard |
| `FINANCE_OFFICER` | Close approved forms with invoice attachments |

## License

This project is provided for portfolio and demonstration purposes.
