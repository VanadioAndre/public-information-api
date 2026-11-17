package ao.vanadio.publicinformationapi.features.countries.dto.response;

import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionLevelResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryDetailResponse(
        String countryCode,
        String alpha3Code,
        String numericCode,
        String name,
        String officialName,
        List<String> capitals,
        String region,
        String subregion,
        Double area,
        Double latitude,
        Double longitude,
        Boolean landlocked,
        Boolean independent,
        Boolean unMember,
        List<String> languages,
        String currency,
        String currencySymbol,
        String phoneCode,
        String demonym,
        List<String> topLevelDomains,
        String flag,
        List<String> borders,
        List<SubdivisionLevelResponse> subdivisionLevels,
        Long cityCount
) {}
