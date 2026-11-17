package ao.vanadio.publicinformationapi.features.countries.dto.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryJson(
        Name name,
        Map<String, Currency> currencies,
        Idd idd,
        Map<String, String> languages,
        String region,
        String subregion,
        List<String> capital,
        Double area,
        List<Double> latlng,
        Boolean landlocked,
        Boolean independent,
        Boolean unMember,
        List<String> tld,
        Map<String, Demonym> demonyms,
        List<String> borders,
        String flag,
        String cca2,
        String cca3,
        String ccn3,
        Map<String, Translation> translations
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(
            String common,
            String official,
            @JsonProperty("native") Map<String, Translation> nativeNames
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Currency(
            String name,
            String symbol
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Idd(
            String root,
            List<String> suffixes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Translation(
            String official,
            String common
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Demonym(
            String f,
            String m
    ) {}
}
