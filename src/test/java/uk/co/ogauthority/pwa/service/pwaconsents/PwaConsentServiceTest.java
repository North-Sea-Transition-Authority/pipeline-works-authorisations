package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaConsentServiceTest {

  @Mock
  private PwaConsentRepository pwaConsentRepository;

  @Mock
  private PwaConsentReferencingService referencingService;

  @Mock
  private Clock clock;

  @Captor
  private ArgumentCaptor<PwaConsent> consentCaptor;

  private PwaConsentService pwaConsentService;
  private PwaApplicationDetail detail;

  private Instant clockTime;
  private static final String FAKE_REF = "99/X/99";

  @Before
  public void setUp() throws Exception {

    pwaConsentService = new PwaConsentService(pwaConsentRepository, clock, referencingService);

    when(referencingService.createConsentReference(any())).thenReturn(FAKE_REF);

    clockTime = Instant.now();
    when(clock.instant()).thenReturn(clockTime);

  }

  @Test
  public void createConsent_newPwa() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaConsentService.createConsent(detail.getPwaApplication());

    verify(pwaConsentRepository, times(1)).save(consentCaptor.capture());

    assertThat(consentCaptor.getValue()).satisfies(consent -> {
      assertThat(consent.getMasterPwa()).isEqualTo(detail.getPwaApplication().getMasterPwa());
      assertThat(consent.getConsentType()).isEqualTo(detail.getPwaApplication().getApplicationType().getPwaConsentType());
      assertThat(consent.getSourcePwaApplication()).isEqualTo(detail.getPwaApplication());
      assertThat(consent.getConsentInstant()).isEqualTo(clockTime);
      assertThat(consent.getCreatedInstant()).isEqualTo(clockTime);
      assertThat(consent.getReference()).isEqualTo(FAKE_REF);
      assertThat(consent.getVariationNumber()).isZero();
      assertThat(consent.isMigratedFlag()).isFalse();
    });

  }

  @Test
  public void createConsent_variation() {

    var initialConsent = new PwaConsent();
    initialConsent.setVariationNumber(0);
    var firstVariation = new PwaConsent();
    firstVariation.setVariationNumber(1);
    var depCon = new PwaConsent();

    when(pwaConsentRepository.findByMasterPwa(any())).thenReturn(List.of(initialConsent, firstVariation, depCon));

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    pwaConsentService.createConsent(detail.getPwaApplication());

    verify(pwaConsentRepository, times(1)).save(consentCaptor.capture());

    assertThat(consentCaptor.getValue()).satisfies(consent -> {
      assertThat(consent.getMasterPwa()).isEqualTo(detail.getPwaApplication().getMasterPwa());
      assertThat(consent.getConsentType()).isEqualTo(detail.getPwaApplication().getApplicationType().getPwaConsentType());
      assertThat(consent.getSourcePwaApplication()).isEqualTo(detail.getPwaApplication());
      assertThat(consent.getConsentInstant()).isEqualTo(clockTime);
      assertThat(consent.getCreatedInstant()).isEqualTo(clockTime);
      assertThat(consent.getReference()).isEqualTo(FAKE_REF);
      assertThat(consent.getVariationNumber()).isEqualTo(2);
      assertThat(consent.isMigratedFlag()).isFalse();
    });

  }

  @Test
  public void createConsent_depcon() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);
    pwaConsentService.createConsent(detail.getPwaApplication());

    verify(pwaConsentRepository, times(1)).save(consentCaptor.capture());

    assertThat(consentCaptor.getValue()).satisfies(consent -> {
      assertThat(consent.getMasterPwa()).isEqualTo(detail.getPwaApplication().getMasterPwa());
      assertThat(consent.getConsentType()).isEqualTo(detail.getPwaApplication().getApplicationType().getPwaConsentType());
      assertThat(consent.getSourcePwaApplication()).isEqualTo(detail.getPwaApplication());
      assertThat(consent.getConsentInstant()).isEqualTo(clockTime);
      assertThat(consent.getCreatedInstant()).isEqualTo(clockTime);
      assertThat(consent.getReference()).isEqualTo(FAKE_REF);
      assertThat(consent.getVariationNumber()).isNull();
      assertThat(consent.isMigratedFlag()).isFalse();
    });

  }

  @Test
  public void getConsentsByMasterPwa() {

    var masterPwa = new MasterPwa();

    pwaConsentService.getConsentsByMasterPwa(masterPwa);

    verify(pwaConsentRepository, times(1)).findByMasterPwa(masterPwa);

  }

}