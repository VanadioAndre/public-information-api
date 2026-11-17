import gzip
import io
import json
import sys
import urllib.request
from pathlib import Path

BASE = "https://raw.githubusercontent.com/dr5hn/countries-states-cities-database/master/json"
CITIES = "https://github.com/dr5hn/countries-states-cities-database/releases/latest/download/json-cities.json.gz"
COUNTY_ID_OFFSET = 1_000_000
OUTPUT = Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "data" / "geo"


def fetch(url):
    with urllib.request.urlopen(url) as response:
        data = response.read()
    if url.endswith(".gz"):
        data = gzip.decompress(data)
    return json.load(io.BytesIO(data))


def to_int(value):
    if value is None or value == "":
        return None
    return int(value)


def to_coord(value):
    if value is None or value == "":
        return None
    return round(float(value), 6)


def depth_resolver(parent_of, base_depth):
    cache = {}

    def depth(record_id, visiting=()):
        if record_id in cache:
            return cache[record_id]
        parent = parent_of(record_id)
        if parent is None or parent in visiting:
            value = base_depth(record_id)
        else:
            value = depth(parent, visiting + (record_id,)) + 1
        cache[record_id] = value
        return value

    return depth


def subdivision(record_id, parent_id, level, source, code, iso):
    return {
        "id": record_id,
        "parentId": parent_id,
        "countryCode": source["country_code"],
        "level": level,
        "code": code,
        "isoCode": iso,
        "name": source["name"],
        "nativeName": source.get("native"),
        "type": source.get("type"),
        "latitude": to_coord(source.get("latitude")),
        "longitude": to_coord(source.get("longitude")),
        "population": to_int(source.get("population")),
        "timezone": source.get("timezone"),
        "wikiDataId": source.get("wikiDataId"),
        "translations": source.get("translations") or {},
    }


def build_subdivisions(states, counties):
    state_by_id = {s["id"]: s for s in states}
    county_by_id = {c["id"]: c for c in counties}

    def state_parent(state_id):
        parent = to_int(state_by_id[state_id].get("parent_id"))
        return parent if parent in state_by_id else None

    state_depth = depth_resolver(state_parent, lambda _: 1)

    def county_parent(county_id):
        parent = to_int(county_by_id[county_id].get("parent_id"))
        return parent if parent in county_by_id else None

    def county_base(county_id):
        state_id = county_by_id[county_id].get("state_id")
        return state_depth(state_id) + 1 if state_id in state_by_id else 1

    county_depth = depth_resolver(county_parent, county_base)

    result = []
    for s in states:
        parent = state_parent(s["id"])
        result.append(subdivision(s["id"], parent, state_depth(s["id"]), s, s.get("iso2"), s.get("iso3166_2")))
    for c in counties:
        parent = county_parent(c["id"])
        if parent is not None:
            parent_id = parent + COUNTY_ID_OFFSET
        elif c.get("state_id") in state_by_id:
            parent_id = c["state_id"]
        else:
            parent_id = None
        result.append(subdivision(c["id"] + COUNTY_ID_OFFSET, parent_id, county_depth(c["id"]), c, None, None))
    return result


def build_cities(cities, state_ids, county_ids):
    result = []
    for c in cities:
        if c.get("county_id") in county_ids:
            subdivision_id = c["county_id"] + COUNTY_ID_OFFSET
        elif c.get("state_id") in state_ids:
            subdivision_id = c["state_id"]
        else:
            subdivision_id = None
        result.append([
            c["id"],
            c["name"],
            subdivision_id,
            c["country_code"],
            to_coord(c.get("latitude")),
            to_coord(c.get("longitude")),
            to_int(c.get("population")),
            c.get("type"),
        ])
    return result


def write(name, payload):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    raw = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    target = OUTPUT / name
    with gzip.GzipFile(target, "wb", compresslevel=9, mtime=0) as handle:
        handle.write(raw)
    print(f"{target} {target.stat().st_size} bytes")


def main():
    states = fetch(f"{BASE}/states.json")
    counties = fetch(f"{BASE}/counties.json")
    cities = fetch(CITIES)
    subdivisions = build_subdivisions(states, counties)
    write("subdivisions.json.gz", subdivisions)
    write("cities.json.gz", build_cities(cities, {s["id"] for s in states}, {c["id"] for c in counties}))
    print(f"subdivisions={len(subdivisions)} cities={len(cities)}")


if __name__ == "__main__":
    sys.exit(main())
