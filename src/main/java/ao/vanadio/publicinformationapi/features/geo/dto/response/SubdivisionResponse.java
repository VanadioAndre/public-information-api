package ao.vanadio.publicinformationapi.features.geo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubdivisionResponse(
        long id,
        String countryCode,
        String code,
        String isoCode,
        String name,
        String nativeName,
        String type,
        int level,
        SubdivisionSummaryResponse parent,
        Double latitude,
        Double longitude,
        Long population,
        String timezone,
        String wikiDataId,
        String capital,
        String area,
        String foundingDate,
        String languages,
        String ethnicities,
        String source,
        List<LeaderResponse> leaders,
        Map<String, String> translations,
        long cityCount,
        int childCount,
        List<SubdivisionSummaryResponse> children
) {}
