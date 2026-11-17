package ao.vanadio.publicinformationapi.features.geo.seed;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"id", "name", "subdivisionId", "countryCode", "latitude", "longitude", "population", "type"})
public record CitySeed(
        long id,
        String name,
        Long subdivisionId,
        String countryCode,
        Double latitude,
        Double longitude,
        Long population,
        String type
) {}
