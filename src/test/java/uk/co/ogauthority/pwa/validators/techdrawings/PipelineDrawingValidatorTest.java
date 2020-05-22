package uk.co.ogauthority.pwa.validators.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDrawingValidatorTest {

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  private PipelineDrawingValidator validator;
  private PipelineDrawingForm form;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new PipelineDrawingValidator(padPipelineService, padTechnicalDrawingRepository);
    form = new PipelineDrawingForm();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void validate_emptyForm() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail);
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds");
  }

  @Test
  public void validate_referenceWhitespace() {
    form.setReference(" ");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail);
    assertThat(result).containsOnlyKeys("reference", "padPipelineIds");
  }

  @Test
  public void validate_invalidPipelineId() {
    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail);
    assertThat(result).containsKeys("padPipelineIds");
  }

  @Test
  public void validate_valid() {

    form.setPadPipelineIds(List.of(1));
    form.setReference("Test");

    when(padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds()))
        .thenReturn(List.of(new PadPipeline()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail);
    assertThat(result).isEmpty();
  }
}