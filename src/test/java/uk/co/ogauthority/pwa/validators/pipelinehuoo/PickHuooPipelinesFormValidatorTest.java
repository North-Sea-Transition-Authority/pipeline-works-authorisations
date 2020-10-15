package uk.co.ogauthority.pwa.validators.pipelinehuoo;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineType;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PickHuooPipelinesFormValidatorTest {

  private final String FORM_PIPELINES_ATTR = "pickedPipelineStrings";
  private final String FORM_ORG_UNIT_ATTR = "organisationUnitIds";
  private final String FORM_ORG_TREATY_ATTR = "treatyAgreements";
  private final HuooRole HUOO_ROLE = HuooRole.HOLDER;
  private final int VALID_ORG_UNIT_ID = 1;
  private final int INVALID_ORG_UNIT_ID = 2;
  private final TreatyAgreement VALID_TREATY = TreatyAgreement.BELGIUM;
  private final TreatyAgreement INVALID_TREATY = TreatyAgreement.NORWAY;
  private final int VALID_PICKED_PIPELINE_ID = 10;
  private final String VALID_PICKED_PIPELINE_STRING = PickableHuooPipelineType.createPickableString(
      new PipelineId(VALID_PICKED_PIPELINE_ID));

  private final String INVALID_PICKED_PIPELINE_STRING = "SomeDodgyString";

  @Mock
  private PickableHuooPipelineService pickableHuooPipelineService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  private PickHuooPipelinesFormValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private PickHuooPipelinesForm form;

  private BindingResult bindingResult;

  @Before
  public void setup() {

    form = new PickHuooPipelinesForm();

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    validator = new PickHuooPipelinesFormValidator(
        pickableHuooPipelineService,
        padOrganisationRoleService
    );
  }

  @Test
  public void validate_whenPipelineValidationHint_andNoPipelinesPicked() {
    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.PIPELINES);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_PIPELINES_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_PIPELINES_ATTR)))
    );

  }

  @Test
  public void validate_whenPipelineValidationHint_andSomePipelinePicked() {
    form.setPickedPipelineStrings(Set.of(VALID_PICKED_PIPELINE_STRING));
    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.PIPELINES);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).isEmpty();

  }

  @Test
  public void validate_whenOrganisationValidationHint_andNoOrgUnitsPicked_andNoTreatiesPicked_andBothRolesAndTreatiesAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_TREATY_ATTR))),
        entry(FORM_ORG_UNIT_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_UNIT_ATTR)))
    );

  }

  @Test
  public void validate_whenOrganisationValidationHint_andNoOrgUnitsPicked_andNoTreatiesPicked_andOnlyTreatiesAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(false);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_TREATY_ATTR)))
    );

  }


  @Test
  public void validate_whenOrganisationValidationHint_andNoOrgUnitsPicked_andNoTreatiesPicked_andOnlyOrgUnitsAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(false);

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_ORG_UNIT_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_UNIT_ATTR)))
    );

  }

  @Test
  public void validate_whenOrganisationValidationHint_andOrgUnitPicked_andBothRolesAndTreatiesAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);

    form.setOrganisationUnitIds(Set.of(VALID_ORG_UNIT_ID));

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).isEmpty();

  }

  @Test
  public void validate_whenOrganisationValidationHint_andOrgTreatyPicked_andBothRolesAndTreatiesAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);

    form.setTreatyAgreements(Set.of(VALID_TREATY));

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).isEmpty();

  }

  @Test
  public void validate_whenOrganisationValidationHint_andNoOrgUnitsPicked_andNoTreatiesPicked_andNoOrgRolesAvailable() {

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.ORGANISATIONS);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_TREATY_ATTR))),
        entry(FORM_ORG_UNIT_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_UNIT_ATTR)))
    );

  }

  @Test
  public void validate_whenFullValidationHint_andNothingPicked_andOrgUnitsAndTreatyAvailable() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.FULL);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).containsExactly(
        entry(FORM_PIPELINES_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_PIPELINES_ATTR))),
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_TREATY_ATTR))),
        entry(FORM_ORG_UNIT_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(FORM_ORG_UNIT_ATTR)))
    );

  }

  @Test
  public void validate_whenFullValidationHint_andInvalidTreatyPicked() {

    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(pwaApplicationDetail, HUOO_ROLE))
        .thenReturn(
            List.of(
                PadOrganisationRole.fromTreatyAgreement(
                    pwaApplicationDetail,
                    VALID_TREATY,
                    HUOO_ROLE
                )
            )
        );

    form.setPickedPipelineStrings(Set.of(VALID_PICKED_PIPELINE_STRING));
    form.setTreatyAgreements(Set.of(INVALID_TREATY));

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.FULL);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).contains(
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(FORM_ORG_TREATY_ATTR)))
    );

  }

  @Test
  public void validate_whenFullValidationHint_andInvalidTreatyPicked_andValidTreatyAlsoPicked() {

    when(padOrganisationRoleService.hasTreatyRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(pwaApplicationDetail, HUOO_ROLE))
        .thenReturn(
            List.of(
                PadOrganisationRole.fromTreatyAgreement(
                    pwaApplicationDetail,
                    VALID_TREATY,
                    HUOO_ROLE
                )
            )
        );

    form.setPickedPipelineStrings(Set.of(VALID_PICKED_PIPELINE_STRING));
    form.setTreatyAgreements(Set.of(INVALID_TREATY, VALID_TREATY));

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.FULL);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).contains(
        entry(FORM_ORG_TREATY_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(FORM_ORG_TREATY_ATTR)))
    );

  }

  @Test
  public void validate_whenFullValidationHint_andInvalidOrgUnitPicked() {

    when(padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(pwaApplicationDetail, HUOO_ROLE)).thenReturn(true);
    var validPortalOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(VALID_ORG_UNIT_ID, "name", null);
    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(pwaApplicationDetail, HUOO_ROLE))
        .thenReturn(
            List.of(
                PadOrganisationRole.fromOrganisationUnit(
                    pwaApplicationDetail,
                    validPortalOrgUnit,
                    HUOO_ROLE
                )
            )
        );

    form.setPickedPipelineStrings(Set.of(VALID_PICKED_PIPELINE_STRING));
    form.setOrganisationUnitIds(Set.of(INVALID_ORG_UNIT_ID));

    bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator( validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE, PickHuooPipelineValidationType.FULL);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errorCodeMap).contains(
        entry(FORM_ORG_UNIT_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(FORM_ORG_UNIT_ATTR)))
    );

  }

  @Test
  public void validate_whenFullValidationHint_andInvalidPipelinePicked_whereTheStringFormatIsValid_andPickedPipelineInvalid() {
    var invalidPickedPipelineStringWithValidFormat = PickableHuooPipelineType.createPickableString(new PipelineId(999));

    form.setTreatyAgreements(Set.of(VALID_TREATY));
    form.setPickedPipelineStrings(Set.of(invalidPickedPipelineStringWithValidFormat));

    bindingResult = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, bindingResult, pwaApplicationDetail, HUOO_ROLE,
        PickHuooPipelineValidationType.FULL);
    var errorCodeMap = ValidatorTestUtils.extractErrors(bindingResult);

    verify(pickableHuooPipelineService, times(1)).reconcilePickablePipelinesFromStrings(
        pwaApplicationDetail,
        HUOO_ROLE,
        Set.of(invalidPickedPipelineStringWithValidFormat));

    assertThat(errorCodeMap).contains(
        entry(FORM_PIPELINES_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(FORM_PIPELINES_ATTR)))
    );

  }

}