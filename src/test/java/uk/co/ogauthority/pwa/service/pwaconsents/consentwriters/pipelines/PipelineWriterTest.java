package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@ExtendWith(MockitoExtension.class)
class PipelineWriterTest {

  @Mock
  private PadPipelineIdentDataService padPipelineIdentDataService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PadPipelineTransferService padPipelineTransferService;

  private PipelineWriter pipelineWriter;

  private PwaApplicationDetail detail;

  private Map<Pipeline, PadPipelineDto> pipelineToPadPipelineDtoMap;

  private ConsentWriterDto consentWriterDto;
  private List<PadPipelineIdentData> identData;

  @BeforeEach
  void setUp() {

    detail = new PwaApplicationDetail();

    pipelineToPadPipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();

    identData = pipelineToPadPipelineDtoMap.values().stream()
        .flatMap(dto -> dto.getIdentToIdentDataSetMap().values().stream())
        .flatMap(Collection::stream)
        .toList();

    pipelineWriter = new PipelineWriter(padPipelineIdentDataService, pipelineDetailService, padPipelineTransferService);

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  void writerIsApplicable_hasPipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.PIPELINES), new PwaConsent());

    assertThat(isApplicable).isTrue();

  }

  @Test
  void writerIsApplicable_doesNotHavePipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.HUOO), new PwaConsent());

    assertThat(isApplicable).isFalse();

  }

  @Test
  void write() {

    when(padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(detail))
        .thenReturn(identData);

    var consent = new PwaConsent();
    pipelineWriter.write(detail, consent, consentWriterDto);

    verify(pipelineDetailService, times(1)).createNewPipelineDetails(pipelineToPadPipelineDtoMap, consent, consentWriterDto);

  }

  @Test
  void write_transferOut() {

    when(padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(detail))
        .thenReturn(identData);

    var transfer = new PadPipelineTransfer();
    var pipes = new ArrayList<>(pipelineToPadPipelineDtoMap.keySet());
    var donorPipe = pipes.get(0);
    var recipientPipe = pipes.get(1);
    transfer.setDonorPipeline(donorPipe);
    transfer.setDonorApplicationDetail(detail);
    transfer.setRecipientPipeline(recipientPipe);
    var recipientDetail = new PwaApplicationDetail();
    transfer.setRecipientApplicationDetail(recipientDetail);

    when(padPipelineTransferService.getPipelineToTransferMap(detail)).thenReturn(Map.of(donorPipe, transfer));

    var consent = new PwaConsent();
    pipelineWriter.write(detail, consent, consentWriterDto);

    verify(pipelineDetailService, times(1)).createNewPipelineDetails(pipelineToPadPipelineDtoMap, consent, consentWriterDto);
    verify(pipelineDetailService, times(0)).setTransferredToPipeline(any(), any());

  }

  @Test
  void write_transferIn() {

    when(padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(detail))
        .thenReturn(identData);

    var transfer = new PadPipelineTransfer();
    var pipes = new ArrayList<>(pipelineToPadPipelineDtoMap.keySet());
    var donorPipe = pipes.get(0);
    var recipientPipe = pipes.get(1);
    transfer.setDonorPipeline(donorPipe);
    var donorDetail = new PwaApplicationDetail();
    transfer.setDonorApplicationDetail(donorDetail);
    transfer.setRecipientPipeline(recipientPipe);
    transfer.setRecipientApplicationDetail(detail);

    when(padPipelineTransferService.getPipelineToTransferMap(detail)).thenReturn(Map.of(recipientPipe, transfer));

    pipelineToPadPipelineDtoMap.get(recipientPipe).setTransferredFromPipeline(donorPipe);

    var consent = new PwaConsent();
    pipelineWriter.write(detail, consent, consentWriterDto);

    verify(pipelineDetailService, times(1)).createNewPipelineDetails(pipelineToPadPipelineDtoMap, consent, consentWriterDto);
    verify(pipelineDetailService, times(1)).setTransferredToPipeline(donorPipe, recipientPipe);

  }

}