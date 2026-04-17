Design (concise)
=================

Summary
-------
Spring Boot service exposing POST /payment and GET /payment/{id}. It validates incoming requests, forwards to a bank simulator (/payments), maps the response to AUTHORIZED/DECLINED, and stores payments in an in-memory repository.

Components (short)
- Controller: `PaymentGatewayController` — handles HTTP endpoints.
- Service: `PaymentGatewayService` — validation, bank call, mapping, persistence.
- Client: `BankClientRest` (extends `RestBaseClient`) — calls external bank, maps 400/503 to custom exceptions.
- Validation: `PaymentRequestValidator` + centralized `ValidationMessages`.
- Repository: `PaymentsRepository` — in-memory store.

Validation rules (brief)
- Card number: digits, length check.
- Expiry: numeric month/year, must be in the future.
- Currency: must be supported (USD, EUR, CNY).
- Amount: >= 1 (cents).
- CVV: 3–4 numeric digits.

Error mapping
- Validation errors -> 400 with {"message":"..."}.
- Bank 400 -> `BadRequestFromBankException` -> 500.
- Bank 503 -> `BankServiceUnavailableException` -> 500.

PaymentStatus decision
- The `REJECTED` payment status was intentionally removed. Rationale: if a request is invalid or is not forwarded to the bank (validation failure or client error), the API returns an HTTP error status code and an error message instead of creating a payment record with a `REJECTED` status. Only outcomes returned by the bank are represented as `AUTHORIZED` or `DECLINED`.
