package ao.vanadio.publicinformationapi.features.angola.service;

import ao.vanadio.publicinformationapi.bootstrap.cache.CacheNames;
import ao.vanadio.publicinformationapi.features.angola.dto.request.AngolaFilter;
import ao.vanadio.publicinformationapi.features.angola.dto.response.ProvinceResponse;
import ao.vanadio.publicinformationapi.features.angola.dto.source.AngolaJson;
import ao.vanadio.publicinformationapi.features.angola.dto.source.ProvinceJson;
import ao.vanadio.publicinformationapi.features.angola.mapper.AngolaMapper;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AngolaService {

    private final ObjectMapper objectMapper;
    private final AngolaMapper angolaMapper;

    private List<ProvinceJson> provinces;

    @PostConstruct
    void loadProvinces() throws Exception {
        InputStream inputStream = new ClassPathResource("data/angola.json").getInputStream();
        AngolaJson wrapper = objectMapper.readValue(inputStream, AngolaJson.class);
        provinces = wrapper.provinces();
    }

    @Cacheable(
            cacheNames = CacheNames.ANGOLA,
            key = "#page + ':' + #size + ':' + #filter"
    )
    public List<ProvinceResponse> getProvinces(int page, int size, AngolaFilter filter) {
        return applyPagination(applyFilters(provinces, filter), page, size)
                .stream()
                .map(angolaMapper::toResponse)
                .toList();
    }

    private List<ProvinceJson> applyFilters(List<ProvinceJson> source, AngolaFilter filter) {
        if (filter.isEmpty()) return source;

        return source.stream()
                .filter(p -> matchesProvinceName(p, filter.provinceName()))
                .filter(p -> matchesCapital(p, filter.capital()))
                .filter(p -> matchesMunicipalityName(p, filter.municipalityName()))
                .toList();
    }

    private boolean matchesProvinceName(ProvinceJson province, String name) {
        if (name == null) return true;
        return province.name().toLowerCase().contains(name.toLowerCase());
    }

    private boolean matchesCapital(ProvinceJson province, String capital) {
        if (capital == null) return true;
        return capital.equalsIgnoreCase(province.capital());
    }

    private boolean matchesMunicipalityName(ProvinceJson province, String municipalityName) {
        if (municipalityName == null) return true;
        if (province.municipalities() == null) return false;
        return province.municipalities().stream()
                .anyMatch(m -> m.name().toLowerCase().contains(municipalityName.toLowerCase()));
    }

    private List<ProvinceJson> applyPagination(List<ProvinceJson> source, int page, int size) {
        int start = page * size;
        if (start >= source.size()) return List.of();
        int end = Math.min(start + size, source.size());
        return source.subList(start, end);
    }
}