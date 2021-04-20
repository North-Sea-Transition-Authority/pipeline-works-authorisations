package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PadPipelineDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriterTestUtils;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineMappingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDetailServiceTest {

  @Mock
  private PipelineDetailRepository pipelineDetailRepository;

  @Mock
  private Clock clock;

  @Mock
  private PipelineDetailIdentService pipelineDetailIdentService;

  @Captor
  private ArgumentCaptor<List<PipelineDetail>> pipelineDetailsArgCaptor;

  @Mock
  private PipelineMappingService pipelineMappingService;

  private PipelineDetailService pipelineDetailService;
  private PwaApplicationDetail detail;

  private Instant clockTime;

  @Before
  public void setUp() {

    clockTime = Instant.now();
    when(clock.instant()).thenReturn(clockTime);

    pipelineDetailService = new PipelineDetailService(pipelineDetailRepository, clock, pipelineMappingService, pipelineDetailIdentService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void getSimilarPipelineBundleNamesByDetailAndNameLike_serviceInteraction() {
    when(pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(List.of());
    var result = pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail);
    assertThat(result).isEqualTo(List.of());
  }

  @Test
  public void getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag_serviceInteraction() {
    var master = detail.getMasterPwa();
    var pipelineDetail = new PipelineDetail();
    when(pipelineDetailRepository.findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(master, PipelineStatus.historicalStatusSet()))        .thenReturn(List.of(pipelineDetail));
    var result = pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(master);
    assertThat(result).containsExactly(pipelineDetail);
  }

  @Test
  public void getActivePipelineDetailsForApplicationMasterPwa_serviceInteraction() {
    pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication());
    verify(pipelineDetailRepository, times(1)).findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        detail.getPwaApplication().getMasterPwa());
  }

  @Test
  public void isPipelineConsented_consented() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.of(pipelineDetail));

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isTrue();
  }

  @Test
  public void isPipelineConsented_notConsented() {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.empty());

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isFalse();
  }

  @Test
  public void createNewPipelineDetails() {

    // set up a current detail that we can check has been ended
    var pipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();
    var pipelineToEndDetailFor = pipelineDtoMap.keySet().iterator().next();
    var currentDetail = new PipelineDetail();
    currentDetail.setPipeline(pipelineToEndDetailFor);
    currentDetail.setTipFlag(true);

    var consent = new PwaConsent();

    when(pipelineDetailRepository.findAllByPipelineInAndEndTimestampIsNull(any())).thenReturn(List.of(currentDetail));

    pipelineDetailService.createNewPipelineDetails(pipelineDtoMap, consent);

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
      assertThat(pipelineDtoMap.keySet()).contains(newDetail.getPipeline());
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

        });

    var identCreationMap = pipelineDtoMap.entrySet().stream()
        .collect(Collectors.toMap(
            e -> newDetailsList.stream().filter(d -> d.getPipeline().equals(e.getKey())).findFirst().orElseThrow(),
            Map.Entry::getValue));

    // verify call to ident creation method
    verify(pipelineDetailIdentService, times(1)).createPipelineDetailIdents(identCreationMap);

  }

}