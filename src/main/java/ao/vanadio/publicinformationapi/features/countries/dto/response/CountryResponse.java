package ao.vanadio.publicinformationapi.features.countries.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryResponse(
        String name,
        String officialName,
        List<String> languages,
        String currency,
        String currencySymbol,
        String flag,
        String phoneCode,
        String region,
        List<String> borders,
        String countryCode
) {}