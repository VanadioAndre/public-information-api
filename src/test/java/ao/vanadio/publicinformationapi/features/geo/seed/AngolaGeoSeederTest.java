package ao.vanadio.publicinformationapi.features.geo.seed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngolaGeoSeederTest {

    @Test
    void displayNameTitleCasesKeepingPortugueseParticles() {
        assertThat(AngolaGeoSeeder.displayName("ICOLO E BENGO")).isEqualTo("Icolo e Bengo");
        assertThat(AngolaGeoSeeder.displayName("CUANZA-NORTE")).isEqualTo("Cuanza-Norte");
        assertThat(AngolaGeoSeeder.displayName("JOSÉ DA SILVA DOS SANTOS")).isEqualTo("José da Silva dos Santos");
        assertThat(AngolaGeoSeeder.displayName("  ")).isNull();
    }
}
