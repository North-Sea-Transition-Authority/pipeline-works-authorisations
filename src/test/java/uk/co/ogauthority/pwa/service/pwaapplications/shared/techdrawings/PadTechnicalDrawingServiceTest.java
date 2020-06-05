package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.techdrawings.EditPipelineDrawingValidator;
import uk.co.ogauthority.pwa.validators.techdrawings.PipelineDrawingValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadTechnicalDrawingServiceTest {

  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PipelineDrawingValidator pipelineDrawingValidator;

  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  @Mock
  private EditPipelineDrawingValidator editPipelineDrawingValidator;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padTechnicalDrawingService = new PadTechnicalDrawingService(padTechnicalDrawingRepository,
        padTechnicalDrawingLinkService, padFileService, padPipelineService, pipelineDrawingValidator,
        editPipelineDrawingValidator, springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  @Test
  public void getDrawings() {
    var list = List.of(new PadTechnicalDrawing());
    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(list);
    var result = padTechnicalDrawingService.getDrawings(pwaApplicationDetail);
    assertThat(list).isEqualTo(result);
  }

  @Test
  public void addDrawing() {
    var form = new PipelineDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);
    padTechnicalDrawingService.addDrawing(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadTechnicalDrawing.class);
    verify(padTechnicalDrawingRepository, times(1)).save(captor.capture());
    verify(padTechnicalDrawingLinkService, times(1)).linkDrawing(pwaApplicationDetail, form.getPadPipelineIds(),
        captor.getValue());

    assertThat(captor.getValue()).extracting(PadTechnicalDrawing::getFile, PadTechnicalDrawing::getReference,
        PadTechnicalDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);
  }

  @Test
  public void getPipelineDrawingSummaryViews_singleOverview() {
    var pipelineDrawing = new PadTechnicalDrawing();
    pipelineDrawing.setReference("ref");
    pipelineDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    pipelineDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    pipelineDrawing.setId(1);

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var drawingLink = new PadTechnicalDrawingLink();
    drawingLink.setTechnicalDrawing(pipelineDrawing);
    drawingLink.setPipeline(pipeline);

    var drawingList = List.of(pipelineDrawing);
    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(drawingList);
    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(drawingList))
        .thenReturn(List.of(drawingLink));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var result = padTechnicalDrawingService.getPipelineDrawingSummaryViewList(pwaApplicationDetail);
    PipelineDrawingSummaryView summaryView = result.get(0);
    assertThat(summaryView.getFileId()).isEqualTo(fileView.getFileId());
    assertThat(summaryView.getDocumentDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(summaryView.getFileName()).isEqualTo(fileView.getFileName());
    assertThat(summaryView.getPipelineReferences()).hasSize(1);
    assertThat(summaryView.getReference()).isEqualTo(pipelineDrawing.getReference());
  }

  @Test
  public void getPipelineDrawingSummaryViews_multipleOverviews() {
    var pipelineDrawing = new PadTechnicalDrawing();
    pipelineDrawing.setReference("ref");
    pipelineDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    pipelineDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    pipelineDrawing.setId(1);

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var pipeline2 = new PadPipeline();
    pipeline2.setPipelineRef("ref");

    var drawingLink = new PadTechnicalDrawingLink();
    drawingLink.setTechnicalDrawing(pipelineDrawing);
    drawingLink.setPipeline(pipeline);
    var drawingLink2 = new PadTechnicalDrawingLink();
    drawingLink2.setTechnicalDrawing(pipelineDrawing);
    drawingLink2.setPipeline(pipeline2);

    var drawingList = List.of(pipelineDrawing);
    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(drawingList);
    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(drawingList))
        .thenReturn(List.of(drawingLink, drawingLink2));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var result = padTechnicalDrawingService.getPipelineDrawingSummaryViewList(pwaApplicationDetail);
    PipelineDrawingSummaryView summaryView = result.get(0);
    assertThat(summaryView.getFileId()).isEqualTo(fileView.getFileId());
    assertThat(summaryView.getDocumentDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(summaryView.getFileName()).isEqualTo(fileView.getFileName());
    assertThat(summaryView.getPipelineReferences()).hasSize(2);
    assertThat(summaryView.getReference()).isEqualTo(pipelineDrawing.getReference());
  }

  @Test
  public void removeDrawing() {
    var drawing = new PadTechnicalDrawing();
    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(drawing));
    padTechnicalDrawingService.removeDrawing(pwaApplicationDetail, 1, new WebUserAccount(1));
    verify(padTechnicalDrawingRepository, times(1)).delete(drawing);
    verify(padFileService, times(1)).processFileDeletion(any(), any());
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeDrawing_drawingNotFound() {
    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.empty());
    padTechnicalDrawingService.removeDrawing(pwaApplicationDetail, 1, new WebUserAccount(1));
    verifyNoInteractions(padTechnicalDrawingRepository);
    verifyNoInteractions(padFileService);
  }

  @Test
  public void getPipelineDrawingSummaryViewFromDrawing_oneResult() {

    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL));
    drawing.setReference("ref");
    drawing.setId(1);

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var link = new PadTechnicalDrawingLink();
    link.setTechnicalDrawing(drawing);
    link.setPipeline(pipeline);

    var uploadedFileView = new UploadedFileView("1", "name", 0L, "desc", Instant.now(), "#");

    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(uploadedFileView));

    var result = padTechnicalDrawingService.getPipelineDrawingSummaryViewFromDrawing(pwaApplicationDetail, drawing);

    assertThat(result.getReference()).isEqualTo(drawing.getReference());
    assertThat(result.getFileName()).isEqualTo(uploadedFileView.getFileName());
    assertThat(result.getFileId()).isEqualTo(drawing.getFileId());
    assertThat(result.getDocumentDescription()).isEqualTo(uploadedFileView.getFileDescription());
    assertThat(result.getDrawingId()).isEqualTo(drawing.getId());

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getPipelineDrawingSummaryViewFromDrawing_noResults() {

    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL));
    drawing.setReference("ref");
    drawing.setId(1);

    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of());

    padTechnicalDrawingService.getPipelineDrawingSummaryViewFromDrawing(pwaApplicationDetail, drawing);

  }

  @Test
  public void getPipelineDrawingSummaryViewList() {

    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL));
    drawing.setReference("ref");
    drawing.setId(1);

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(drawing));

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var link = new PadTechnicalDrawingLink();
    link.setTechnicalDrawing(drawing);
    link.setPipeline(pipeline);

    var uploadedFileView = new UploadedFileView("1", "name", 0L, "desc", Instant.now(), "#");

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing))).thenReturn(List.of(link));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(uploadedFileView));

    var result = padTechnicalDrawingService.getPipelineDrawingSummaryViewList(pwaApplicationDetail);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getReference()).isEqualTo(drawing.getReference());
    assertThat(result.get(0).getFileName()).isEqualTo(uploadedFileView.getFileName());
    assertThat(result.get(0).getFileId()).isEqualTo(drawing.getFileId());
    assertThat(result.get(0).getDocumentDescription()).isEqualTo(uploadedFileView.getFileDescription());
    assertThat(result.get(0).getDrawingId()).isEqualTo(drawing.getId());

  }

  @Test
  public void updateDrawing() {
    var form = new PipelineDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, padFile, "ref");

    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(drawing));

    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);
    padTechnicalDrawingService.updateDrawing(pwaApplicationDetail, 1, new WebUserAccount(), form);

    verify(padTechnicalDrawingRepository, times(1)).delete(drawing);
    verify(padTechnicalDrawingLinkService, times(1)).unlinkDrawing(pwaApplicationDetail, drawing);

    var captor = ArgumentCaptor.forClass(PadTechnicalDrawing.class);
    verify(padTechnicalDrawingRepository, times(1)).save(captor.capture());
    verify(padTechnicalDrawingLinkService, times(1)).linkDrawing(pwaApplicationDetail, form.getPadPipelineIds(),
        captor.getValue());

    assertThat(captor.getValue()).extracting(PadTechnicalDrawing::getFile, PadTechnicalDrawing::getReference,
        PadTechnicalDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);
  }

}