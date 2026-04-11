package ao.vanadio.publicinformationapi.features.angola.dto.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProvinceJson(
        @JsonProperty("it_id_provincia") int id,
        @JsonProperty("vc_nome") String name,
        @JsonProperty("vc_governador") Governor governor,
        @JsonProperty("vc_vice_governadores") List<ViceGovernor> viceGovernors,
        @JsonProperty("vc_capital") String capital,
        @JsonProperty("vc_linguas") String languages,
        @JsonProperty("vc_data_fundacao_provincia") String foundingDate,
        @JsonProperty("vc_extensao") String area,
        @JsonProperty("vc_etnias") String ethnicities,
        @JsonProperty("it_numero_municipios") int municipalityCount,
        @JsonProperty("municipalities") List<Municipality> municipalities
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Governor(
            @JsonProperty("vc_nome") String name,
            @JsonProperty("vc_data_nomeacao") String appointmentDate,
            @JsonProperty("vc_imagem") String imageUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ViceGovernor(
            @JsonProperty("vc_nome") String name,
            @JsonProperty("vc_setor") String sector,
            @JsonProperty("vc_data_nomeacao") String appointmentDate,
            @JsonProperty("vc_imagem") String imageUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Municipality(
            @JsonProperty("vc_nome") String name,
            @JsonProperty("vc_comunas") List<String> comunas,
            @JsonProperty("it_numero_comunas") int comunaCount,
            @JsonProperty("vc_data_fundacao") String foundingDate,
            @JsonProperty("vc_administrador") String administrator
    ) {}
}