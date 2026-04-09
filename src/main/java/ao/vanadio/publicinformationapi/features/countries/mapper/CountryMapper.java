package ao.vanadio.publicinformationapi.features.countries.mapper;

import ao.vanadio.publicinformationapi.features.countries.dto.source.CountryJson;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
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
        String iso1 = locale.getLanguage();
        String lang = ISO_639_1_TO_3.getOrDefault(iso1, iso1);

        String name = c.name().common();
        String official = c.name().official();

        if (c.translations() != null && c.translations().containsKey(lang)) {
            var t = c.translations().get(lang);
            name = t.common();
            official = t.official();
        } else if (c.name().nativeNames() != null && c.name().nativeNames().containsKey(lang)) {
            var n = c.name().nativeNames().get(lang);
            name = n.common();
            official = n.official();
        }

        String currencyName = null;
        String currencySymbol = null;
        if (c.currencies() != null && !c.currencies().isEmpty()) {
            Map.Entry<String, CountryJson.Currency> entry = c.currencies().entrySet().iterator().next();
            currencyName = entry.getValue().name();
            currencySymbol = entry.getValue().symbol();
        }

        String phoneCode = null;
        if (c.idd() != null
                && c.idd().root() != null
                && c.idd().suffixes() != null
                && !c.idd().suffixes().isEmpty()) {
            phoneCode = c.idd().root() + c.idd().suffixes().get(0);
        }

        return new CountryResponse(
                c.cca2(),
                name,
                official,
                c.languages() != null ? c.languages().values().stream().toList() : List.of(),
                currencyName,
                currencySymbol,
                c.flag(),
                phoneCode,
                c.region(),
                c.borders() != null ? c.borders() : List.of()
        );
    }
}