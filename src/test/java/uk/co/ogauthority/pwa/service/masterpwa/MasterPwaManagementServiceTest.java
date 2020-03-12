package uk.co.ogauthority.pwa.service.masterpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaRepository;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaManagementServiceTest {

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private MasterPwaManagementService masterPwaManagementService;


  @Before
  public void setup() {
    masterPwaManagementService = new MasterPwaManagementService(
        masterPwaRepository,
        masterPwaDetailRepository,
        clock
    );

  }

  @Test
  public void createMasterPwa() {

    ArgumentCaptor<MasterPwa> pwaArgumentCaptor = ArgumentCaptor.forClass(MasterPwa.class);
    ArgumentCaptor<MasterPwaDetail> pwaDetailArgumentCaptor = ArgumentCaptor.forClass(MasterPwaDetail.class);
    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);
    ArgumentCaptor<PwaApplicationDetail> detailArgumentCaptor = ArgumentCaptor.forClass(PwaApplicationDetail.class);

    masterPwaManagementService.createMasterPwa(MasterPwaDetailStatus.APPLICATION, "REFERENCE");

    verify(masterPwaRepository, times(1)).save(pwaArgumentCaptor.capture());
    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    MasterPwa masterPwa = pwaArgumentCaptor.getValue();
    MasterPwaDetail masterPwaDetail = pwaDetailArgumentCaptor.getValue();

    // check master pwa set up correctly
    assertThat(masterPwa.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(masterPwa.getPortalOrganisationUnit()).isNull();

    assertThat(masterPwaDetail.getStartInstant()).isEqualTo(clock.instant());
    assertThat(masterPwaDetail.getReference()).isNotBlank();
    assertThat(masterPwaDetail.getMasterPwaDetailStatus()).isEqualTo(MasterPwaDetailStatus.APPLICATION);
  }
}
