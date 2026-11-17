package ao.vanadio.publicinformationapi.features.geo.controller;

import ao.vanadio.publicinformationapi.bootstrap.web.PageQuery;
import ao.vanadio.publicinformationapi.bootstrap.web.PageResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.request.CityFilter;
import ao.vanadio.publicinformationapi.features.geo.dto.request.SubdivisionFilter;
import ao.vanadio.publicinformationapi.features.geo.dto.response.CityResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionSummaryResponse;
import ao.vanadio.publicinformationapi.features.geo.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data/countries/{countryCode}")
public class GeoController {

    private final GeoService geoService;

    @GetMapping("/subdivisions")
    @Operation(summary = "List administrative subdivisions (states, provinces, counties, municipalities, communes)")
    public PageResponse<SubdivisionSummaryResponse> getSubdivisions(
            @Parameter(description = "ISO 3166-1 alpha-2 or alpha-3 code") @PathVariable String countryCode,
            @Parameter(description = "Hierarchy level; defaults to 1 when no other filter is given")
            @RequestParam(required = false) Integer level,
            @Parameter(description = "ISO 3166-2 code or id of the parent subdivision")
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String type,
            @Parameter(description = "Partial, case-insensitive name match")
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Locale locale
    ) {
        return geoService.getSubdivisions(countryCode,
                new SubdivisionFilter(level, blankToNull(parentCode), blankToNull(type), blankToNull(name)),
                new PageQuery(page, size), locale);
    }

    @GetMapping("/subdivisions/{subdivisionCode}")
    @Operation(summary = "Subdivision details, leaders and direct children")
    public SubdivisionResponse getSubdivision(
            @PathVariable String countryCode,
            @Parameter(description = "ISO 3166-2 code (e.g. AO-LUA, US-CA, or just LUA) or numeric id")
            @PathVariable String subdivisionCode,
            Locale locale
    ) {
        return geoService.getSubdivision(countryCode, subdivisionCode, locale);
    }

    @GetMapping("/subdivisions/{subdivisionCode}/children")
    @Operation(summary = "Paginated direct children of a subdivision")
    public PageResponse<SubdivisionSummaryResponse> getChildren(
            @PathVariable String countryCode,
            @PathVariable String subdivisionCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Locale locale
    ) {
        return geoService.getChildren(countryCode, subdivisionCode, new PageQuery(page, size), locale);
    }

    @GetMapping("/cities")
    @Operation(summary = "List cities, ordered by population")
    public PageResponse<CityResponse> getCities(
            @PathVariable String countryCode,
            @Parameter(description = "ISO 3166-2 code or id; includes cities of nested subdivisions")
            @RequestParam(required = false) String subdivisionCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long minPopulation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Locale locale
    ) {
        return geoService.getCities(countryCode,
                new CityFilter(blankToNull(subdivisionCode), blankToNull(name), minPopulation),
                new PageQuery(page, size), locale);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
