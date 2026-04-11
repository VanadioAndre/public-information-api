package ao.vanadio.publicinformationapi.features.angola.dto.request;

public record AngolaFilter(
        String provinceName,
        String capital,
        String municipalityName
) {
    public boolean isEmpty() {
        return provinceName == null && capital == null && municipalityName == null;
    }
}