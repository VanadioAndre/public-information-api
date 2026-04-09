package ao.vanadio.publicinformationapi.features.countries.dto.response;

import java.util.List;

public record CountryListResponse(
        List<CountryResponse> countries
) {}