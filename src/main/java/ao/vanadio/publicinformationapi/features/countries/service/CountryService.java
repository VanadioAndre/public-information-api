package ao.vanadio.publicinformationapi.features.countries.service;

import ao.vanadio.publicinformationapi.bootstrap.cache.CacheNames;
import ao.vanadio.publicinformationapi.features.countries.dto.request.CountryFilter;
import ao.vanadio.publicinformationapi.features.countries.dto.source.CountryJson;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
import ao.vanadio.publicinformationapi.features.countries.mapper.CountryMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final ObjectMapper objectMapper;
    private final CountryMapper countryMapper;

    private List<CountryJson> countries;

    @PostConstruct
    void loadCountries() throws Exception {
        InputStream inputStream = new ClassPathResource("data/countries.json").getInputStream();
        countries = objectMapper.readValue(inputStream, new TypeReference<>() {});
    }

    @Cacheable(
            cacheNames = CacheNames.COUNTRIES,
            key = "#page + ':' + #size + ':' + #filter + ':' + #locale"
    )
    public List<CountryResponse> getCountries(int page, int size, CountryFilter filter, Locale locale) {
//        log.debug("Cache miss — computing countries [page={}, size={}, filter={}, locale={}]", page, size, filter, locale);

        return applyPagination(applyFilters(countries, filter), page, size)
                .stream()
                .map(country -> countryMapper.toResponse(country, locale))
                .toList();
    }

    private List<CountryJson> applyFilters(List<CountryJson> source, CountryFilter filter) {
        if (filter.isEmpty()) return source;

        return source.stream()
                .filter(c -> matchesCode(c, filter.countryCode()))
                .filter(c -> matchesName(c, filter.countryName()))
                .filter(c -> matchesRegion(c, filter.region()))
                .filter(c -> matchesPhoneCode(c, filter.phoneCode()))
                .toList();
    }

    private boolean matchesCode(CountryJson country, String code) {
        if (code == null) return true;
        if (code.length() > 2){
            return code.equalsIgnoreCase(country.cca3());
        }
        return code.equalsIgnoreCase(country.cca2());
    }

    private boolean matchesName(CountryJson country, String name) {
        if (name == null) return true;
        String lowerName = name.toLowerCase();
        return country.name().common().toLowerCase().contains(lowerName)
                || country.name().official().toLowerCase().contains(lowerName);
    }

    private boolean matchesRegion(CountryJson country, String region) {
        if (region == null) return true;
        return region.equalsIgnoreCase(country.region());
    }

    private boolean matchesPhoneCode(CountryJson country, String phoneCode) {
        if (phoneCode == null) return true;
        String resolved = resolvePhoneCode(country);
        if (resolved == null) return false;
        return normalizePhoneCode(phoneCode).equals(normalizePhoneCode(resolved));
    }

    private String normalizePhoneCode(String phoneCode) {
        return phoneCode.replace("+", "").strip();
    }

    private String resolvePhoneCode(CountryJson country) {
        var idd = country.idd();
        if (idd == null || idd.root() == null
                || idd.suffixes() == null || idd.suffixes().isEmpty()) {
            return null;
        }
        return country.idd().root() + country.idd().suffixes().get(0);
    }


    private List<CountryJson> applyPagination(List<CountryJson> source, int page, int size) {
        int start = page * size;
        if (start >= source.size()) return List.of();
        int end = Math.min(start + size, source.size());
        return source.subList(start, end);
    }
}