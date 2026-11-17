# 🌍 Public Information API

> **⚠️ This project is currently under active development. New features and data sources are on the way.**

A Spring Boot REST API that provides publicly available information about countries worldwide and their internal administrative structure — states, provinces, counties, municipalities, communes and cities — with detailed data for Angola (governors, administrators, comunas).

---

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 4.x**
- **H2 (in-memory)** — embedded database seeded at startup with world subdivisions and cities
- **Caffeine** — in-memory caching
- **Jackson** — JSON deserialization
- **Lombok** — boilerplate reduction
- **SpringDoc OpenAPI** — automatic API documentation (Swagger UI)

---

## 🚀 Running the Project

```bash
./mvnw spring-boot:run
```

The server starts on port **8088** by default (override with `PORT`).

Swagger UI is available at:
```
http://localhost:8088/swagger-ui.html
```

At startup the embedded H2 database is filled from `src/main/resources/data/geo/*.json.gz` and `angola.json` (~10 s). Set `app.geo.seed.enabled=false` to skip it.

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

#### `GET /v1/data/countries/{countryCode}`

Full details of one country (ISO alpha-2 or alpha-3): capital, sub-region, area, coordinates, demonym, TLDs, UN membership, plus a summary of its administrative levels and the number of known cities.

```json
{
  "countryCode": "AO",
  "alpha3Code": "AGO",
  "name": "Angola",
  "capitals": ["Luanda"],
  "subregion": "Middle Africa",
  "area": 1246700.0,
  "subdivisionLevels": [
    { "level": 1, "count": 21, "types": ["province"] },
    { "level": 2, "count": 326, "types": ["municipality"] },
    { "level": 3, "count": 379, "types": ["commune"] }
  ],
  "cityCount": 83
}
```

---

### 🗺️ Subdivisions and cities (all countries)

All list endpoints return a page envelope:

```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 21, "totalPages": 2 }
```

`size` defaults to `20` (max `100`). Names are translated through `Accept-Language` when a translation exists. `{subdivisionCode}` accepts an ISO 3166-2 code (`AO-LUA`, `US-CA`, or just `LUA`) or the numeric `id` returned by the API.

| Endpoint | Description | Filters |
|---|---|---|
| `GET /v1/data/countries/{countryCode}/subdivisions` | States, provinces, counties, municipalities, communes | `level` (defaults to `1` when no other filter is set), `parentCode`, `type`, `name` |
| `GET /v1/data/countries/{countryCode}/subdivisions/{subdivisionCode}` | Details: coordinates, population, timezone, Wikidata id, capital, leaders, translations, direct children and city count | — |
| `GET /v1/data/countries/{countryCode}/subdivisions/{subdivisionCode}/children` | Paginated direct children (e.g. municipalities of a province, comunas of a municipality) | `page`, `size` |
| `GET /v1/data/countries/{countryCode}/cities` | Cities ordered by population | `subdivisionCode` (includes nested levels), `name`, `minPopulation` |

**Example Requests**

```http
GET /v1/data/countries/US/subdivisions?name=calif
GET /v1/data/countries/AO/subdivisions/AO-LUA
GET /v1/data/countries/AO/subdivisions/AO-BGU/children?size=50
GET /v1/data/countries/AO/subdivisions?level=3&name=caxito
GET /v1/data/countries/BR/cities?subdivisionCode=SP&minPopulation=500000
```

**Example Response** — `GET /v1/data/countries/AO/subdivisions/AO-LUA`

```json
{
  "id": 2000011,
  "countryCode": "AO",
  "code": "LUA",
  "isoCode": "AO-LUA",
  "name": "Luanda",
  "type": "province",
  "level": 1,
  "population": 8665510,
  "capital": "Ingombota",
  "area": "1.648,1 km²",
  "source": "angola.json",
  "leaders": [
    { "role": "GOVERNOR", "name": "Luís Manuel da Fonseca Nunes", "appointmentDate": "31/10/2024" }
  ],
  "cityCount": 7,
  "childCount": 16,
  "children": [
    { "id": 2100171, "name": "Belas", "type": "municipality", "level": 2, "parentId": 2000011 }
  ]
}
```

Errors use RFC 9457 problem details (`404` for unknown country/subdivision, `400` for invalid parameters).

---

### 🇦🇴 Angola

#### `GET /v1/data/angola`

Returns a paginated list of Angola's provinces with detailed administrative data, including governor, vice-governors, municipalities, and comunas. The same data is also available through the generic subdivision endpoints above (`/v1/data/countries/AO/...`), where it replaces the generic dataset for Angola.

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
| `geo`      | 1 hour | 500         |

Cache keys are built from all query parameters — different combinations of filters, page, size, and locale always produce independent cache entries.

---

## 📚 Data Sources

| Data | Source | License |
|---|---|---|
| Countries | [REST Countries](https://restcountries.com) dataset (`data/countries.json`) | MPL 2.0 |
| World states, counties and cities | [dr5hn/countries-states-cities-database](https://github.com/dr5hn/countries-states-cities-database) (`data/geo/*.json.gz`) | [ODbL 1.0](https://opendatacommons.org/licenses/odbl/1-0/) |
| Angola provinces, municipalities, comunas and leaders | `data/angola.json`, curated from official Angolan government sources | Public information |

Angola data was last reviewed on 2026-11-17 (governors and vice-governors from governo.gov.ao, population and area from the INE 2024 census). Province codes follow ISO 3166-2:AO; Cuando (`AO-CUB`), Cubango (`AO-CBG`), Moxico Leste (`AO-MLE`) and Icolo e Bengo (`AO-ICB`) created by the 2024 reform have no ISO code yet, so GeoNames-style codes are used.

The world subdivision dataset is © dr5hn and contributors, made available under the Open Database License. Its records also credit GeoNames (CC BY 4.0), OpenStreetMap (ODbL) and Wikidata (CC0).

To refresh the world dataset with the latest dr5hn release:

```bash
python scripts/build_geo_data.py
```

The script downloads `states.json`, `counties.json` and the cities release, flattens states and counties into one hierarchy (county ids are offset by `1000000`) and writes compact gzip files to `src/main/resources/data/geo/`.

---

## 📁 Project Structure

```
src/main/java/ao/vanadio/publicinformationapi/
│
├── bootstrap/
│   ├── cache/              # CacheConfig, CacheNames
│   └── web/                # PageQuery, PageResponse, GlobalExceptionHandler
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
│   ├── geo/
│   │   ├── controller/     # GeoController
│   │   ├── service/        # GeoService
│   │   ├── repository/     # GeoRepository (JdbcClient over H2)
│   │   ├── seed/           # GeoDataSeeder, AngolaGeoSeeder
│   │   └── dto/
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

- 🗺️ More Angola-specific data (historical facts, coordinates per municipality)
- 🌎 Region summary endpoints
- 🔍 Full-text search improvements
- 📊 Statistics and aggregation endpoints
- 🌐 Expanded translation support for country names

---

## 📄 License

This project is for public information purposes. Data is sourced from publicly available datasets.