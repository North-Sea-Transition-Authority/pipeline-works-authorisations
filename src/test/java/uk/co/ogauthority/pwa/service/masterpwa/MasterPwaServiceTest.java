package uk.co.ogauthority.pwa.service.masterpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaServiceTest {

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  @Captor
  private ArgumentCaptor<MasterPwa> pwaArgumentCaptor;

  @Captor
  private ArgumentCaptor<MasterPwaDetail> pwaDetailArgumentCaptor;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private MasterPwaService masterPwaService;

  @Before
  public void setup() {
    masterPwaService = new MasterPwaService(
        masterPwaRepository,
        masterPwaDetailRepository,
        clock
    );
  }

  @Test
  public void createMasterPwa() {

    var user = new WebUserAccount();

    masterPwaService.createMasterPwa(MasterPwaDetailStatus.APPLICATION, "REFERENCE");

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

  @Test
  public void getCurrentDetailOrThrow_found() {

    var detail = new MasterPwaDetail();
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(any())).thenReturn(Optional.of(detail));

    var result = masterPwaService.getCurrentDetailOrThrow(new MasterPwa());

    assertThat(result).isEqualTo(detail);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getCurrentDetailOrThrow_notFound() {

    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(any())).thenReturn(Optional.empty());

    masterPwaService.getCurrentDetailOrThrow(new MasterPwa());

  }

  @Test
  public void updateDetail() {

    var detail = new MasterPwaDetail();

    masterPwaService.updateDetail(detail, MasterPwaDetailStatus.CONSENTED, true, null);

    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getValue()).satisfies(saved -> {
      assertThat(saved.getMasterPwaDetailStatus()).isEqualTo(MasterPwaDetailStatus.CONSENTED);
      assertThat(saved.getLinkedToFields()).isTrue();
      assertThat(saved.getPwaLinkedToDescription()).isNull();
    });

  }

  @Test
  public void createNewDetail() {

    var masterPwa = new MasterPwa();
    var detail = new MasterPwaDetail();
    detail.setReference("ref");
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)).thenReturn(Optional.of(detail));

    masterPwaService.createNewDetail(masterPwa, false, "description here");

    verify(masterPwaDetailRepository, times(2)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(0)).satisfies(first -> {
      assertThat(first.getEndInstant()).isNotNull();
    });

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(1)).satisfies(second -> {
      assertThat(second.getMasterPwa()).isEqualTo(masterPwa);
      assertThat(second.getStartInstant()).isEqualTo(clock.instant());
      assertThat(second.getReference()).isEqualTo(detail.getReference());
      assertThat(second.getLinkedToFields()).isFalse();
      assertThat(second.getPwaLinkedToDescription()).isEqualTo("description here");
    });

  }

}
