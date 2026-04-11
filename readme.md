# 🌍 Public Information API

> **⚠️ This project is currently under active development. New features and data sources are on the way.**

A Spring Boot REST API that provides publicly available information about countries worldwide and Angola's internal administrative structure — provinces, municipalities, and comunas.

---

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 4.x**
- **Caffeine** — in-memory caching
- **Jackson** — JSON deserialization
- **Lombok** — boilerplate reduction
- **SpringDoc OpenAPI** — automatic API documentation (Swagger UI)

---

## 🚀 Running the Project

```bash
./mvnw spring-boot:run
```

The server starts on port **8086** by default.

Swagger UI is available at:
```
http://localhost:8086/api/swagger-ui.html
```

---

## 📡 Endpoints

### 🌐 Countries

#### `GET /v1/data/countries`

Returns a paginated list of countries from around the world, with optional filters.

**Query Parameters**

| Parameter       | Type     | Required | Description                                 |
|-----------------|----------|----------|---------------------------------------------|
| `page`          | `int`    | No       | Page number (default: `0`)                  |
| `size`          | `int`    | No       | Page size (default: `10`)                   |
| `countryCode`   | `string` | No       | Filter by ISO 3166-1 alpha-2 or alpha-3 code (e.g. `AO`, `AGO`) |
| `countryName`   | `string` | No       | Filter by country name (partial match)      |
| `region`        | `string` | No       | Filter by region (e.g. `Africa`, `Europe`)  |
| `phoneCode`     | `string` | No       | Filter by phone code (e.g. `244` or `+244`) |

**Example Requests**

```http
GET /v1/data/countries
GET /v1/data/countries?region=Africa&page=0&size=5
GET /v1/data/countries?phoneCode=244
GET /v1/data/countries?countryCode=AO
GET /v1/data/countries?countryName=angola
```

**Example Response**

```json
{
  "countries": [
    {
      "countryCode": "AO",
      "name": "Angola",
      "officialName": "Republic of Angola",
      "languages": ["Portuguese"],
      "currency": "Angolan kwanza",
      "currencySymbol": "Kz",
      "flag": "🇦🇴",
      "phoneCode": "+244",
      "region": "Africa",
      "borders": ["COG", "COD", "ZMB", "NAM"]
    }
  ]
}
```

> **Locale support:** Pass an `Accept-Language` header (e.g. `pt`, `fr`, `es`) to receive country names translated into the requested language where available.

---

### 🇦🇴 Angola

#### `GET /v1/data/angola`

Returns a paginated list of Angola's provinces with detailed administrative data, including governor, vice-governors, municipalities, and comunas.

**Query Parameters**

| Parameter          | Type     | Required | Description                                          |
|--------------------|----------|----------|------------------------------------------------------|
| `page`             | `int`    | No       | Page number (default: `0`)                           |
| `size`             | `int`    | No       | Page size (default: `10`)                            |
| `provinceName`     | `string` | No       | Filter by province name (partial match)              |
| `capital`          | `string` | No       | Filter by provincial capital name (exact match)      |
| `municipalityName` | `string` | No       | Filter by municipality name (partial match)          |

**Example Requests**

```http
GET /v1/data/angola
GET /v1/data/angola?provinceName=benguela
GET /v1/data/angola?capital=LUANDA
GET /v1/data/angola?municipalityName=lobito
```

**Example Response**

```json
{
  "provinces": [
    {
      "id": 2,
      "name": "BENGUELA",
      "capital": "BENGUELA",
      "languages": "Português, Umbundu, Ohvanyaneka",
      "area": "39.827 km²",
      "ethnicities": "Ovimbundu, Ohvanyaneka",
      "foundingDate": "17-05-1617",
      "municipalityCount": 23,
      "governor": {
        "name": "Manuel Nunes Júnior",
        "appointmentDate": "31/10/2024",
        "imageUrl": "https://..."
      },
      "viceGovernors": [...],
      "municipalities": [
        {
          "name": "LOBITO",
          "administrator": "Evaristo Calopa Mário",
          "foundingDate": "",
          "comunaCount": 0,
          "comunas": []
        }
      ]
    }
  ]
}
```

---

## 🧠 Caching

Responses are cached in-memory using **Caffeine** to avoid redundant computation on repeated requests.

| Cache      | TTL    | Max Entries |
|------------|--------|-------------|
| `countries`| 1 hour | 500         |
| `angola`   | 1 hour | 500         |

Cache keys are built from all query parameters — different combinations of filters, page, size, and locale always produce independent cache entries.

---

## 📁 Project Structure

```
src/main/java/ao/vanadio/publicinformationapi/
│
├── bootstrap/
│   └── cache/              # CacheConfig, CacheNames
│
├── features/
│   ├── countries/
│   │   ├── controller/     # CountryController
│   │   ├── service/        # CountryService
│   │   ├── mapper/         # CountryMapper
│   │   └── dto/
│   │       ├── request/    # CountryFilter
│   │       ├── response/   # CountryResponse, CountryListResponse
│   │       └── source/     # CountryJson
│   │
│   └── angola/
│       ├── controller/     # AngolaController
│       ├── service/        # AngolaService
│       ├── mapper/         # AngolaMapper
│       └── dto/
│           ├── request/    # AngolaFilter
│           ├── response/   # ProvinceResponse, ProvinceListResponse, ...
│           └── source/     # ProvinceJson, AngolaJson
│
└── PublicInformationApiApplication.java
```

---

## 🔮 Coming Soon

This API is still growing. Planned additions include:

- 🗺️ More Angola-specific data (population density, historical facts, coordinates)
- 🌎 Additional country detail endpoints (by code, by region summary)
- 🔍 Full-text search improvements
- 📊 Statistics and aggregation endpoints
- 🌐 Expanded translation support for country names

---

## 📄 License

This project is for public information purposes. Data is sourced from publicly available datasets.