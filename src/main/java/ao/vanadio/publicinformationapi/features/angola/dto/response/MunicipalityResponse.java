package ao.vanadio.publicinformationapi.features.angola.dto.response;

import java.util.List;

public record MunicipalityResponse(
        String name,
        String administrator,
        String foundingDate,
        int comunaCount,
        List<String> comunas
) {}