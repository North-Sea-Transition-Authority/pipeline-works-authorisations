package uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.NamedPipelineDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class ModifyPipelineValidatorTest {

  @Mock
  private ModifyPipelineService modifyPipelineService;

  private ModifyPipelineValidator modifyPipelineValidator;
  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {
    modifyPipelineValidator = new ModifyPipelineValidator(modifyPipelineService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
  }

  @Test
  void validate_formValid() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);
    pipelineDetail.setPipeline(pipeline);
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(
        List.of(NamedPipelineDto.fromPipelineDetail(pipelineDetail))
    );
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    form.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).isEmpty();
  }

  @Test
  void validate_idNotSelectable() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(2);
    pipelineDetail.setPipeline(pipeline);
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(
        List.of(NamedPipelineDto.fromPipelineDetail(pipelineDetail))
    );
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("pipelineId", Set.of("pipelineId" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_pipelineStatus_notSelected() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("pipelineStatus", Set.of("pipelineStatus" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_pipelineStatus_historical() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.PENDING);
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("pipelineStatus", Set.of("pipelineStatus" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_pipelineStatus_selected() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).doesNotContain(
        entry("pipelineStatus", Set.of("pipelineStatus" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_pipelineStatusReason_invalid() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("outOfUseStatusReason", Set.of("outOfUseStatusReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_pipelineStatusReason_over4k() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    form.setOutOfUseStatusReason("a".repeat(4001));
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("outOfUseStatusReason", Set.of("outOfUseStatusReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_pipelineStatusReason_valid() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    form.setOutOfUseStatusReason("reason");
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).doesNotContain(
        entry("pipelineStatusReason", Set.of("pipelineStatusReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_transferStatusSelected_confirmationNull_valid() {
    when(modifyPipelineService.getSelectableConsentedPipelines(detail)).thenReturn(List.of());
    var form = new ModifyPipelineForm();
    form.setPipelineStatus(PipelineStatus.TRANSFERRED);
    var errors = ValidatorTestUtils.getFormValidationErrors(modifyPipelineValidator, form, detail);
    assertThat(errors).contains(
        entry("transferAgreed", Set.of("transferAgreed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transferStatusReason", Set.of("transferStatusReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

}
