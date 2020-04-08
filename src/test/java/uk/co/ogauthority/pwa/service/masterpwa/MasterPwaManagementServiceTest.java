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
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaManagementServiceTest {

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  @Captor
  private ArgumentCaptor<MasterPwa> pwaArgumentCaptor;

  @Captor
  private ArgumentCaptor<MasterPwaDetail> pwaDetailArgumentCaptor;

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

    var user = new WebUserAccount();

    masterPwaManagementService.createMasterPwa(MasterPwaDetailStatus.APPLICATION, "REFERENCE");

    verify(masterPwaRepository, times(1)).save(pwaArgumentCaptor.capture());
    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    MasterPwa masterPwa = pwaArgumentCaptor.getValue();
    MasterPwaDetail masterPwaDetail = pwaDetailArgumentCaptor.getValue();

    // check master pwa set up correctly
    assertThat(masterPwa.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(masterPwaDetail.getStartInstant()).isEqualTo(clock.instant());
    assertThat(masterPwaDetail.getReference()).isEqualTo("REFERENCE");
    assertThat(masterPwaDetail.getMasterPwaDetailStatus()).isEqualTo(MasterPwaDetailStatus.APPLICATION);

  }
}
