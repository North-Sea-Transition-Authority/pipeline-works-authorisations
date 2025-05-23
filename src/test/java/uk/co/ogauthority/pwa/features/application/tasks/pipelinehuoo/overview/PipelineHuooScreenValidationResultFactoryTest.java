package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;

@ExtendWith(MockitoExtension.class)
class PipelineHuooScreenValidationResultFactoryTest {

  private static final String UNNASSIGNED_PIPELINE_ERROR = "pipeline_error";
  private static final String UNNASSIGNED_ROLE_ERROR = "role_error";
  private static final String INVALID_SECTION_ERROR = "section_error";

  private PipelineHuooScreenValidationResultFactory factory;

  @Mock
  private PipelineHuooValidationResult validationResult;

  @BeforeEach
  void setUp() throws Exception {
    factory = new PipelineHuooScreenValidationResultFactory();

    // by default, no errors
    when(validationResult.getValidationResults()).thenReturn(
        Map.of(
            HuooRole.HOLDER, PipelineHuooRoleValidationResultTestUtil.validResult(),
            HuooRole.USER, PipelineHuooRoleValidationResultTestUtil.validResult(),
            HuooRole.OPERATOR, PipelineHuooRoleValidationResultTestUtil.validResult(),
            HuooRole.OWNER, PipelineHuooRoleValidationResultTestUtil.validResult()
        )
    );
    when(validationResult.isValid()).thenReturn(true);

  }

  @Test
  void createFromValidationResult_whenNoErrors() {

    var summary = factory.createFromValidationResult(validationResult);
    assertThat(summary.getErrorItems()).isEmpty();
    assertThat(summary.getIdPrefix()).isEqualTo("huoo-");
    assertThat(summary.getSectionIncompleteError()).isNotEmpty();
    assertThat(summary.getInvalidObjectIds()).isEmpty();
    assertThat(summary.isSectionComplete()).isTrue();

  }

  @Test
  void createFromValidationResult_isSectionComplete_isSameAsValidationResultIsValid() {
    when(validationResult.isValid()).thenReturn(false);

    var summary = factory.createFromValidationResult(validationResult);
    assertThat(summary.isSectionComplete()).isFalse();

  }

  @Test
  void createFromValidationResult_whenRolesInvalidWithUnassignedPipelinesAndOrgRoles() {
    var roleValidationResultMap = new LinkedHashMap<HuooRole, PipelineHuooRoleValidationResult>();
    roleValidationResultMap.put(
        HuooRole.HOLDER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsUnassigned(UNNASSIGNED_PIPELINE_ERROR, UNNASSIGNED_ROLE_ERROR));
    roleValidationResultMap.put(
        HuooRole.USER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsUnassigned(UNNASSIGNED_PIPELINE_ERROR, UNNASSIGNED_ROLE_ERROR));
    roleValidationResultMap.put(
        HuooRole.OPERATOR,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsUnassigned(UNNASSIGNED_PIPELINE_ERROR, UNNASSIGNED_ROLE_ERROR));
    roleValidationResultMap.put(
        HuooRole.OWNER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsUnassigned(UNNASSIGNED_PIPELINE_ERROR, UNNASSIGNED_ROLE_ERROR));

    when(validationResult.getValidationResults()).thenReturn(roleValidationResultMap);

    var summary = factory.createFromValidationResult(validationResult);
    assertThat(summary.getErrorItems()).containsExactly(
        // these errors require ending space due to ErrorItem message having empty suffix attached by summary object constructor
        new ErrorItem(1, "huoo-HOLDER-UNASSIGNED-PIPELINES", UNNASSIGNED_PIPELINE_ERROR + " "),
        new ErrorItem(2, "huoo-HOLDER-UNASSIGNED-ROLES", UNNASSIGNED_ROLE_ERROR + " "),

        new ErrorItem(3, "huoo-USER-UNASSIGNED-PIPELINES", UNNASSIGNED_PIPELINE_ERROR + " "),
        new ErrorItem(4, "huoo-USER-UNASSIGNED-ROLES", UNNASSIGNED_ROLE_ERROR + " "),

        new ErrorItem(5, "huoo-OPERATOR-UNASSIGNED-PIPELINES", UNNASSIGNED_PIPELINE_ERROR + " "),
        new ErrorItem(6, "huoo-OPERATOR-UNASSIGNED-ROLES", UNNASSIGNED_ROLE_ERROR + " "),

        new ErrorItem(7, "huoo-OWNER-UNASSIGNED-PIPELINES", UNNASSIGNED_PIPELINE_ERROR + " "),
        new ErrorItem(8, "huoo-OWNER-UNASSIGNED-ROLES", UNNASSIGNED_ROLE_ERROR + " ")

    );

  }

  @Test
  void createFromValidationResult_whenRoleInvalidDueToInvalidSections(){
    var roleValidationResultMap = new LinkedHashMap<HuooRole, PipelineHuooRoleValidationResult>();
    roleValidationResultMap.put(
        HuooRole.HOLDER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsBadSection(INVALID_SECTION_ERROR));
    roleValidationResultMap.put(
        HuooRole.USER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsBadSection(INVALID_SECTION_ERROR));
    roleValidationResultMap.put(
        HuooRole.OPERATOR,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsBadSection(INVALID_SECTION_ERROR));
    roleValidationResultMap.put(
        HuooRole.OWNER,
        PipelineHuooRoleValidationResultTestUtil.invalidResultAsBadSection(INVALID_SECTION_ERROR));

    when(validationResult.getValidationResults()).thenReturn(roleValidationResultMap);

    var summary = factory.createFromValidationResult(validationResult);
    assertThat(summary.getErrorItems()).containsExactly(
        // these errors require ending space due to ErrorItem message having empty suffix attached by summary object constructor
        new ErrorItem(1, "huoo-HOLDER-INVALID-SPLITS", INVALID_SECTION_ERROR + " "),

        new ErrorItem(2, "huoo-USER-INVALID-SPLITS", INVALID_SECTION_ERROR + " "),

        new ErrorItem(3, "huoo-OPERATOR-INVALID-SPLITS", INVALID_SECTION_ERROR + " "),

        new ErrorItem(4, "huoo-OWNER-INVALID-SPLITS", INVALID_SECTION_ERROR + " ")

    );

  }


}