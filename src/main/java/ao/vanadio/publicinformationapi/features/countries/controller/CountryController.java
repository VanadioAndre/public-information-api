package ao.vanadio.publicinformationapi.features.countries.controller;

import ao.vanadio.publicinformationapi.features.countries.dto.request.CountryFilter;
import ao.vanadio.publicinformationapi.features.countries.dto.response.CountryListResponse;
import ao.vanadio.publicinformationapi.features.countries.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data/countries")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public CountryListResponse getCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String countryName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String phoneCode,
            Locale locale
    ) {
        CountryFilter filter = new CountryFilter(countryCode, countryName, region, phoneCode);
        return new CountryListResponse(countryService.getCountries(page, size, filter, locale));
    }
}