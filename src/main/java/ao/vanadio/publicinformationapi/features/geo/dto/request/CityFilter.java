package ao.vanadio.publicinformationapi.features.geo.dto.request;

public record CityFilter(
        String subdivisionCode,
        String name,
        Long minPopulation
) {}
