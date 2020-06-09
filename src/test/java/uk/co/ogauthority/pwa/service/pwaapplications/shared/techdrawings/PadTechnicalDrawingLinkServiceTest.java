package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadTechnicalDrawingLinkServiceTest {

  @Mock
  private PadTechnicalDrawingLinkRepository padTechnicalDrawingLinkRepository;

  @Mock
  private PadPipelineService padPipelineService;

  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padTechnicalDrawingLinkService = new PadTechnicalDrawingLinkService(padTechnicalDrawingLinkRepository,
        padPipelineService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void getLinksFromDrawingList() {
    var resultList = List.of(new PadTechnicalDrawingLink());
    var argList = List.of(new PadTechnicalDrawing());

    when(padTechnicalDrawingLinkRepository.getAllByTechnicalDrawingIn(argList)).thenReturn(resultList);
    var result = padTechnicalDrawingLinkService.getLinksFromDrawingList(argList);
    assertThat(result).isEqualTo(resultList);
  }

  @Test
  public void linkDrawing() {

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
  public void unlinkDrawing() {
    var drawing = new PadTechnicalDrawing();
    var link = new PadTechnicalDrawingLink();
    var linkList = List.of(link);
    when(padTechnicalDrawingLinkRepository.getAllByTechnicalDrawingIn(List.of(drawing)))
        .thenReturn(linkList);
    padTechnicalDrawingLinkService.unlinkDrawing(pwaApplicationDetail, drawing);
    verify(padTechnicalDrawingLinkRepository, times(1)).deleteAll(linkList);
  }
}