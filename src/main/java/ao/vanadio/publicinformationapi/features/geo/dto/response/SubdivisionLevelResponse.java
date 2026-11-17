package ao.vanadio.publicinformationapi.features.geo.dto.response;

import java.util.List;

public record SubdivisionLevelResponse(
        int level,
        long count,
        List<String> types
) {}
