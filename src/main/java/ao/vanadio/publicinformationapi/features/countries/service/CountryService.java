package ao.vanadio.publicinformationapi.features.countries.service;

import ao.vanadio.publicinformationapi.features.countries.dto.source.CountryJson;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
import ao.vanadio.publicinformationapi.features.countries.mapper.CountryMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

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

    public List<CountryResponse> getAllCountries(int page, int size, Locale locale) {
        int start = page * size;
        int end = Math.min(start + size, countries.size());

        return countries.subList(start, end)
                .stream()
                .map(country -> countryMapper.toResponse(country, locale))
                .toList();
    }
}