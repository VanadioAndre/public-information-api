package ao.vanadio.publicinformationapi.features.geo.dto.request;

public record SubdivisionFilter(
        Integer level,
        String parentCode,
        String type,
        String name
) {}
