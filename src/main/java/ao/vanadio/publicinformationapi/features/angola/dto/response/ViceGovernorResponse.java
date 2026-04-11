package ao.vanadio.publicinformationapi.features.angola.dto.response;

public record ViceGovernorResponse(
        String name,
        String sector,
        String appointmentDate,
        String imageUrl
) {}