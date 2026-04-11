package ao.vanadio.publicinformationapi.features.angola.dto.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AngolaJson(
        @JsonProperty("provinces") List<ProvinceJson> provinces
) {}