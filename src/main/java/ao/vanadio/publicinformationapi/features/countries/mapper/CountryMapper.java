package ao.vanadio.publicinformationapi.features.countries.mapper;

import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryDetailResponse;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
import ao.vanadio.publicinformationapi.features.countries.dto.source.CountryJson;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionLevelResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CountryMapper {

    private static final Map<String, String> ISO_639_1_TO_3 = Map.ofEntries(
            Map.entry("af", "afr"), Map.entry("ar", "ara"), Map.entry("az", "aze"),
            Map.entry("be", "bel"), Map.entry("bg", "bul"), Map.entry("bn", "ben"),
            Map.entry("br", "bre"), Map.entry("bs", "bos"), Map.entry("ca", "cat"),
            Map.entry("cs", "ces"), Map.entry("cy", "cym"), Map.entry("da", "dan"),
            Map.entry("de", "deu"), Map.entry("el", "ell"), Map.entry("en", "eng"),
            Map.entry("eo", "epo"), Map.entry("es", "spa"), Map.entry("et", "est"),
            Map.entry("eu", "eus"), Map.entry("fa", "per"), Map.entry("fi", "fin"),
            Map.entry("fr", "fra"), Map.entry("ga", "gle"), Map.entry("gl", "glg"),
            Map.entry("gu", "guj"), Map.entry("he", "heb"), Map.entry("hi", "hin"),
            Map.entry("hr", "hrv"), Map.entry("hu", "hun"), Map.entry("hy", "hye"),
            Map.entry("id", "ind"), Map.entry("is", "isl"), Map.entry("it", "ita"),
            Map.entry("ja", "jpn"), Map.entry("ka", "kat"), Map.entry("kk", "kaz"),
            Map.entry("km", "khm"), Map.entry("ko", "kor"), Map.entry("lt", "lit"),
            Map.entry("lv", "lav"), Map.entry("mk", "mkd"), Map.entry("ml", "mal"),
            Map.entry("mn", "mon"), Map.entry("mr", "mar"), Map.entry("ms", "msa"),
            Map.entry("mt", "mlt"), Map.entry("nl", "nld"), Map.entry("no", "nor"),
            Map.entry("pa", "pan"), Map.entry("pl", "pol"), Map.entry("ps", "pus"),
            Map.entry("pt", "por"), Map.entry("ro", "ron"), Map.entry("ru", "rus"),
            Map.entry("sk", "slk"), Map.entry("sl", "slv"), Map.entry("sq", "sqi"),
            Map.entry("sr", "srp"), Map.entry("sv", "swe"), Map.entry("sw", "swa"),
            Map.entry("ta", "tam"), Map.entry("te", "tel"), Map.entry("th", "tha"),
            Map.entry("tr", "tur"), Map.entry("uk", "ukr"), Map.entry("ur", "urd"),
            Map.entry("uz", "uzb"), Map.entry("vi", "vie"), Map.entry("zh", "zho")
    );

    public CountryResponse toResponse(CountryJson c, Locale locale) {
        CountryJson.Translation name = localizedName(c, locale);
        CountryJson.Currency currency = firstCurrency(c);
        return new CountryResponse(
                c.cca2(),
                c.cca3(),
                name.common(),
                name.official(),
                languages(c),
                currency != null ? currency.name() : null,
                currency != null ? currency.symbol() : null,
                c.flag(),
                phoneCode(c),
                c.region(),
                c.subregion(),
                firstCapital(c),
                c.borders() != null ? c.borders() : List.of()
        );
    }

    public CountryDetailResponse toDetail(
            CountryJson c, Locale locale, List<SubdivisionLevelResponse> subdivisionLevels, Long cityCount) {
        CountryJson.Translation name = localizedName(c, locale);
        CountryJson.Currency currency = firstCurrency(c);
        boolean hasLatLng = c.latlng() != null && c.latlng().size() == 2;
        return new CountryDetailResponse(
                c.cca2(),
                c.cca3(),
                c.ccn3(),
                name.common(),
                name.official(),
                c.capital() != null ? c.capital() : List.of(),
                c.region(),
                c.subregion(),
                c.area(),
                hasLatLng ? c.latlng().get(0) : null,
                hasLatLng ? c.latlng().get(1) : null,
                c.landlocked(),
                c.independent(),
                c.unMember(),
                languages(c),
                currency != null ? currency.name() : null,
                currency != null ? currency.symbol() : null,
                phoneCode(c),
                demonym(c, locale),
                c.tld() != null ? c.tld() : List.of(),
                c.flag(),
                c.borders() != null ? c.borders() : List.of(),
                subdivisionLevels,
                cityCount
        );
    }

    private CountryJson.Translation localizedName(CountryJson c, Locale locale) {
        String lang = iso3(locale);
        if (c.translations() != null && c.translations().containsKey(lang)) {
            return c.translations().get(lang);
        }
        if (c.name().nativeNames() != null && c.name().nativeNames().containsKey(lang)) {
            return c.name().nativeNames().get(lang);
        }
        return new CountryJson.Translation(c.name().official(), c.name().common());
    }

    private String demonym(CountryJson c, Locale locale) {
        if (c.demonyms() == null || c.demonyms().isEmpty()) return null;
        CountryJson.Demonym demonym = c.demonyms().getOrDefault(iso3(locale), c.demonyms().get("eng"));
        if (demonym == null) demonym = c.demonyms().values().iterator().next();
        return demonym.m() != null ? demonym.m() : demonym.f();
    }

    private String iso3(Locale locale) {
        String iso1 = locale != null ? locale.getLanguage() : "";
        return ISO_639_1_TO_3.getOrDefault(iso1, iso1);
    }

    private CountryJson.Currency firstCurrency(CountryJson c) {
        if (c.currencies() == null || c.currencies().isEmpty()) return null;
        return c.currencies().values().iterator().next();
    }

    private String firstCapital(CountryJson c) {
        return c.capital() != null && !c.capital().isEmpty() ? c.capital().get(0) : null;
    }

    private List<String> languages(CountryJson c) {
        return c.languages() != null ? c.languages().values().stream().toList() : List.of();
    }

    private String phoneCode(CountryJson c) {
        if (c.idd() == null || c.idd().root() == null || c.idd().suffixes() == null || c.idd().suffixes().isEmpty()) {
            return null;
        }
        return c.idd().root() + c.idd().suffixes().get(0);
    }
}
