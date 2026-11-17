package ao.vanadio.publicinformationapi.features.geo.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubdivisionSeed(
        long id,
        Long parentId,
        String countryCode,
        int level,
        String code,
        String isoCode,
        String name,
        String nativeName,
        String type,
        Double latitude,
        Double longitude,
        Long population,
        String timezone,
        String wikiDataId,
        Map<String, String> translations
) {}
