# Pocket Ledger

An offline-first Personal Expense Tracker built as a compact Android architecture practice project.

## What is implemented

- Add, edit, soft-delete, search, categorize, and summarize expenses.
- Room is the single source of truth; Compose observes Room through Flow and StateFlow.
- Receipt images are selected with the system Photo Picker and copied to app-private storage.
- A Room outbox records each upsert/delete in the same local transaction.
- WorkManager runs unique sync work only with a validated network and retries transient failures.
- Retrofit defines the remote sync boundary. Hilt wires the database, API, repository, use cases, ViewModel, and Worker.
- Conflicts use a documented demo policy: pending local changes win; otherwise the newest `updatedAt` wins.

## Offline-first flow

```text
User mutation -> Room transaction (expense + outbox) -> Flow emits -> UI updates
                                                        |
Network returns -> WorkManager -> Retrofit sync -> merge newer server changes
                                      |
                              accepted outbox rows removed
                              local rows marked SYNCED
```

## Server contract

Set `BASE_URL` in `AppModule.kt` to your backend. The default `example.com` endpoint intentionally does not pretend to be a real server; all offline features still work and rows remain queued/failed until a backend is configured.

`POST /api/expenses/sync`

```json
{
  "deviceId": "client-uuid",
  "changes": [{
    "operation": "UPSERT",
    "expense": {
      "id": "expense-uuid",
      "title": "Lunch",
      "amountCents": 1250,
      "category": "FOOD",
      "spentAt": 1783180800000,
      "note": "",
      "receiptBase64": null,
      "updatedAt": 1783180800000,
      "deleted": false
    }
  }]
}
```

The response contains `acceptedIds` and `serverChanges` using the same expense shape. Production apps should use server revisions or vector clocks, authentication, pagination, and a dedicated object-storage upload for receipts rather than Base64.

## Run

Open the project in Android Studio and run the `app` configuration. Unit tests:

```bash
./gradlew testDebugUnitTest
```
