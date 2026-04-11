package ao.vanadio.publicinformationapi.features.angola.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProvinceResponse(
        String name,
        String capital,
        String languages,
        String area,
        String ethnicities,
        String foundingDate,
        int municipalityCount,
        GovernorResponse governor,
        List<ViceGovernorResponse> viceGovernors,
        List<MunicipalityResponse> municipalities
) {}