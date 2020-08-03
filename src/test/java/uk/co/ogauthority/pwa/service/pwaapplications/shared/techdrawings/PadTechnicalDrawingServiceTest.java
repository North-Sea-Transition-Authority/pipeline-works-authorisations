package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
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

  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipeline padPipelineForAppOverviewA;
  private PadPipeline padPipelineForAppOverviewB;


  @Before
  public void setUp() {
    padTechnicalDrawingService = new PadTechnicalDrawingService(padTechnicalDrawingRepository,
        padTechnicalDrawingLinkService, padFileService, padPipelineService, pipelineDrawingValidator,
        springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

    padPipelineForAppOverviewA = new PadPipeline(pwaApplicationDetail);
    padPipelineForAppOverviewA.setId(1);
    padPipelineForAppOverviewB = new PadPipeline(pwaApplicationDetail);
    padPipelineForAppOverviewB.setId(2);

    var pipelineA = new Pipeline();
    pipelineA.setId(1);
    var pipelineB = new Pipeline();
    pipelineB.setId(2);

    padPipelineForAppOverviewA.setPipeline(pipelineA);
    padPipelineForAppOverviewB.setPipeline(pipelineB);
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
  public void removeDrawing_noFileLinked() {
    var drawing = new PadTechnicalDrawing();
    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(drawing));
    padTechnicalDrawingService.removeDrawing(pwaApplicationDetail, 1, new WebUserAccount(1));
    verify(padTechnicalDrawingRepository, times(1)).delete(drawing);
    verify(padFileService, never()).processFileDeletionWithPreDeleteAction(any(), any(), any());
  }

  @Test
  public void removeDrawing_fileLinked() {
    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile());
    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(drawing));
    padTechnicalDrawingService.removeDrawing(pwaApplicationDetail, 1, new WebUserAccount(1));
    verify(padTechnicalDrawingRepository, times(1)).delete(drawing);
    verify(padFileService, times(1)).processFileDeletion(eq(drawing.getFile()), any());
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
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
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
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    drawing.setReference("ref");
    drawing.setId(1);

    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of());

    padTechnicalDrawingService.getPipelineDrawingSummaryViewFromDrawing(pwaApplicationDetail, drawing);

  }

  @Test
  public void getPipelineDrawingSummaryViewList() {

    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
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

    verify(padTechnicalDrawingLinkService, times(1)).unlinkDrawing(pwaApplicationDetail, drawing);

    var captor = ArgumentCaptor.forClass(PadTechnicalDrawing.class);
    verify(padTechnicalDrawingRepository, times(1)).save(captor.capture());
    verify(padTechnicalDrawingLinkService, times(1)).linkDrawing(pwaApplicationDetail, form.getPadPipelineIds(),
        captor.getValue());

    assertThat(captor.getValue()).extracting(PadTechnicalDrawing::getFile, PadTechnicalDrawing::getReference,
        PadTechnicalDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);
  }

  @Test
  public void validateSection_valid() {
    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(pipeline);

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(any())).thenReturn(List.of(link));
    when(padPipelineService.getPipelines(any())).thenReturn(List.of(pipeline));

    BindingResult bindingResult = new BeanPropertyBindingResult(null, "form");

    padTechnicalDrawingService.validateSection(bindingResult, pwaApplicationDetail);

    assertThat(bindingResult.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void validateSection_invalid() {
    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);

    var pipeline2 = new PadPipeline(pwaApplicationDetail);
    pipeline2.setId(2);

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(pipeline);

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(any())).thenReturn(List.of(link));
    when(padPipelineService.getPipelines(any())).thenReturn(List.of(pipeline, pipeline2));

    BindingResult bindingResult = new BeanPropertyBindingResult(null, "form");

    padTechnicalDrawingService.validateSection(bindingResult, pwaApplicationDetail);

    assertThat(bindingResult.getAllErrors()).extracting(ObjectError::getCode)
        .containsExactly("allPipelinesAdded" + FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void getDrawingLinkedToPadFile() {
    var optionalDrawing = Optional.of(new PadTechnicalDrawing());
    var padFile = new PadFile();
    when(padTechnicalDrawingRepository.findByPwaApplicationDetailAndFile(pwaApplicationDetail, padFile))
        .thenReturn(optionalDrawing);
    var result = padTechnicalDrawingService.getDrawingLinkedToPadFile(pwaApplicationDetail, padFile);
    assertThat(result).isEqualTo(optionalDrawing);
  }

  @Test
  public void unlinkFile() {
    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, new PadFile(), "ref");
    padTechnicalDrawingService.unlinkFile(drawing);
    assertThat(drawing.getFile()).isNull();
    verify(padTechnicalDrawingRepository, times(1)).save(drawing);
  }

  @Test
  public void allPipelinesLinked_allPipelinesLinked() {
    var drawing = new PadTechnicalDrawing();

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(pipeline);
    link.setTechnicalDrawing(drawing);

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing))).thenReturn(List.of(link));
    when(padPipelineService.getPipelines(pwaApplicationDetail)).thenReturn(List.of(pipeline));

    var result = padTechnicalDrawingService.allPipelinesLinked(pwaApplicationDetail, List.of(drawing));
    assertThat(result).isTrue();
  }

  @Test
  public void allPipelinesLinked_notAllPipelinesLinked() {
    var drawing = new PadTechnicalDrawing();

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);

    var pipeline2 = new PadPipeline(pwaApplicationDetail);
    pipeline2.setId(2);

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(pipeline);
    link.setTechnicalDrawing(drawing);

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing))).thenReturn(List.of(link));
    when(padPipelineService.getPipelines(pwaApplicationDetail)).thenReturn(List.of(pipeline, pipeline2));

    var result = padTechnicalDrawingService.allPipelinesLinked(pwaApplicationDetail, List.of(drawing));
    assertThat(result).isFalse();
  }

  @Test
  public void drawingsValid_valid() {
    var drawing = new PadTechnicalDrawing();
    drawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL));

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(drawing));

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(pipeline);
    link.setTechnicalDrawing(drawing);

    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing))).thenReturn(List.of(link));
    when(padPipelineService.getPipelines(pwaApplicationDetail)).thenReturn(List.of(pipeline));

    var result = padTechnicalDrawingService.drawingsValid(pwaApplicationDetail);
    assertThat(result).isTrue();
  }

  @Test
  public void drawingsValid_invalid() {
    var drawing = new PadTechnicalDrawing();

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(drawing));

    var result = padTechnicalDrawingService.drawingsValid(pwaApplicationDetail);
    assertThat(result).isFalse();
  }

  @Test
  public void getValidationFactory_ensureIsComplete_true() {

    // Call to drawingsValid to provide mocks for valid data.
    drawingsValid_valid();
    var validationFactory = padTechnicalDrawingService.getValidationFactory(pwaApplicationDetail);
    assertThat(validationFactory.isComplete()).isTrue();
  }

  @Test
  public void getValidationFactory_ensureIsComplete_false() {

    // Call to drawingsValid to provide mocks for invalid data.
    drawingsValid_invalid();
    var validationFactory = padTechnicalDrawingService.getValidationFactory(pwaApplicationDetail);
    assertThat(validationFactory.isComplete()).isFalse();
  }

  @Test
  public void cleanupData() {

    var drawing1 = new PadTechnicalDrawing();
    var file1 = new PadFile();
    file1.setId(1);
    drawing1.setFile(file1);

    var drawing2 = new PadTechnicalDrawing();
    var file2 = new PadFile();
    file2.setId(2);
    drawing2.setFile(file2);

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(drawing1, drawing2));

    padTechnicalDrawingService.cleanupData(pwaApplicationDetail);

    verify(padFileService, times(1)).cleanupFiles(eq(pwaApplicationDetail),
        eq(ApplicationFilePurpose.PIPELINE_DRAWINGS), eq(List.of(1, 2)));

  }

  @Test
  public void getUnlinkedApplicationPipelineOverviews_allPipelinesLinked() {
    var pipelineOverviewA = new PadPipelineOverview(padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(
        new PadPipelineKeyDto(4, 1),
        new PadPipelineKeyDto(5, 2)
    ));
    var result = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail);
    assertThat(result).isEmpty();
  }

  @Test
  public void getUnlinkedApplicationPipelineOverviews_somePipelinesLinked() {

    var pipelineOverviewA = new PadPipelineOverview(padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(
        new PadPipelineKeyDto(4, 1)
    ));
    var result = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail);
    assertThat(result).containsExactly(pipelineOverviewB);
  }

  @Test
  public void getUnlinkedApplicationPipelineOverviews_noPipelinesLinked() {var pipelineOverviewA = new PadPipelineOverview(
      padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of());
    var result = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail);
    assertThat(result).containsExactly(pipelineOverviewA, pipelineOverviewB);
  }

  @Test
  public void getUnlinkedAndSpecificApplicationPipelineOverviews_allPipelinesLinked() {var pipelineOverviewA = new PadPipelineOverview(
      padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(
        new PadPipelineKeyDto(4, 1),
        new PadPipelineKeyDto(5, 2)
    ));
    var result = padTechnicalDrawingService.getUnlinkedAndSpecificApplicationPipelineOverviews(pwaApplicationDetail,
        List.of(1));
    assertThat(result).containsExactly(pipelineOverviewA);
  }

  @Test
  public void getUnlinkedAndSpecificApplicationPipelineOverviews_excludeNonLinked() {var pipelineOverviewA = new PadPipelineOverview(
      padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(
        List.of(new PadPipelineKeyDto(4, 1)));
    var result = padTechnicalDrawingService.getUnlinkedAndSpecificApplicationPipelineOverviews(pwaApplicationDetail,
        List.of(2));
    assertThat(result).containsExactly(pipelineOverviewB);
  }

  @Test
  public void getUnlinkedAndSpecificApplicationPipelineOverviews_noPipelinesLinked() {var pipelineOverviewA = new PadPipelineOverview(
      padPipelineForAppOverviewA, 1L);
    var pipelineOverviewB = new PadPipelineOverview(padPipelineForAppOverviewB, 1L);
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipelineOverviewA, pipelineOverviewB));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of());
    var result = padTechnicalDrawingService.getUnlinkedAndSpecificApplicationPipelineOverviews(pwaApplicationDetail,
        List.of(2));
    assertThat(result).containsExactly(pipelineOverviewA, pipelineOverviewB);
  }

  @Test
  public void cleanUnlinkedDrawings_serviceInteraction_noLinks() {
    var drawing = new PadTechnicalDrawing();
    drawing.setId(1);
    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(drawing));
    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing)))
        .thenReturn(List.of());
    padTechnicalDrawingService.cleanUnlinkedDrawings(pwaApplicationDetail);
    verify(padTechnicalDrawingRepository, times(1)).deleteAll(List.of(drawing));
  }

  @Test
  public void cleanUnlinkedDrawings_serviceInteraction_remainingLinks() {
    var drawing = new PadTechnicalDrawing();
    drawing.setId(1);
    var link = new PadTechnicalDrawingLink();
    link.setTechnicalDrawing(drawing);
    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(drawing));
    when(padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing)))
        .thenReturn(List.of(link));
    padTechnicalDrawingService.cleanUnlinkedDrawings(pwaApplicationDetail);
    verify(padTechnicalDrawingRepository, never()).deleteAll(any());
  }

}