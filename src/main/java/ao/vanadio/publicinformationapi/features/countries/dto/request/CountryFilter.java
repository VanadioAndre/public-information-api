package ao.vanadio.publicinformationapi.features.countries.dto.request;

public record CountryFilter(
        String countryCode,
        String countryName,
        String region,
        String phoneCode
) {
    public boolean isEmpty() {
        return countryCode == null
                && countryName == null
                && region == null
                && phoneCode == null;
    }
}
