package ao.vanadio.publicinformationapi.features.geo.service;

import ao.vanadio.publicinformationapi.bootstrap.cache.CacheNames;
import ao.vanadio.publicinformationapi.bootstrap.web.NotFoundException;
import ao.vanadio.publicinformationapi.bootstrap.web.PageQuery;
import ao.vanadio.publicinformationapi.bootstrap.web.PageResponse;
import ao.vanadio.publicinformationapi.features.countries.service.CountryService;
import ao.vanadio.publicinformationapi.features.geo.dto.request.CityFilter;
import ao.vanadio.publicinformationapi.features.geo.dto.request.SubdivisionFilter;
import ao.vanadio.publicinformationapi.features.geo.dto.response.CityResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionLevelResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionResponse;
import ao.vanadio.publicinformationapi.features.geo.dto.response.SubdivisionSummaryResponse;
import ao.vanadio.publicinformationapi.features.geo.repository.GeoRepository;
import ao.vanadio.publicinformationapi.features.geo.repository.GeoRepository.Lang;
import ao.vanadio.publicinformationapi.features.geo.repository.GeoRepository.SubdivisionRow;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoRepository geoRepository;
    private final CountryService countryService;

    public String resolveCountryCode(String code) {
        return countryService.requireCountry(code).cca2().toUpperCase(Locale.ROOT);
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'levels', #countryCode}")
    public List<SubdivisionLevelResponse> getLevelSummary(String countryCode) {
        Map<Integer, long[]> counts = new LinkedHashMap<>();
        Map<Integer, Set<String>> types = new LinkedHashMap<>();
        for (GeoRepository.LevelCount row : geoRepository.countByLevel(countryCode)) {
            counts.computeIfAbsent(row.level(), k -> new long[1])[0] += row.count();
            if (row.type() != null) types.computeIfAbsent(row.level(), k -> new LinkedHashSet<>()).add(row.type());
        }
        List<SubdivisionLevelResponse> result = new ArrayList<>();
        counts.forEach((level, count) -> result.add(
                new SubdivisionLevelResponse(level, count[0], List.copyOf(types.getOrDefault(level, Set.of())))));
        return result;
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'cityCount', #countryCode}")
    public long countCities(String countryCode) {
        return geoRepository.countCities(countryCode, null);
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'subdivisions', #code, #filter, #page, #locale}")
    public PageResponse<SubdivisionSummaryResponse> getSubdivisions(
            String code, SubdivisionFilter filter, PageQuery page, Locale locale) {
        String countryCode = resolveCountryCode(code);
        Lang lang = Lang.of(locale);
        Long parentId = filter.parentCode() == null ? null : requireSubdivision(countryCode, filter.parentCode(), lang).id();
        Integer level = filter.level();
        if (level == null && parentId == null && filter.name() == null && filter.type() == null) level = 1;
        GeoRepository.Page<SubdivisionSummaryResponse> result = geoRepository.findSubdivisions(
                countryCode, level, parentId, filter.type(), filter.name(), page, lang);
        return PageResponse.of(result.content(), page, result.total());
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'subdivision', #code, #reference, #locale}")
    public SubdivisionResponse getSubdivision(String code, String reference, Locale locale) {
        String countryCode = resolveCountryCode(code);
        Lang lang = Lang.of(locale);
        SubdivisionRow s = requireSubdivision(countryCode, reference, lang);
        SubdivisionSummaryResponse parent = s.parentId() == null ? null
                : geoRepository.findSummary(s.parentId(), lang).orElse(null);
        List<SubdivisionSummaryResponse> children = geoRepository.findChildren(s.id(), lang);
        Map<String, String> translations = geoRepository.findTranslations(s.id());
        return new SubdivisionResponse(
                s.id(), s.countryCode(), s.code(), s.isoCode(), s.name(), s.nativeName(), s.type(), s.level(),
                parent, s.latitude(), s.longitude(), s.population(), s.timezone(), s.wikiDataId(), s.capital(),
                s.area(), s.foundingDate(), s.languages(), s.ethnicities(), s.source(),
                geoRepository.findLeaders(s.id()), translations.isEmpty() ? null : translations,
                geoRepository.countCities(countryCode, s.id()), children.size(), children
        );
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'children', #code, #reference, #page, #locale}")
    public PageResponse<SubdivisionSummaryResponse> getChildren(
            String code, String reference, PageQuery page, Locale locale) {
        String countryCode = resolveCountryCode(code);
        Lang lang = Lang.of(locale);
        SubdivisionRow parent = requireSubdivision(countryCode, reference, lang);
        GeoRepository.Page<SubdivisionSummaryResponse> result = geoRepository.findSubdivisions(
                countryCode, null, parent.id(), null, null, page, lang);
        return PageResponse.of(result.content(), page, result.total());
    }

    @Cacheable(cacheNames = CacheNames.GEO, key = "{'cities', #code, #filter, #page, #locale}")
    public PageResponse<CityResponse> getCities(String code, CityFilter filter, PageQuery page, Locale locale) {
        String countryCode = resolveCountryCode(code);
        Lang lang = Lang.of(locale);
        Long subdivisionId = filter.subdivisionCode() == null ? null
                : requireSubdivision(countryCode, filter.subdivisionCode(), lang).id();
        GeoRepository.Page<CityResponse> result = geoRepository.findCities(
                countryCode, subdivisionId, filter.name(), filter.minPopulation(), page, lang);
        return PageResponse.of(result.content(), page, result.total());
    }

    private SubdivisionRow requireSubdivision(String countryCode, String reference, Lang lang) {
        return geoRepository.findSubdivision(countryCode, reference.strip(), lang)
                .orElseThrow(() -> new NotFoundException(
                        "Subdivision not found: " + reference + " in country " + countryCode));
    }
}
