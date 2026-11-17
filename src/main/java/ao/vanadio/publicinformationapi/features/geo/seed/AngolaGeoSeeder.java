package ao.vanadio.publicinformationapi.features.geo.seed;

import ao.vanadio.publicinformationapi.features.angola.dto.source.ProvinceJson;
import ao.vanadio.publicinformationapi.features.angola.service.AngolaService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AngolaGeoSeeder {

    static final String COUNTRY_CODE = "AO";
    static final String SOURCE = "angola.json";
    static final long PROVINCE_ID_BASE = 2_000_000L;
    static final long MUNICIPALITY_ID_BASE = 2_100_000L;
    static final long COMMUNE_ID_BASE = 2_200_000L;

    private static final Set<String> LOWERCASE_WORDS = Set.of("e", "de", "do", "da", "dos", "das");

    private static final String INSERT_SUBDIVISION = """
            INSERT INTO subdivision (id, parent_id, country_code, level, code, iso_code, name, name_lower, type,
                population, capital, area, founding_date, languages, ethnicities, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERT_LEADER = """
            INSERT INTO subdivision_leader (subdivision_id, role, name, sector, appointment_date, image_url)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final AngolaService angolaService;
    private final JdbcTemplate jdbcTemplate;

    public boolean overrides(String countryCode) {
        return COUNTRY_CODE.equals(countryCode);
    }

    public Map<String, Long> seed() {
        List<Object[]> subdivisions = new ArrayList<>();
        List<Object[]> leaders = new ArrayList<>();
        Map<String, Long> provinceIdsByIsoCode = new HashMap<>();
        long municipalityId = MUNICIPALITY_ID_BASE;
        long communeId = COMMUNE_ID_BASE;

        for (ProvinceJson p : angolaService.getRawProvinces()) {
            long provinceId = PROVINCE_ID_BASE + p.id();
            String name = displayName(p.name());
            subdivisions.add(new Object[]{
                    provinceId, null, COUNTRY_CODE, 1, shortCode(p.isoCode()), blankToNull(p.isoCode()), name,
                    GeoDataSeeder.lower(name), "province", p.population(), displayName(p.capital()),
                    blankToNull(p.area()), blankToNull(p.foundingDate()), blankToNull(p.languages()),
                    blankToNull(p.ethnicities()), SOURCE
            });
            if (p.isoCode() != null && !p.isoCode().isBlank()) provinceIdsByIsoCode.put(p.isoCode(), provinceId);
            addGovernor(leaders, provinceId, p.governor());
            addViceGovernors(leaders, provinceId, p.viceGovernors());

            if (p.municipalities() == null) continue;
            for (ProvinceJson.Municipality m : p.municipalities()) {
                long currentMunicipality = ++municipalityId;
                String municipalityName = displayName(m.name());
                subdivisions.add(new Object[]{
                        currentMunicipality, provinceId, COUNTRY_CODE, 2, null, null, municipalityName,
                        GeoDataSeeder.lower(municipalityName), "municipality", null, null, null,
                        blankToNull(m.foundingDate()), null, null, SOURCE
                });
                if (m.administrator() != null && !m.administrator().isBlank()) {
                    leaders.add(new Object[]{currentMunicipality, "ADMINISTRATOR", displayName(m.administrator()), null, null, null});
                }
                if (m.comunas() == null) continue;
                for (String commune : m.comunas()) {
                    if (commune == null || commune.isBlank()) continue;
                    String communeName = displayName(commune);
                    subdivisions.add(new Object[]{
                            ++communeId, currentMunicipality, COUNTRY_CODE, 3, null, null, communeName,
                            GeoDataSeeder.lower(communeName), "commune", null, null, null, null, null, null, SOURCE
                    });
                }
            }
        }

        jdbcTemplate.batchUpdate(INSERT_SUBDIVISION, subdivisions);
        jdbcTemplate.batchUpdate(INSERT_LEADER, leaders);
        return provinceIdsByIsoCode;
    }

    private void addGovernor(List<Object[]> leaders, long provinceId, ProvinceJson.Governor governor) {
        if (governor == null || governor.name() == null || governor.name().isBlank()) return;
        leaders.add(new Object[]{provinceId, "GOVERNOR", displayName(governor.name()), null,
                blankToNull(governor.appointmentDate()), blankToNull(governor.imageUrl())});
    }

    private void addViceGovernors(List<Object[]> leaders, long provinceId, List<ProvinceJson.ViceGovernor> viceGovernors) {
        if (viceGovernors == null) return;
        for (ProvinceJson.ViceGovernor v : viceGovernors) {
            if (v.name() == null || v.name().isBlank()) continue;
            leaders.add(new Object[]{provinceId, "VICE_GOVERNOR", displayName(v.name()), blankToNull(v.sector()),
                    blankToNull(v.appointmentDate()), blankToNull(v.imageUrl())});
        }
    }

    static String displayName(String value) {
        if (value == null || value.isBlank()) return null;
        String[] words = value.strip().toLowerCase(Locale.ROOT).split("\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(' ');
            String word = words[i];
            result.append(i > 0 && LOWERCASE_WORDS.contains(word) ? word : capitalizeParts(word));
        }
        return result.toString();
    }

    private static String capitalizeParts(String word) {
        StringBuilder result = new StringBuilder(word.length());
        boolean capitalize = true;
        for (char ch : word.toCharArray()) {
            result.append(capitalize ? Character.toUpperCase(ch) : ch);
            capitalize = ch == '-' || ch == '\'';
        }
        return result.toString();
    }

    private static String shortCode(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) return null;
        int dash = isoCode.indexOf('-');
        return dash >= 0 ? isoCode.substring(dash + 1) : isoCode;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
