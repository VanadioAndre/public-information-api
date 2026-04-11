package ao.vanadio.publicinformationapi.features.countries.dto.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryJson(
        Name name,
        Map<String, Currency> currencies,
        Idd idd,
        Map<String, String> languages,
        String region,
        List<String> borders,
        String flag,
        String cca2,
        String cca3,
        Map<String, Translation> translations
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(
            String common,
            String official,
            Map<String, Translation> nativeNames
    ) {}

    public record Currency(
            String name,
            String symbol
    ) {}

    public record Idd(
            String root,
            List<String> suffixes
    ) {}

    public record Translation(
            String official,
            String common
    ) {}
}