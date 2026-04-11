package ao.vanadio.publicinformationapi.features.angola.controller;

import ao.vanadio.publicinformationapi.features.angola.dto.request.AngolaFilter;
import ao.vanadio.publicinformationapi.features.angola.dto.response.ProvinceListResponse;
import ao.vanadio.publicinformationapi.features.angola.service.AngolaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data/angola")
public class AngolaController {

    private final AngolaService angolaService;

    @GetMapping
    public ProvinceListResponse getProvinces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String provinceName,
            @RequestParam(required = false) String capital,
            @RequestParam(required = false) String municipalityName
    ) {
        AngolaFilter filter = new AngolaFilter(provinceName, capital, municipalityName);
        return new ProvinceListResponse(angolaService.getProvinces(page, size, filter));
    }
}