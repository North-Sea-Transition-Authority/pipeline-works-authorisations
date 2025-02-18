package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PadPipelineDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriterTestUtils;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineMappingService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineDetailServiceTest {

  @Mock
  private PipelineDetailRepository pipelineDetailRepository;

  @Mock
  private Clock clock;

  @Mock
  private PipelineDetailIdentService pipelineDetailIdentService;

  @Captor
  private ArgumentCaptor<Collection<PipelineDetail>> pipelineDetailsArgCaptor;

  @Captor
  private ArgumentCaptor<PipelineDetail> pipeDetailArgCaptor;

  @Mock
  private PipelineMappingService pipelineMappingService;

  @Mock
  MasterPwaService masterPwaService;

  private PipelineDetailService pipelineDetailService;
  private PwaApplicationDetail detail;

  private Instant clockTime;

  @BeforeEach
  void setUp() {

    clockTime = Instant.now();
    when(clock.instant()).thenReturn(clockTime);

    pipelineDetailService = new PipelineDetailService(pipelineDetailRepository, clock, pipelineMappingService, pipelineDetailIdentService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  void getSimilarPipelineBundleNamesByDetailAndNameLike_serviceInteraction() {
    when(pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(List.of());
    var result = pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail);
    assertThat(result).isEqualTo(List.of());
  }

  @Test
  void getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag_serviceInteraction() {
    var master = detail.getMasterPwa();
    var pipelineDetail = new PipelineDetail();
    when(pipelineDetailRepository.findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(master,
        PipelineStatus.historicalStatusSet()))
        .thenReturn(List.of(pipelineDetail));
    var result = pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(master);
    assertThat(result).containsExactly(pipelineDetail);
  }

  @Test
  void getActivePipelineDetailsForApplicationMasterPwa_serviceInteraction() {
    pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication());
    verify(pipelineDetailRepository, times(1)).findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        detail.getPwaApplication().getMasterPwa());
  }

  @Test
  void isPipelineConsented_consented() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.of(pipelineDetail));

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isTrue();
  }

  @Test
  void isPipelineConsented_notConsented() {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.empty());

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isFalse();
  }

  @Test
  void createNewPipelineDetails() {

    // set up a current detail that we can check has been ended
    var pipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();
    var pipelineToEndDetailFor = pipelineDtoMap.keySet().iterator().next();
    var currentDetail = new PipelineDetail();
    currentDetail.setPipeline(pipelineToEndDetailFor);
    currentDetail.setTipFlag(true);

    var consent = new PwaConsent();

    var consentWriterDto = new ConsentWriterDto();

    when(pipelineDetailRepository.findAllByPipelineInAndEndTimestampIsNull(any())).thenReturn(List.of(currentDetail));

    pipelineDetailService.createNewPipelineDetails(pipelineDtoMap, consent, consentWriterDto);

    verify(pipelineDetailRepository, times(2)).saveAll(pipelineDetailsArgCaptor.capture());

    assertThat(pipelineDetailsArgCaptor.getAllValues().size()).isEqualTo(2);

    // check that the detail we wanted to be ended has been ended
    var endedDetailsList = pipelineDetailsArgCaptor.getAllValues().get(0);
    assertThat(endedDetailsList).hasOnlyOneElementSatisfying(pipelineDetail -> {
      assertThat(pipelineDetail.getTipFlag()).isFalse();
      assertThat(pipelineDetail.getEndTimestamp()).isEqualTo(clockTime);
    });

    // check that all pipelines passed in have been given new details
    var newDetailsList = new ArrayList<>(pipelineDetailsArgCaptor.getAllValues().get(1));
    assertThat(newDetailsList).allSatisfy(newDetail -> {
      assertThat(newDetail.getTipFlag()).isTrue();
      assertThat(newDetail.getStartTimestamp()).isEqualTo(clockTime);
      assertThat(pipelineDtoMap).containsKey(newDetail.getPipeline());
    });

    // check that each app pipeline has been mapped onto its new detail after creation
    pipelineDtoMap.values().stream()
        .map(PadPipelineDto::getPadPipeline)
        .forEach(padPipeline -> {

          var newDetail = newDetailsList.stream()
              .filter(d -> d.getPipeline().equals(padPipeline.getPipeline()))
              .findFirst()
              .orElseThrow();

          verify(pipelineMappingService, times(1)).mapPipelineEntities(newDetail, padPipeline);

          assertThat(consentWriterDto.getPipelineToNewDetailMap()).containsEntry(padPipeline.getPipeline(), newDetail);

        });

    var identCreationMap = pipelineDtoMap.entrySet().stream()
        .collect(Collectors.toMap(
            e -> newDetailsList.stream().filter(d -> d.getPipeline().equals(e.getKey())).findFirst().orElseThrow(),
            Map.Entry::getValue));

    // verify call to ident creation method
    verify(pipelineDetailIdentService, times(1)).createPipelineDetailIdents(identCreationMap);

  }

  @Test
  void createNewPipelineDetails_transferOut() {

    // set up a current detail that we can check has been ended
    var pipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();
    var pipelineToEndDetailFor = pipelineDtoMap.keySet().iterator().next();
    var currentDetail = new PipelineDetail();
    currentDetail.setPipeline(pipelineToEndDetailFor);
    currentDetail.setTipFlag(true);

    var donorPipe = new Pipeline();
    donorPipe.setId(1);
    pipelineDtoMap.get(pipelineToEndDetailFor).setTransferredFromPipeline(donorPipe);

    var consent = new PwaConsent();

    var consentWriterDto = new ConsentWriterDto();

    when(pipelineDetailRepository.findAllByPipelineInAndEndTimestampIsNull(any())).thenReturn(List.of(currentDetail));

    pipelineDetailService.createNewPipelineDetails(pipelineDtoMap, consent, consentWriterDto);

    verify(pipelineDetailRepository, times(2)).saveAll(pipelineDetailsArgCaptor.capture());

    assertThat(pipelineDetailsArgCaptor.getAllValues().size()).isEqualTo(2);

    // check that the ended detail has no transfer info
    var endedDetailsList = pipelineDetailsArgCaptor.getAllValues().get(0);
    assertThat(endedDetailsList).singleElement().satisfies(endedDetail -> assertThat(endedDetail.getTransferredFromPipeline()).isNull());

    var newDetailsList = new ArrayList<>(pipelineDetailsArgCaptor.getAllValues().get(1));

    // check that new detail has transfer info
    assertThat(newDetailsList).anySatisfy(newDetail -> assertThat(newDetail.getTransferredFromPipeline()).isEqualTo(donorPipe));

  }

  @Test
  void getAllPipelineOverviewsForMasterPwa_getsOverviewsSuccessfully() {
    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    var overview = PipelineDetailTestUtil
        .createPipelineOverview("REF", PipelineStatus.IN_SERVICE);

    when(pipelineDetailRepository.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(detail.getMasterPwa(), pipelineStatusFilter, clock.instant()))
        .thenReturn(List.of(overview));

    assertThat(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(detail.getMasterPwa(), pipelineStatusFilter, clock.instant()))
        .extracting(PipelineOverview::getPipelineId)
        .containsExactly(overview.getPipelineId());
  }

  @Test
  void getLatestPipelineDetailsForIds() {
    var pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(20, new PipelineId(10), Instant.now());

    when(pipelineDetailRepository.findAllByPipeline_IdInAndTipFlagIsTrue(List.of(pipelineDetail.getPipeline().getId())))
        .thenReturn(List.of(pipelineDetail));

    assertThat(pipelineDetailService.getLatestPipelineDetailsForIds(List.of(pipelineDetail.getPipeline().getId())))
        .containsExactly((pipelineDetail));
  }

  @Test
  void getPipelineDetailsBeforePwaConsentCreated() {
    var pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(20, new PipelineId(10), Instant.now());
    var consentCreationTs = Instant.now();

    when(pipelineDetailRepository.findAllByPipeline_IdAndPwaConsent_consentInstantIsBefore(pipelineDetail.getPipeline().getId(), consentCreationTs))
        .thenReturn(List.of(pipelineDetail));

    assertThat(pipelineDetailService.getPipelineDetailsBeforePwaConsentCreated(pipelineDetail.getPipeline().getPipelineId(), consentCreationTs))
        .containsExactly(pipelineDetail);
  }

  @Test
  void setTransferredToPipeline_exists() {

    var pipeDetail = new PipelineDetail();
    var pipe = new Pipeline();
    pipe.setId(1);
    pipeDetail.setPipeline(pipe);

    var transferredToPipe = new Pipeline();
    transferredToPipe.setId(2);

    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipe.getId())).thenReturn(Optional.of(pipeDetail));

    pipelineDetailService.setTransferredToPipeline(pipe, transferredToPipe);

    verify(pipelineDetailRepository, times(1)).save(pipeDetailArgCaptor.capture());

    assertThat(pipeDetailArgCaptor.getValue().getTransferredToPipeline()).isEqualTo(transferredToPipe);

  }

  @Test
  void setTransferredToPipeline_doesntExist() {

    var pipe = new Pipeline();
    var pipe2 = new Pipeline();

    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(any())).thenReturn(Optional.empty());

    pipelineDetailService.setTransferredToPipeline(pipe, pipe2);

    verify(pipelineDetailRepository, times(0)).save(any());

  }

}