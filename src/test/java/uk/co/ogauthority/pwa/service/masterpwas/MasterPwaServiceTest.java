package uk.co.ogauthority.pwa.service.masterpwas;

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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;

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

    // make sure we get an object back from save as real repos do.
    when(masterPwaDetailRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
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
  public void updateDetailFieldInfo_setsValuesAsExpected() {
    var masterPwa = new MasterPwa();
    var detail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.APPLICATION, "some ref", clock.instant().minusMillis(100));
    detail.setLinkedToFields(false);
    detail.setPwaLinkedToDescription("some description");

    masterPwaService.updateDetailFieldInfo(detail, true, null);

    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getValue()).satisfies(saved -> {
      assertThat(saved.getLinkedToFields()).isTrue();
      assertThat(saved.getPwaLinkedToDescription()).isNull();
    });

  }

  @Test
  public void createDuplicateNewDetail_endsOldDetailAndSetsNewDetailValuesAsExpected() {

    var masterPwa = new MasterPwa();
    var detail = new MasterPwaDetail();
    detail.setMasterPwa(masterPwa);
    detail.setReference("ref");
    detail.setLinkedToFields(false);
    detail.setPwaLinkedToDescription("some description");
    detail.setMasterPwaDetailStatus(MasterPwaDetailStatus.APPLICATION);

    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)).thenReturn(Optional.of(detail));

    masterPwaService.createDuplicateNewDetail(masterPwa);

    verify(masterPwaDetailRepository, times(2)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(0)).satisfies(first -> {
      assertThat(first.getEndInstant()).isNotNull();
    });

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(1)).satisfies(second -> {
      assertThat(second.getMasterPwa()).isEqualTo(masterPwa);
      assertThat(second.getStartInstant()).isEqualTo(clock.instant());
      assertThat(second.getReference()).isEqualTo(detail.getReference());
      assertThat(second.getLinkedToFields()).isEqualTo(detail.getLinkedToFields());
      assertThat(second.getPwaLinkedToDescription()).isEqualTo(detail.getPwaLinkedToDescription());
    });

  }

}
