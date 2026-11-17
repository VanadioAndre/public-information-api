package ao.vanadio.publicinformationapi.features.geo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubdivisionSummaryResponse(
        long id,
        String code,
        String isoCode,
        String name,
        String type,
        int level,
        Long parentId,
        Double latitude,
        Double longitude,
        Long population
) {}
