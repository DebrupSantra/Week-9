# Secure E-commerce Platform

This project implements a Spring Boot backend for a multi-tenant e-commerce platform with JWT authentication, tenant isolation, OAuth2 login, audit logging, and rate limiting.

## Features
- JWT authentication and refresh-token rotation
- Role-based access control (ADMIN, VENDOR, CUSTOMER)
- Multi-tenant context enforced by `X-Tenant-ID`
- OAuth2 login with Google and GitHub
- Bucket4j rate limiting with per-IP / per-user / per-tenant buckets
- Security audit logging
- PostgreSQL-ready configuration with H2 test profile

## Run locally
1. Copy `.env.example` to `.env` and fill in environment values.
2. Start PostgreSQL locally or use Docker Compose:
   `docker compose up -d`
3. Run the app:
   `mvn spring-boot:run`
4. Test endpoints with a JSON request body.

## Default admin account
The app seeds a default tenant and admin account on startup when no users are present.

## Security notes
- JWT secrets and OAuth client secrets are read from environment variables.
- Refresh tokens are stored as hashes.
- Tenant context is cleaned up after each request.
