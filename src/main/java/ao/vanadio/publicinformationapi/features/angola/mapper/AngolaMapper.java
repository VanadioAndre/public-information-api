package ao.vanadio.publicinformationapi.features.angola.mapper;

import ao.vanadio.publicinformationapi.features.angola.dto.source.ProvinceJson;
import ao.vanadio.publicinformationapi.features.angola.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AngolaMapper {

    public ProvinceResponse toResponse(ProvinceJson p) {
        return new ProvinceResponse(
                p.name(),
                p.capital(),
                p.languages(),
                p.area(),
                p.ethnicities(),
                p.foundingDate(),
                p.municipalityCount(),
                toGovernorResponse(p.governor()),
                toViceGovernorResponses(p.viceGovernors()),
                toMunicipalityResponses(p.municipalities())
        );
    }

    private GovernorResponse toGovernorResponse(ProvinceJson.Governor g) {
        if (g == null) return null;
        return new GovernorResponse(g.name(), g.appointmentDate(), g.imageUrl());
    }

    private List<ViceGovernorResponse> toViceGovernorResponses(List<ProvinceJson.ViceGovernor> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(v -> new ViceGovernorResponse(v.name(), v.sector(), v.appointmentDate(), v.imageUrl()))
                .toList();
    }

    private List<MunicipalityResponse> toMunicipalityResponses(List<ProvinceJson.Municipality> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(m -> new MunicipalityResponse(
                        m.name(),
                        m.administrator(),
                        m.foundingDate(),
                        m.comunaCount(),
                        m.comunas() != null ? m.comunas() : List.of()
                ))
                .toList();
    }
}