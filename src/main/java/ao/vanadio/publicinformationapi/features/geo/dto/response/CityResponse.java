package ao.vanadio.publicinformationapi.features.geo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CityResponse(
        long id,
        String name,
        String countryCode,
        Long subdivisionId,
        String subdivisionName,
        Double latitude,
        Double longitude,
        Long population,
        String type
) {}
