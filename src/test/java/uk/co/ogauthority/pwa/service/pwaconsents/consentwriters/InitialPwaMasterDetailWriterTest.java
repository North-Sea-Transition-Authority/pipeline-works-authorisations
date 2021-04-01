package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class InitialPwaMasterDetailWriterTest {

  @Mock
  private MasterPwaService masterPwaService;

  private InitialPwaMasterDetailWriter initialPwaMasterDetailWriter;

  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;

  @Before
  public void setUp() throws Exception {
    masterPwa = new MasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setConsentType(PwaConsentType.INITIAL_PWA);

    initialPwaMasterDetailWriter = new InitialPwaMasterDetailWriter(masterPwaService);
  }

  @Test
  public void getExecutionOrder() {
    assertThat(initialPwaMasterDetailWriter.getExecutionOrder()).isEqualTo(1);
  }

  @Test
  public void writerIsApplicable_whenInitialConsent() {
    assertThat(initialPwaMasterDetailWriter.writerIsApplicable(Set.of(), pwaConsent)).isTrue();
  }

  @Test
  public void writerIsApplicable_whenNotInitialConsent() {
    pwaConsent.setConsentType(PwaConsentType.DEPOSIT_CONSENT);

    assertThat(initialPwaMasterDetailWriter.writerIsApplicable(Set.of(), pwaConsent)).isFalse();
  }

  @Test
  public void write_serviceInteractions() {

    var pwaAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = pwaAppDetail.getMasterPwa();
    pwaConsent.setMasterPwa(masterPwa);


    initialPwaMasterDetailWriter.write(pwaAppDetail, pwaConsent);

    verify(masterPwaService, times(1)).createNewDetailWithStatus(masterPwa, MasterPwaDetailStatus.CONSENTED);

  }
}