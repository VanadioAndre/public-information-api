package ao.vanadio.publicinformationapi.features.countries.mapper;

import ao.vanadio.publicinformationapi.features.countries.dto.source.CountryJson;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CountryMapper {

    public CountryResponse toResponse(CountryJson c, Locale locale) {
        String lang = locale.getLanguage();

        // pega nome padrão
        String name = c.name().common();
        String official = c.name().official();

        // 1️⃣ verifica translations globais
        if (c.translations() != null && c.translations().containsKey(lang)) {
            var t = c.translations().get(lang);
            name = t.common();
            official = t.official();
        }
        // 2️⃣ fallback: verifica nomes nativos
        else if (c.name().nativeNames() != null && c.name().nativeNames().containsKey(lang)) {
            var n = c.name().nativeNames().get(lang);
            name = n.common();
            official = n.official();
        }

        // moedas
        String currencyName = null;
        String currencySymbol = null;
        if (c.currencies() != null && !c.currencies().isEmpty()) {
            Map.Entry<String, CountryJson.Currency> entry = c.currencies().entrySet().iterator().next();
            currencyName = entry.getValue().name();
            currencySymbol = entry.getValue().symbol();
        }

        // telefone
        String phoneCode = null;
        if (c.idd() != null
                && c.idd().root() != null
                && c.idd().suffixes() != null
                && !c.idd().suffixes().isEmpty()) {

            phoneCode = c.idd().root() + c.idd().suffixes().get(0);
        }

        return new CountryResponse(
                name,
                official,
                c.languages() != null ? c.languages().values().stream().toList() : List.of(),
                currencyName,
                currencySymbol,
                c.flag(),
                phoneCode,
                c.region(),
                c.borders() != null ? c.borders() : List.of(),
                c.cca2()
        );
    }
}