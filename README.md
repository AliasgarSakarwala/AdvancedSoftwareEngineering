# Tax API (COMS 4156)

Simple Spring Boot sales tax service. Data is stored in local JSON files under `IndividualProject/src/main/resources/data/`.

## Setup

Needs **Java 17** and **Maven**.

```bash
cd IndividualProject
mvn clean install
```

## Run the server

```bash
cd IndividualProject
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

Most endpoints need the `X-API-Key` header. You can grab a key from `IndividualProject/src/main/resources/data/clients.json`, or create a new client with `POST /v1/clients`.

## Useful commands

```bash
# tests
mvn test

# coverage report (opens under target/site/jacoco/index.html)
mvn test jacoco:report

# checkstyle
mvn checkstyle:check
```

CI runs tests + checkstyle on every push/PR via `.github/workflows/ci.yml`.

## Endpoints

Base path: `/v1`

| Method | Path | Auth | What it does |
|--------|------|------|--------------|
| POST | `/clients` | no | Create a client, returns an API key. 409 if name already exists. |
| POST | `/items` | yes | Create an item (`name`, `category`, `basePrice`). |
| GET | `/items` | yes | List items. Optional query params: `category`, `q` (name search). |
| GET | `/items/{id}` | yes | Get one item by id. |
| PATCH | `/items/{id}` | yes | Update an item's `basePrice`. |
| DELETE | `/items/{id}` | yes | Delete an item. |
| POST | `/tax/quote` | yes | Calculate tax. Send `state` + either `itemId`, or `price` + `category`. |
| GET | `/supported` | yes | Lists supported states and categories. |

Example tax quote:

```bash
curl -X POST http://localhost:8080/v1/tax/quote \
  -H "X-API-Key: YOUR_KEY" \
  -H "Content-Type: application/json" \
  -d '{"state":"CA","itemId":"ITEM_ID_HERE"}'
```

## Demo videos

- API service demo: **[paste Loom link here]**
- Client app demo: **[paste Loom link here]**

## Client app

I built a small [pick one: e-commerce site / state tax comparison tool / inventory tool / price tag generator / restaurant menu viewer] that calls this API.

AI tool used: **[e.g. Cursor / ChatGPT]**

The client demo video link is above. The client code is not required for submission.
