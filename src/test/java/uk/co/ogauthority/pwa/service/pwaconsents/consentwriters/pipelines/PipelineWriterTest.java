package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PipelineWriterTest {

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

  @Before
  public void setUp() throws Exception {

    detail = new PwaApplicationDetail();

    pipelineToPadPipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();

    var identData = pipelineToPadPipelineDtoMap.values().stream()
        .flatMap(dto -> dto.getIdentToIdentDataSetMap().values().stream())
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    when(padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(detail))
        .thenReturn(identData);

    pipelineWriter = new PipelineWriter(padPipelineIdentDataService, pipelineDetailService, padPipelineTransferService);

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  public void writerIsApplicable_hasPipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.PIPELINES), new PwaConsent());

    assertThat(isApplicable).isTrue();

  }

  @Test
  public void writerIsApplicable_doesNotHavePipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.HUOO), new PwaConsent());

    assertThat(isApplicable).isFalse();

  }

  @Test
  public void write() {

    var consent = new PwaConsent();
    pipelineWriter.write(detail, consent, consentWriterDto);

    verify(pipelineDetailService, times(1)).createNewPipelineDetails(pipelineToPadPipelineDtoMap, consent, consentWriterDto);

  }

  @Test
  public void write_transferOut() {

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
  public void write_transferIn() {

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