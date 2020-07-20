package uk.co.ogauthority.pwa.validators.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadPipelineKeyDto;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDrawingValidatorTest {

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  private PipelineDrawingValidator validator;
  private PipelineDrawingForm form;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new PipelineDrawingValidator(padPipelineService, padTechnicalDrawingRepository,
        padTechnicalDrawingLinkService);
    form = new PipelineDrawingForm();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void validate_emptyForm() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, null,
        PipelineDrawingValidationType.ADD);
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds");
  }

  @Test
  public void validate_referenceWhitespace() {
    form.setReference(" ");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, null,
        PipelineDrawingValidationType.ADD);
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds");
  }

  @Test
  public void validate_invalidPipelineId() {
    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, null,
        PipelineDrawingValidationType.ADD);
    assertThat(result).containsKeys("padPipelineIds");
  }

  @Test
  public void validate_existingReference_add() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);
    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    var drawing = new PadTechnicalDrawing();
    drawing.setReference("ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.ADD);
    assertThat(result).containsExactly(
        entry("reference", Set.of("reference" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void validate_valid() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);
    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, null,
        PipelineDrawingValidationType.ADD);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_duplicateReference_edit() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);
    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    var drawing = new PadTechnicalDrawing();
    drawing.setReference("ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.EDIT);
    assertThat(result).containsExactly(
        entry("reference", Set.of("reference" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void validate_duplicateReference_sameDrawing_edit() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("ref");

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);
    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(pipeline));

    var existingDrawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingDrawing));

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.EDIT);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_pipelineNotAdded() {
    form.setPadPipelineIds(List.of(1));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of());

    var pipeline = new PadPipeline(pwaApplicationDetail);
    pipeline.setId(1);
    when(padPipelineService.getByIdList(pwaApplicationDetail, List.of(1))).thenReturn(List.of(pipeline));

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.EDIT);
    assertThat(result).extractingFromEntries(Map.Entry::getValue)
        .doesNotContain(Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode()));
  }

  @Test
  public void validate_pipelineAlreadyAdded() {
    form.setPadPipelineIds(List.of(1));
    when(padTechnicalDrawingLinkService.getLinkedPipelineIds(pwaApplicationDetail)).thenReturn(List.of(
        new PadPipelineKeyDto(1, 1)
    ));

    var drawing = new PadTechnicalDrawing(1, pwaApplicationDetail, null, "ref");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.EDIT);
    assertThat(result).extractingFromEntries(Map.Entry::getValue)
        .contains(Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode()));
  }
}