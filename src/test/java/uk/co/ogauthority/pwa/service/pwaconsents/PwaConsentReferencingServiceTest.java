package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaConsentReferencingServiceTest {

  @Mock
  private PwaConsentReferenceNumberGenerator numberGenerator;

  private PwaConsentReferencingService pwaConsentReferencingService;

  @Before
  public void setUp() throws Exception {

    pwaConsentReferencingService = new PwaConsentReferencingService(numberGenerator);

    when(numberGenerator.getConsentNumber(any())).thenReturn(5);

  }

  @Test
  public void createConsentReference_initial() {

    var consent = new PwaConsent();
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    consent.setSourcePwaApplication(detail.getPwaApplication());
    var consentInstant = Instant.now();
    var consentDate = LocalDate.ofInstant(consentInstant, ZoneId.systemDefault());

    consent.setConsentInstant(consentInstant);

    var ref = pwaConsentReferencingService.createConsentReference(consent);

    assertThat(ref).isEqualTo("5/W/" + DateUtils.getTwoDigitYear(consentDate));

  }

  @Test
  public void createConsentReference_variation() {

    var consent = new PwaConsent();
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    consent.setSourcePwaApplication(detail.getPwaApplication());
    var consentInstant = Instant.now();
    var consentDate = LocalDate.ofInstant(consentInstant, ZoneId.systemDefault());

    consent.setConsentInstant(consentInstant);

    var ref = pwaConsentReferencingService.createConsentReference(consent);

    assertThat(ref).isEqualTo("5/V/" + DateUtils.getTwoDigitYear(consentDate));

  }

  @Test
  public void createConsentReference_depcon() {

    var consent = new PwaConsent();
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);
    consent.setSourcePwaApplication(detail.getPwaApplication());
    var consentInstant = Instant.now();
    var consentDate = LocalDate.ofInstant(consentInstant, ZoneId.systemDefault());

    consent.setConsentInstant(consentInstant);

    var ref = pwaConsentReferencingService.createConsentReference(consent);

    assertThat(ref).isEqualTo("5/D/" + DateUtils.getTwoDigitYear(consentDate));

  }

}