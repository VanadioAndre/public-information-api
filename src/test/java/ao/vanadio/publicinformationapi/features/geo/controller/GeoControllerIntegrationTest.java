package ao.vanadio.publicinformationapi.features.geo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GeoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions call(String path) throws Exception {
        return mockMvc.perform(get(path).header(HttpHeaders.ACCEPT_LANGUAGE, "en"));
    }

    @Test
    void countryDetailSummarizesAngolaHierarchy() throws Exception {
        call("/v1/data/countries/AGO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryCode").value("AO"))
                .andExpect(jsonPath("$.capitals[0]").value("Luanda"))
                .andExpect(jsonPath("$.subdivisionLevels[0].count").value(21))
                .andExpect(jsonPath("$.subdivisionLevels[1].count").value(326))
                .andExpect(jsonPath("$.subdivisionLevels.length()").value(3))
                .andExpect(jsonPath("$.cityCount").value(greaterThan(0)));
    }

    @Test
    void angolaProvinceIsResolvedByIsoCodeWithMunicipalities() throws Exception {
        call("/v1/data/countries/AO/subdivisions/AO-LUA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luanda"))
                .andExpect(jsonPath("$.source").value("angola.json"))
                .andExpect(jsonPath("$.childCount").value(16))
                .andExpect(jsonPath("$.leaders[0].role").value("GOVERNOR"));

        call("/v1/data/countries/AO/subdivisions/LUA/children?size=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(16))
                .andExpect(jsonPath("$.content[0].type").value("municipality"));
    }

    @Test
    void subdivisionsCanBeSearchedByName() throws Exception {
        call("/v1/data/countries/US/subdivisions?name=calif")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].isoCode", hasItem("US-CA")));
    }

    @Test
    void citiesIncludeNestedSubdivisionsAndAreOrderedByPopulation() throws Exception {
        call("/v1/data/countries/BR/cities?subdivisionCode=BR-SP&size=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("São Paulo"))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void translatesSubdivisionNamesFromAcceptLanguage() throws Exception {
        mockMvc.perform(get("/v1/data/countries/DE/subdivisions/DE-BY").header(HttpHeaders.ACCEPT_LANGUAGE, "pt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Baviera"));
    }

    @Test
    void unknownCountryReturnsNotFound() throws Exception {
        call("/v1/data/countries/XX/subdivisions").andExpect(status().isNotFound());
        call("/v1/data/countries/XX").andExpect(status().isNotFound());
    }

    @Test
    void invalidPaginationReturnsBadRequest() throws Exception {
        call("/v1/data/countries/AO/subdivisions?size=500").andExpect(status().isBadRequest());
        call("/v1/data/countries/AO/cities?page=-1").andExpect(status().isBadRequest());
    }

    @Test
    void legacyAngolaEndpointKeepsItsContract() throws Exception {
        call("/v1/data/angola?size=30")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provinces.length()").value(21))
                .andExpect(jsonPath("$.provinces[0].name").value("BENGO"))
                .andExpect(jsonPath("$.provinces[0].municipalities[0].name").exists())
                .andExpect(jsonPath("$.provinces[0].governor.name").exists());
    }
}
