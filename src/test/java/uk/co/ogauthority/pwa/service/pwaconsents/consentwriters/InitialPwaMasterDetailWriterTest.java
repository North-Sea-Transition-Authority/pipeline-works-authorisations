package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class InitialPwaMasterDetailWriterTest {

  @Mock
  private MasterPwaService masterPwaService;

  private InitialPwaMasterDetailWriter initialPwaMasterDetailWriter;

  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;

  private ConsentWriterDto consentWriterDto;

  @BeforeEach
  void setUp() throws Exception {
    masterPwa = new MasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setConsentType(PwaConsentType.INITIAL_PWA);

    initialPwaMasterDetailWriter = new InitialPwaMasterDetailWriter(masterPwaService);

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  void getExecutionOrder() {
    assertThat(initialPwaMasterDetailWriter.getExecutionOrder()).isEqualTo(1);
  }

  @Test
  void writerIsApplicable_whenInitialConsent() {
    assertThat(initialPwaMasterDetailWriter.writerIsApplicable(Set.of(), pwaConsent)).isTrue();
  }

  @Test
  void writerIsApplicable_whenNotInitialConsent() {
    pwaConsent.setConsentType(PwaConsentType.DEPOSIT_CONSENT);

    assertThat(initialPwaMasterDetailWriter.writerIsApplicable(Set.of(), pwaConsent)).isFalse();
  }

  @Test
  void write_serviceInteractions() {

    var pwaAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = pwaAppDetail.getMasterPwa();

    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setReference("consentRef");

    var masterDetail = new MasterPwaDetail();
    masterDetail.setMasterPwa(masterPwa);

    when(masterPwaService.createNewDetailWithStatus(any(), any())).thenReturn(masterDetail);

    initialPwaMasterDetailWriter.write(pwaAppDetail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(1)).createNewDetailWithStatus(masterPwa, MasterPwaDetailStatus.CONSENTED);

    verify(masterPwaService, times(1)).updateDetailReference(masterDetail, pwaConsent.getReference());

  }
}