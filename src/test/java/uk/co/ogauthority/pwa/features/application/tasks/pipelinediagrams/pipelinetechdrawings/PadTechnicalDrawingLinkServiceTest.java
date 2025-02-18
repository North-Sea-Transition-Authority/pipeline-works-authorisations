package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadTechnicalDrawingLinkServiceTest {

  @Mock
  private PadTechnicalDrawingLinkRepository padTechnicalDrawingLinkRepository;

  @Mock
  private PadPipelineService padPipelineService;

  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    padTechnicalDrawingLinkService = new PadTechnicalDrawingLinkService(padTechnicalDrawingLinkRepository,
        padPipelineService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void getLinksFromDrawingList() {
    var resultList = List.of(new PadTechnicalDrawingLink());
    var argList = List.of(new PadTechnicalDrawing());

    when(padTechnicalDrawingLinkRepository.getAllByTechnicalDrawingIn(argList)).thenReturn(resultList);
    var result = padTechnicalDrawingLinkService.getLinksFromDrawingList(argList);
    assertThat(result).isEqualTo(resultList);
  }

  @Test
  void linkDrawing() {

    var pipelineA = new PadPipeline(pwaApplicationDetail);
    pipelineA.setId(1);

    var pipelineB = new PadPipeline(pwaApplicationDetail);
    pipelineB.setId(2);

    var form = new PipelineDrawingForm();
    form.setPadPipelineIds(List.of(1, 2));

    var pipelineIdList = List.of(1, 2);
    var pipelineList = List.of(pipelineA, pipelineB);
    when(padPipelineService.getByIdList(pwaApplicationDetail, pipelineIdList)).thenReturn(pipelineList);

    var techDrawing = new PadTechnicalDrawing();

    var captor = ArgumentCaptor.forClass(Iterable.class);

    padTechnicalDrawingLinkService.linkDrawing(pwaApplicationDetail, form.getPadPipelineIds(), techDrawing);

    verify(padTechnicalDrawingLinkRepository, times(1)).saveAll(captor.capture());

    var links = (List<PadTechnicalDrawingLink>) captor.getValue();
    assertThat(links).extracting(PadTechnicalDrawingLink::getPipeline, PadTechnicalDrawingLink::getTechnicalDrawing)
        .containsExactly(
            tuple(pipelineA, techDrawing),
            tuple(pipelineB, techDrawing)
        );
  }

  @Test
  void unlinkDrawing() {
    var drawing = new PadTechnicalDrawing();
    var link = new PadTechnicalDrawingLink();
    var linkList = List.of(link);
    when(padTechnicalDrawingLinkRepository.getAllByTechnicalDrawingIn(List.of(drawing)))
        .thenReturn(linkList);
    padTechnicalDrawingLinkService.unlinkDrawing(pwaApplicationDetail, drawing);
    verify(padTechnicalDrawingLinkRepository, times(1)).deleteAll(linkList);
  }

  @Test
  void getLinkedPipelineIds() {
    var dto = new PadPipelineKeyDto(1, 1);
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(dto));
    var result = padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail);
    assertThat(result).isEqualTo(List.of(dto));
  }

  @Test
  void removeAllPipelineLinks_serviceInteraction() {
    var padPipeline = new PadPipeline();
    var link = new PadTechnicalDrawingLink();
    when(padTechnicalDrawingLinkRepository.getAllByTechnicalDrawing_PwaApplicationDetailAndPipeline(pwaApplicationDetail, padPipeline))
        .thenReturn(List.of(link));
    padTechnicalDrawingLinkService.removeAllPipelineLinks(pwaApplicationDetail, padPipeline);
    verify(padTechnicalDrawingLinkRepository, times(1)).deleteAll(List.of(link));
  }
}