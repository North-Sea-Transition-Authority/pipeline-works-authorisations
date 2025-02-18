package uk.co.ogauthority.pwa.service.masterpwas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.HYDROGEN;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.PETROLEUM;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;

@ExtendWith(MockitoExtension.class)
class MasterPwaServiceTest {

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

  @BeforeEach
  void setup() {
    masterPwaService = new MasterPwaService(
        masterPwaRepository,
        masterPwaDetailRepository,
        clock
    );
  }

  @Test
  void createMasterPwa() {

    // make sure we get an object back from save as real repos do.
    when(masterPwaDetailRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var user = new WebUserAccount();

    masterPwaService.createMasterPwa(MasterPwaDetailStatus.APPLICATION, "REFERENCE", PETROLEUM);

    verify(masterPwaRepository, times(1)).save(pwaArgumentCaptor.capture());
    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    MasterPwa masterPwa = pwaArgumentCaptor.getValue();
    MasterPwaDetail masterPwaDetail = pwaDetailArgumentCaptor.getValue();

    // check master pwa set up correctly
    assertThat(masterPwa.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(masterPwaDetail.getStartInstant()).isEqualTo(clock.instant());
    assertThat(masterPwaDetail.getReference()).isEqualTo("REFERENCE");
    assertThat(masterPwaDetail.getResourceType()).isEqualTo(PETROLEUM);
    assertThat(masterPwaDetail.getMasterPwaDetailStatus()).isEqualTo(MasterPwaDetailStatus.APPLICATION);

  }

  @Test
  void getCurrentDetailOrThrow_found() {

    var detail = new MasterPwaDetail();
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(any())).thenReturn(Optional.of(detail));

    var result = masterPwaService.getCurrentDetailOrThrow(new MasterPwa());

    assertThat(result).isEqualTo(detail);

  }

  @Test
  void getCurrentDetailOrThrow_notFound() {
    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(any())).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      masterPwaService.getCurrentDetailOrThrow(new MasterPwa()));

  }

  @Test
  void updateDetailFieldInfo_setsValuesAsExpected() {

    // make sure we get an object back from save as real repos do.
    when(masterPwaDetailRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var masterPwa = new MasterPwa();
    var detail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.APPLICATION, "some ref", clock.instant().minusMillis(100), HYDROGEN);
    detail.setLinkedToFields(false);
    detail.setPwaLinkedToDescription("some description");

    masterPwaService.updateDetailFieldInfo(detail, true, null);

    verify(masterPwaDetailRepository, times(1)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getValue()).satisfies(saved -> {
      assertThat(saved.getLinkedToFields()).isTrue();
      assertThat(saved.getPwaLinkedToDescription()).isNull();
      assertThat(saved.getResourceType()).isEqualTo(HYDROGEN);
    });

  }

  @Test
  void createDuplicateNewDetail_endsOldDetailAndSetsNewDetailValuesAsExpected() {

    // make sure we get an object back from save as real repos do.
    when(masterPwaDetailRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var masterPwa = new MasterPwa();
    var detail = new MasterPwaDetail();
    detail.setMasterPwa(masterPwa);
    detail.setReference("ref");
    detail.setLinkedToFields(false);
    detail.setResourceType(HYDROGEN);
    detail.setPwaLinkedToDescription("some description");
    detail.setMasterPwaDetailStatus(MasterPwaDetailStatus.APPLICATION);

    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)).thenReturn(Optional.of(detail));

    masterPwaService.createDuplicateNewDetail(masterPwa);

    verify(masterPwaDetailRepository, times(2)).save(pwaDetailArgumentCaptor.capture());

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(0)).satisfies(first ->
      assertThat(first.getEndInstant()).isNotNull());

    assertThat(pwaDetailArgumentCaptor.getAllValues().get(1)).satisfies(second -> {
      assertThat(second.getMasterPwa()).isEqualTo(masterPwa);
      assertThat(second.getStartInstant()).isEqualTo(clock.instant());
      assertThat(second.getResourceType()).isEqualTo(HYDROGEN);
      assertThat(second.getReference()).isEqualTo(detail.getReference());
      assertThat(second.getLinkedToFields()).isEqualTo(detail.getLinkedToFields());
      assertThat(second.getPwaLinkedToDescription()).isEqualTo(detail.getPwaLinkedToDescription());
    });

  }

}
