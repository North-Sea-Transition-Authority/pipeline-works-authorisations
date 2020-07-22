package uk.co.ogauthority.pwa.validators.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipelineDto;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ModifyPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ModifyPipelineValidatorTest {

  @Mock
  private ModifyPipelineService modifyPipelineService;

  private ModifyPipelineValidator modifyPipelineValidator;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    modifyPipelineValidator = new ModifyPipelineValidator(modifyPipelineService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
  }

  @Test
  public void validate_formValid() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);
    pipelineDetail.setPipeline(pipeline);
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(
        List.of(new NamedPipelineDto(pipelineDetail))
    );
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).isEmpty();
  }

  @Test
  public void validate_idNotSelectable() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(2);
    pipelineDetail.setPipeline(pipeline);
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(
        List.of(new NamedPipelineDto(pipelineDetail))
    );
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).containsExactly(
        entry("pipelineId", Set.of("pipelineId" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

}