package ao.vanadio.publicinformationapi.features.geo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeaderResponse(
        String role,
        String name,
        String sector,
        String appointmentDate,
        String imageUrl
) {}
