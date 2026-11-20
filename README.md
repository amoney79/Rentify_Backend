<div align="center">

# ⚙️ RENTIFY BACKEND
### Core Payment Gateway, Escrow & Scheduling Engine

*Powering instant KCB Buni M-Pesa STK Push, Visa card processing and real-time MongoDB transaction tracking for Kenyan real estate.*

<!--Technology Stack Badges -->
<p>
<img src="https://shields.io" alt="Java 25" />
<img src="https://shields.io"  alt="Spring Boot" />
<img src="https://shields.io" alt="MongoDB" />
<img src="https://shields.io" alt="M-Pesa KCB Buni" />
<img src="https://shields.io" alt="Visa & Card" />
</p>

<!--System status Badges--> 
<p>
<img src="https://shields.io" alt="Region" />
<img src="https://shields.io" alt="Security" />
<img src="https://shields.io" alt="Status" />
</p>

</div>

---

## 📌 Executive Summary

The **Rentify Backend** is  the financial and operational engine behind the Rentify Digital Real Estate Marketplace. Designed to handle high-trust property viewing scheduling across Nairobi, Mombasa, Malindi, and beyond, this RESTful Spring Boott service bridges the Rentify HTML/JS frontend with **KCB Buni banking rails** and **MongoDB**.

It automates the entire payment lifecycle:
1. **Search OAuth 2.0 Handshake:** Dynamically authenticates with KCB Buni using Base64-encoded client credentials.
2. **Instant STK Push & Card Processing:** Triggers mobile money prompts to tenants handsets and processes bank cards checkouts.
3. **Asynchronous Webhook Ingestion:** Listens for KCB Buni payment callbacks, validates statuses, and extracts official M-Pesa receipt numbers.
4. **Real-Time Status Polling:** Servers Low - latency transaction states ('PENDING','COMPLETED','FAILED') to the frontend receipt engine.

---

## 🏗️ System & Payment Sequence Architecture

```mermaid
sequenceDiagram
    autonumber
    actor Tenant as 🧑 Tenant (Frontend)
    participant API as ⚙️ Rentify Spring Boot API
    participant DB as 🍃 MongoDB (payments)
    participant KCB as 🏦 KCB Buni Gateway

    Tenant->>API: POST /api/payments/pay (phoneNumber, amount)
    API->>KCB: POST /token (OAuth 2.0 Client Credentials)
    KCB-->>API: Bearer Access Token
    API->>KCB: POST /mm/api/request/1.0.0/stkpush
    KCB-->>API: CheckoutRequestID
    API->>DB: Save Document (Status: PENDING)
    API-->>Tenant: 200 OK (reference: CheckoutRequestID)

    loop Every 3 Seconds
        Tenant->>API:GET/api/payments/status/{reference}
        API->>DB:fiindByReference(reference)
        DB->>API: Status: PENDING
        API->>Tenant: Status: PENDING
    end


    