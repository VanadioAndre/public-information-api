package ao.vanadio.publicinformationapi.features.countries.controller;

import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryResponse;
import ao.vanadio.publicinformationapi.features.countries.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data/countries")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public List<CountryResponse> getCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Locale locale
    ) {
        return countryService.getAllCountries(page, size, locale);
    }
}