package uk.co.ogauthority.pwa.service.masterpwas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class MasterPwaViewServiceTest {

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  private MasterPwaViewService masterPwaViewService;

  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;
  private PwaApplication pwaApplication;

  @BeforeEach
  void setup() {

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setMasterPwa(masterPwa);;
    masterPwaDetail.setReference("EXAMPLE_REFERENCE");

    masterPwaViewService = new MasterPwaViewService(masterPwaDetailRepository);

  }


  @Test
  void masterPwaViewService_whenNoCurrentDetailFound() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      masterPwaViewService.getCurrentMasterPwaView(pwaApplication));

  }

  @Test
  void masterPwaViewService_whenCurrentDetailFound() {
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)).thenReturn(Optional.of(masterPwaDetail));
    assertThat(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).satisfies(masterPwaView ->{
      assertThat(masterPwaView.getMasterPwaId()).isEqualTo(masterPwa.getId());
      assertThat(masterPwaView.getReference()).isEqualTo(masterPwaDetail.getReference());

    });

  }
}