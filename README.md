# Zalex Human Resources - Certification Request Service

A Spring Boot microservice that allows employees to submit and manage certificate of employment requests.

## Prerequisites

- [Docker](https://www.docker.com/get-started) installed and running
- `zalex-config-server` cloned as a sibling directory: https://github.com/varnavacy/zalex-config-server.git
  ```
  Projects/
    zalexHumanResources/   ← this project
    zalex-config-server/   ← must be here
  ```

## Architecture

The application runs as three Docker services:

| Service | Port | Description |
|---------|------|-------------|
| `postgres` | 5432 | PostgreSQL database |
| `config-server` | 8888 | Spring Cloud Config Server — serves configuration from [zalex-config-repo](https://github.com/varnavacy/zalex-config-repo) |
| `app` | 8080 | The HR microservice |

Startup order: `postgres` and `config-server` must be healthy before `app` starts. The app fetches its datasource configuration from the Config Server on startup using the `docker` profile.

## Running the Application

From the project root directory, run:

```bash
docker compose up --build
```

This will:
1. Build the Config Server and application images
2. Start PostgreSQL
3. Start the Config Server — clones config from GitHub and serves it on port 8888
4. Start the application once both PostgreSQL and the Config Server are healthy

The API will be available at `http://localhost:8080`.

To stop the application:

```bash
docker compose down
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/certification-requests` | Create a new certification request |
| GET | `/certification-requests` | List certification requests for an employee |
| GET | `/certification-requests/{referenceNo}` | Get a specific certification request |
| PATCH | `/certification-requests/{referenceNo}` | Update the purpose of a request |

## Create a Certification Request

```
POST /certification-requests
```

Request body:
```json
{
  "address_to": "Embassy of Neptune",
  "purpose": "Visa application for travel to Neptune, required for the upcoming conference attendance",
  "issued_on": "12/9/2022",
  "employee_id": "123456"
}
```

| Field | Type | Rules |
|-------|------|-------|
| `address_to` | String | Required. Letters, numbers, spaces and `. , ' -` only |
| `purpose` | String (textarea) | Required. Minimum 50 characters |
| `issued_on` | Date (d/M/yyyy) | Accepted but ignored — server always sets the current date |
| `employee_id` | Number | Required. Must be a positive number |

## List Certification Requests

```
GET /certification-requests?employeeId=123456
```

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `employeeId` | Yes | — | Employee ID |
| `page` | No | `0` | Page number (0-based) |
| `size` | No | `10` | Page size (max 10) |
| `sortBy` | No | `ISSUED_ON` | Sort field: `ISSUED_ON` or `STATUS` |
| `sortDirection` | No | `DESC` | Sort direction: `ASC` or `DESC` |
| `referenceNo` | No | — | Filter by exact reference number |
| `addressTo` | No | — | Filter by address (contains, case-insensitive) |
| `status` | No | — | Filter by status: `OPEN`, `IN_PROGRESS`, `CLOSED`, `CANCELLED` |

**Filtering examples:**
```
GET /certification-requests?employeeId=123456&status=OPEN
GET /certification-requests?employeeId=123456&addressTo=Embassy
GET /certification-requests?employeeId=123456&referenceNo=1
```

**Sorting examples:**
```
GET /certification-requests?employeeId=123456&sortBy=ISSUED_ON&sortDirection=ASC
GET /certification-requests?employeeId=123456&sortBy=STATUS&sortDirection=DESC
```

**Combined example:**
```
GET /certification-requests?employeeId=123456&status=OPEN&sortBy=ISSUED_ON&sortDirection=DESC&page=0&size=5
```

## Get a Certification Request

```
GET /certification-requests/{referenceNo}
```

## Update Purpose

```
PATCH /certification-requests/{referenceNo}?employeeId=123456
```

Request body:
```json
{
  "purpose": "Updated purpose with at least fifty characters required here"
}
```

## Test Data

Once the application is running, you can seed the database with sample data by running the following command from the project root:

```bash
docker exec -i zalexhumanresources-postgres-1 psql -U postgres -d zalex_hr << 'EOF'
INSERT INTO certification_requests (address_to, purpose, issued_on, employee_id, status) VALUES
('Embassy of Neptune', 'Visa application for travel to Neptune required for the upcoming international conference attendance', '2025-01-15', 123456, 0),
('HR Department', 'Proof of employment required for bank loan application submission to the financial institution', '2025-02-10', 123456, 1),
('Ministry of Foreign Affairs', 'Certificate of employment needed for residency permit renewal at the local government office', '2025-03-05', 123456, 2),
('Cyprus Embassy', 'Work permit documentation required for relocation to Cyprus office as part of the transfer process', '2025-04-20', 123456, 3),
('U.S. Embassy, Athens', 'Employment verification letter required for visa application to attend the annual sales conference', '2025-05-12', 123456, 0),
('Embassy of Neptune', 'Visa application for travel to Neptune required for the upcoming international conference attendance', '2025-01-20', 789012, 0),
('Tax Authority', 'Certificate of employment required for annual tax return filing with the local tax authority office', '2025-02-28', 789012, 2),
('National Bank', 'Proof of employment and salary confirmation required for mortgage application to the national bank', '2025-06-01', 789012, 1);
EOF
```

This creates 8 records across two employees (`123456` and `789012`) with a mix of statuses.

| employeeId | referenceNo | status | addressTo |
|---|---|---|---|
| 123456 | 1 | OPEN | Embassy of Neptune |
| 123456 | 2 | IN_PROGRESS | HR Department |
| 123456 | 3 | CLOSED | Ministry of Foreign Affairs |
| 123456 | 4 | CANCELLED | Cyprus Embassy |
| 123456 | 5 | OPEN | U.S. Embassy, Athens |
| 789012 | 6 | OPEN | Embassy of Neptune |
| 789012 | 7 | CLOSED | Tax Authority |
| 789012 | 8 | IN_PROGRESS | National Bank |

## Running Tests

```bash
./mvnw test
```
