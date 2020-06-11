package uk.co.ogauthority.pwa.service.masterpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaViewServiceTest {

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  private MasterPwaViewService masterPwaViewService;

  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;
  private PwaApplication pwaApplication;
  @Before
  public void setup() {

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setMasterPwa(masterPwa);;
    masterPwaDetail.setReference("EXAMPLE_REFERENCE");

    masterPwaViewService = new MasterPwaViewService(masterPwaDetailRepository);

  }


  @Test(expected = PwaEntityNotFoundException.class)
  public void masterPwaViewService_whenNoCurrentDetailFound() {
    masterPwaViewService.getCurrentMasterPwaView(pwaApplication);

  }

  @Test
  public void masterPwaViewService_whenrrentDetailFound() {
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)).thenReturn(Optional.of(masterPwaDetail));
    assertThat(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).satisfies(masterPwaView ->{
      assertThat(masterPwaView.getMasterPwaId()).isEqualTo(masterPwa.getId());
      assertThat(masterPwaView.getReference()).isEqualTo(masterPwaDetail.getReference());

    });

  }
}