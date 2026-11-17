package ao.vanadio.publicinformationapi.features.geo.seed;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOnDatabaseInitialization
@ConditionalOnProperty(name = "app.geo.seed.enabled", havingValue = "true", matchIfMissing = true)
public class GeoDataSeeder implements InitializingBean {

    static final String SUBDIVISIONS_FILE = "data/geo/subdivisions.json.gz";
    static final String CITIES_FILE = "data/geo/cities.json.gz";
    static final String SOURCE = "dr5hn";
    private static final int BATCH_SIZE = 5_000;

    private static final String INSERT_SUBDIVISION = """
            INSERT INTO subdivision (id, parent_id, country_code, level, code, iso_code, name, name_lower,
                native_name, type, latitude, longitude, population, timezone, wiki_data_id, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERT_TRANSLATION =
            "INSERT INTO subdivision_translation (subdivision_id, lang, name) VALUES (?, ?, ?)";
    private static final String INSERT_CITY = """
            INSERT INTO city (id, subdivision_id, country_code, name, name_lower, latitude, longitude, population, type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final AngolaGeoSeeder angolaGeoSeeder;

    @Override
    public void afterPropertiesSet() throws IOException {
        long start = System.currentTimeMillis();
        Map<Long, String> overriddenSubdivisionCodes = new HashMap<>();
        int subdivisions = seedSubdivisions(overriddenSubdivisionCodes);
        Map<String, Long> overrideTargets = angolaGeoSeeder.seed();
        int cities = seedCities(overriddenSubdivisionCodes, overrideTargets);
        log.info("Geo data seeded: {} subdivisions, {} cities in {} ms",
                subdivisions, cities, System.currentTimeMillis() - start);
    }

    private int seedSubdivisions(Map<Long, String> overriddenSubdivisionCodes) throws IOException {
        Batch subdivisions = new Batch(INSERT_SUBDIVISION);
        Batch translations = new Batch(INSERT_TRANSLATION);
        int count = 0;
        try (MappingIterator<SubdivisionSeed> it = read(SUBDIVISIONS_FILE, SubdivisionSeed.class)) {
            while (it.hasNext()) {
                SubdivisionSeed s = it.next();
                if (angolaGeoSeeder.overrides(s.countryCode())) {
                    if (s.isoCode() != null) overriddenSubdivisionCodes.put(s.id(), s.isoCode());
                    continue;
                }
                subdivisions.add(new Object[]{
                        s.id(), s.parentId(), s.countryCode(), s.level(), s.code(), s.isoCode(), s.name(),
                        lower(s.name()), s.nativeName(), s.type(), s.latitude(), s.longitude(), s.population(),
                        s.timezone(), s.wikiDataId(), SOURCE
                });
                if (s.translations() != null) {
                    s.translations().forEach((lang, name) -> {
                        if (name != null && !name.isBlank()) translations.add(new Object[]{s.id(), lang, name});
                    });
                }
                count++;
            }
        }
        subdivisions.flush();
        translations.flush();
        return count;
    }

    private int seedCities(Map<Long, String> overriddenSubdivisionCodes, Map<String, Long> overrideTargets) throws IOException {
        Batch cities = new Batch(INSERT_CITY);
        int count = 0;
        try (MappingIterator<CitySeed> it = read(CITIES_FILE, CitySeed.class)) {
            while (it.hasNext()) {
                CitySeed c = it.next();
                Long subdivisionId = c.subdivisionId();
                if (angolaGeoSeeder.overrides(c.countryCode())) {
                    subdivisionId = subdivisionId == null ? null
                            : overrideTargets.get(overriddenSubdivisionCodes.get(subdivisionId));
                }
                cities.add(new Object[]{
                        c.id(), subdivisionId, c.countryCode(), c.name(), lower(c.name()),
                        c.latitude(), c.longitude(), c.population(), c.type()
                });
                count++;
            }
        }
        cities.flush();
        return count;
    }

    private <T> MappingIterator<T> read(String path, Class<T> type) throws IOException {
        InputStream input = new GZIPInputStream(new ClassPathResource(path).getInputStream(), 1 << 16);
        return objectMapper.readerFor(type).readValues(input);
    }

    static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private final class Batch {
        private final String sql;
        private final List<Object[]> rows = new ArrayList<>(BATCH_SIZE);

        private Batch(String sql) {
            this.sql = sql;
        }

        void add(Object[] row) {
            rows.add(row);
            if (rows.size() >= BATCH_SIZE) flush();
        }

        void flush() {
            if (rows.isEmpty()) return;
            jdbcTemplate.batchUpdate(sql, rows);
            rows.clear();
        }
    }
}
