package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PipelineDrawingValidatorTest {

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  private PipelineDrawingValidator validator;
  private PipelineDrawingForm form;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipeline pipeline;
  private UploadFileWithDescriptionForm fileForm;

  @BeforeEach
  void setUp() {
    validator = new PipelineDrawingValidator(padPipelineService, padTechnicalDrawingRepository,
        padTechnicalDrawingLinkService, padTechnicalDrawingService);
    form = new PipelineDrawingForm();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pipeline = new PadPipeline();
    pipeline.setId(1);
    pipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

    fileForm = new UploadFileWithDescriptionForm("1", "desc", Instant.now());
  }

  private PadTechnicalDrawingValidationHints getAddDrawingValidationHints() {
   return new PadTechnicalDrawingValidationHints(
        pwaApplicationDetail, null, PipelineDrawingValidationType.ADD);
  }

  private PadTechnicalDrawingValidationHints getEditDrawingValidationHints(PadTechnicalDrawing drawing) {
    return new PadTechnicalDrawingValidationHints(
        pwaApplicationDetail, drawing, PipelineDrawingValidationType.EDIT);
  }

  @Test
  void validate_emptyForm() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getAddDrawingValidationHints());
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds", "uploadedFiles");
  }

  @Test
  void validate_referenceWhitespace() {
    form.setReference(" ");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getAddDrawingValidationHints());
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds");
  }

  @Test
  void validate_invalidPipelineId() {
    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getAddDrawingValidationHints());
    assertThat(result).containsKeys("padPipelineIds");
  }

  @Test
  void validate_existingReference_add() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(pipeline.getPipelineStatus())).thenReturn(true);

    var drawing = new PadTechnicalDrawing();
    drawing.setReference("ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getAddDrawingValidationHints());
    assertThat(result).containsExactly(
        entry("reference", Set.of("reference" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_valid() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(pipeline.getPipelineStatus())).thenReturn(true);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getAddDrawingValidationHints());
    assertThat(result).isEmpty();
  }

  @Test
  void validate_duplicateReference_edit() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(pipeline.getPipelineStatus())).thenReturn(true);

    var drawing = new PadTechnicalDrawing();
    drawing.setReference("ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form,  getEditDrawingValidationHints(drawing));
    assertThat(result).containsExactly(
        entry("reference", Set.of("reference" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_duplicateReference_sameDrawing_edit() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(pipeline.getPipelineStatus())).thenReturn(true);

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getEditDrawingValidationHints(drawing));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_pipelineNotAdded() {
    form.setPadPipelineIds(List.of(1));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of());

    when(padPipelineService.getByIdList(pwaApplicationDetail, List.of(1))).thenReturn(List.of(pipeline));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(pipeline.getPipelineStatus())).thenReturn(true);

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getEditDrawingValidationHints(drawing));
    assertThat(result).extractingFromEntries(Map.Entry::getValue)
        .isNotEmpty()
        .doesNotContain(Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode()));
  }

  @Test
  void validate_pipelineAlreadyAdded() {
    form.setPadPipelineIds(List.of(1));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(
        new PadPipelineKeyDto(1, 1)
    ));

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, getEditDrawingValidationHints(drawing));
    assertThat(result).extractingFromEntries(Map.Entry::getValue)
        .contains(Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode()));
  }
}