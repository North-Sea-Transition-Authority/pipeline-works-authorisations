package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;

@RunWith(MockitoJUnitRunner.class)
public class PwaConsentServiceTest {

  private static final String REFERENCE = "REFERENCE";
  private static final int VARIATION_NUMBER = 1;

  @Mock
  private PwaConsentRepository pwaConsentRepository;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private Instant consentedInstant = Instant.MIN;

  private MasterPwa masterPwa;

  private PwaConsentService pwaConsentService;


  @Before
  public void setup() {
    pwaConsentService = new PwaConsentService(pwaConsentRepository, clock);
    masterPwa = new MasterPwa(clock.instant());
  }

  @Test
  public void createPwaConsentWithoutApplication_createsConsentAsExpected_whenIsMigrated() {
    var pwaApplication = pwaConsentService.createPwaConsentWithoutApplication(
        masterPwa,
        REFERENCE,
        PwaConsentType.VARIATION,
        consentedInstant,
        VARIATION_NUMBER,
        true
    );

    assertThat(pwaApplication.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(pwaApplication.getConsentInstant()).isEqualTo(consentedInstant);
    assertThat(pwaApplication.getCreatedInstant()).isEqualTo(clock.instant());
    assertThat(pwaApplication.getConsentType()).isEqualTo(PwaConsentType.VARIATION);
    assertThat(pwaApplication.getVariationNumber()).isEqualTo(VARIATION_NUMBER);
    assertThat(pwaApplication.isMigratedFlag()).isTrue();
    assertThat(pwaApplication.getReference()).isEqualTo(REFERENCE);
    assertThat(pwaApplication.getSourcePwaApplication()).isNull();
  }

  @Test
  public void createPwaConsentWithoutApplication_createsConsentAsExpected_whenNotMigrated() {
    var pwaApplication = pwaConsentService.createPwaConsentWithoutApplication(
        masterPwa,
        REFERENCE,
        PwaConsentType.VARIATION,
        consentedInstant,
        VARIATION_NUMBER,
        false
    );

    assertThat(pwaApplication.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(pwaApplication.getConsentInstant()).isEqualTo(consentedInstant);
    assertThat(pwaApplication.getCreatedInstant()).isEqualTo(clock.instant());
    assertThat(pwaApplication.getConsentType()).isEqualTo(PwaConsentType.VARIATION);
    assertThat(pwaApplication.getVariationNumber()).isEqualTo(VARIATION_NUMBER);
    assertThat(pwaApplication.isMigratedFlag()).isFalse();
    assertThat(pwaApplication.getReference()).isEqualTo(REFERENCE);
    assertThat(pwaApplication.getSourcePwaApplication()).isNull();
  }

  @Test
  public void createPwaConsentWithoutApplication_mapsCorrectConsentType() {
    for (PwaConsentType pwaConsentType : PwaConsentType.values()) {
      var pwaApplication = pwaConsentService.createPwaConsentWithoutApplication(
          masterPwa,
          REFERENCE,
          pwaConsentType,
          consentedInstant,
          VARIATION_NUMBER,
          false
      );

      assertThat(pwaApplication.getConsentType()).isEqualTo(pwaConsentType);

    }
  }

}
