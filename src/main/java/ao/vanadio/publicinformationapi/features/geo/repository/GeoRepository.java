package ao.vanadio.publicinformationapi.features.geo.repository;

import ao.vanadio.publicinformationapi.bootstrap.web.PageQuery;
import ao.vanadio.publicinformationapi.features.geo.dto.response.CityResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.LeaderResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GeoRepository {

    private static final String LOCALIZED_NAME = "COALESCE(t1.name, t2.name, s.name)";
    private static final String TRANSLATION_JOINS = """
             LEFT JOIN subdivision_translation t1 ON t1.subdivision_id = s.id AND t1.lang = :langTag
             LEFT JOIN subdivision_translation t2 ON t2.subdivision_id = s.id AND t2.lang = :lang
            """;
    private static final String SUMMARY_COLUMNS = "s.id, s.code, s.iso_code, " + LOCALIZED_NAME
            + " AS name, s.type, s.level, s.parent_id, s.latitude, s.longitude, s.population";
    private static final String DESCENDANT_FILTER = """
            c.subdivision_id IN (
                SELECT d.id FROM subdivision d WHERE d.id = :subdivisionId OR d.parent_id = :subdivisionId
                OR d.parent_id IN (SELECT m.id FROM subdivision m WHERE m.parent_id = :subdivisionId))""";

    private final JdbcClient jdbcClient;

    public record SubdivisionRow(
            long id, Long parentId, String countryCode, int level, String code, String isoCode, String name,
            String nativeName, String type, Double latitude, Double longitude, Long population, String timezone,
            String wikiDataId, String capital, String area, String foundingDate, String languages,
            String ethnicities, String source
    ) {}

    public record LevelCount(int level, String type, long count) {}

    public record Page<T>(List<T> content, long total) {}

    public record Lang(String tag, String language) {
        public static Lang of(Locale locale) {
            if (locale == null) return new Lang("", "");
            return new Lang(locale.toLanguageTag(), locale.getLanguage());
        }

        Map<String, Object> params() {
            Map<String, Object> params = new HashMap<>();
            params.put("langTag", tag);
            params.put("lang", language);
            return params;
        }
    }

    public Optional<SubdivisionRow> findSubdivision(String countryCode, String reference, Lang lang) {
        Where where = new Where().and("s.country_code = :country", "country", countryCode);
        Long id = parseId(reference);
        if (id != null) {
            where.and("s.id = :ref", "ref", id);
        } else {
            where.and("UPPER(s.iso_code) = :ref", "ref", normalizeIso(countryCode, reference));
        }
        String sql = "SELECT s.id, s.parent_id, s.country_code, s.level, s.code, s.iso_code, " + LOCALIZED_NAME
                + " AS name, s.native_name, s.type, s.latitude, s.longitude, s.population, s.timezone, s.wiki_data_id,"
                + " s.capital, s.area, s.founding_date, s.languages, s.ethnicities, s.source FROM subdivision s"
                + TRANSLATION_JOINS + where.sql();
        return jdbcClient.sql(sql).params(where.params(lang)).query(SubdivisionRow.class).optional();
    }

    public Optional<SubdivisionSummaryResponse> findSummary(long id, Lang lang) {
        String sql = "SELECT " + SUMMARY_COLUMNS + " FROM subdivision s" + TRANSLATION_JOINS + " WHERE s.id = :id";
        return jdbcClient.sql(sql).params(lang.params()).param("id", id)
                .query(SubdivisionSummaryResponse.class).optional();
    }

    public Page<SubdivisionSummaryResponse> findSubdivisions(
            String countryCode, Integer level, Long parentId, String type, String name, PageQuery page, Lang lang) {
        Where where = new Where().and("s.country_code = :country", "country", countryCode);
        if (level != null) where.and("s.level = :level", "level", level);
        if (parentId != null) where.and("s.parent_id = :parentId", "parentId", parentId);
        if (type != null) where.and("LOWER(s.type) = :type", "type", type.toLowerCase(Locale.ROOT));
        if (name != null) {
            where.and("(s.name_lower LIKE :name OR LOWER(t1.name) LIKE :name OR LOWER(t2.name) LIKE :name)",
                    "name", like(name));
        }
        return page("SELECT " + SUMMARY_COLUMNS, "FROM subdivision s" + TRANSLATION_JOINS + where.sql(),
                "ORDER BY s.level, name, s.id", where.params(lang), page, SubdivisionSummaryResponse.class);
    }

    public List<SubdivisionSummaryResponse> findChildren(long parentId, Lang lang) {
        String sql = "SELECT " + SUMMARY_COLUMNS + " FROM subdivision s" + TRANSLATION_JOINS
                + " WHERE s.parent_id = :parentId ORDER BY name, s.id";
        return jdbcClient.sql(sql).params(lang.params()).param("parentId", parentId)
                .query(SubdivisionSummaryResponse.class).list();
    }

    public List<LevelCount> countByLevel(String countryCode) {
        return jdbcClient.sql("""
                        SELECT level, type, COUNT(*) AS count FROM subdivision
                        WHERE country_code = :country GROUP BY level, type ORDER BY level, count DESC
                        """)
                .param("country", countryCode)
                .query(LevelCount.class).list();
    }

    public List<LeaderResponse> findLeaders(long subdivisionId) {
        return jdbcClient.sql("""
                        SELECT role, name, sector, appointment_date, image_url FROM subdivision_leader
                        WHERE subdivision_id = :id
                        ORDER BY CASE role WHEN 'GOVERNOR' THEN 0 WHEN 'VICE_GOVERNOR' THEN 1 ELSE 2 END, id
                        """)
                .param("id", subdivisionId)
                .query(LeaderResponse.class).list();
    }

    public Map<String, String> findTranslations(long subdivisionId) {
        Map<String, String> result = new LinkedHashMap<>();
        jdbcClient.sql("SELECT lang, name FROM subdivision_translation WHERE subdivision_id = :id ORDER BY lang")
                .param("id", subdivisionId)
                .query(rs -> {
                    result.put(rs.getString("lang"), rs.getString("name"));
                });
        return result;
    }

    public long countCities(String countryCode, Long subdivisionId) {
        Where where = new Where().and("c.country_code = :country", "country", countryCode);
        if (subdivisionId != null) where.and(DESCENDANT_FILTER, "subdivisionId", subdivisionId);
        return jdbcClient.sql("SELECT COUNT(*) FROM city c" + where.sql())
                .params(where.params(null)).query(Long.class).single();
    }

    public Page<CityResponse> findCities(
            String countryCode, Long subdivisionId, String name, Long minPopulation, PageQuery page, Lang lang) {
        Where where = new Where().and("c.country_code = :country", "country", countryCode);
        if (subdivisionId != null) where.and(DESCENDANT_FILTER, "subdivisionId", subdivisionId);
        if (name != null) where.and("c.name_lower LIKE :name", "name", like(name));
        if (minPopulation != null) where.and("c.population >= :minPopulation", "minPopulation", minPopulation);
        return page(
                "SELECT c.id, c.name, c.country_code, c.subdivision_id, " + LOCALIZED_NAME
                        + " AS subdivision_name, c.latitude, c.longitude, c.population, c.type",
                "FROM city c LEFT JOIN subdivision s ON s.id = c.subdivision_id" + TRANSLATION_JOINS + where.sql(),
                "ORDER BY c.population DESC NULLS LAST, c.name, c.id",
                where.params(lang), page, CityResponse.class);
    }

    private <T> Page<T> page(String select, String from, String orderBy, Map<String, Object> params,
                             PageQuery page, Class<T> type) {
        long total = jdbcClient.sql("SELECT COUNT(*) " + from).params(params).query(Long.class).single();
        if (total == 0 || page.offset() >= total) return new Page<>(List.of(), total);
        List<T> content = jdbcClient.sql(select + " " + from + " " + orderBy + " LIMIT :limit OFFSET :offset")
                .params(params).param("limit", page.size()).param("offset", page.offset())
                .query(type).list();
        return new Page<>(content, total);
    }

    private static Long parseId(String reference) {
        try {
            return Long.parseLong(reference);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String normalizeIso(String countryCode, String reference) {
        String upper = reference.toUpperCase(Locale.ROOT);
        return upper.startsWith(countryCode + "-") ? upper : countryCode + "-" + upper;
    }

    private static String like(String value) {
        return "%" + value.toLowerCase(Locale.ROOT).replace("%", "").replace("_", "") + "%";
    }

    private static final class Where {
        private final List<String> clauses = new ArrayList<>();
        private final Map<String, Object> params = new HashMap<>();

        Where and(String clause, String name, Object value) {
            clauses.add(clause);
            params.put(name, value);
            return this;
        }

        String sql() {
            return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
        }

        Map<String, Object> params(Lang lang) {
            Map<String, Object> result = new HashMap<>(params);
            if (lang != null) result.putAll(lang.params());
            return result;
        }
    }
}
