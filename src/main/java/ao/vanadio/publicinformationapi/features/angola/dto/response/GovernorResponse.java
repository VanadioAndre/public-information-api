package ao.vanadio.publicinformationapi.features.angola.dto.response;

public record GovernorResponse(
        String name,
        String appointmentDate,
        String imageUrl
) {}