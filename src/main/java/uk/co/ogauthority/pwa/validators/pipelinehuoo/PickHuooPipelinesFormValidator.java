package uk.co.ogauthority.pwa.validators.pipelinehuoo;


import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOption;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineService;

@Service
public class PickHuooPipelinesFormValidator implements SmartValidator {

  private final PickablePipelineService pickablePipelineService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PickHuooPipelinesFormValidator(
      PickablePipelineService pickablePipelineService,
      PadOrganisationRoleService padOrganisationRoleService) {

    this.pickablePipelineService = pickablePipelineService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return PickHuooPipelinesForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, new Object[0]);

  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (PickHuooPipelinesForm) target;
    var validationType = Arrays.stream(validationHints)
        .filter(o -> o.getClass().equals(PickHuooPipelineValidationType.class))
        .map(o -> (PickHuooPipelineValidationType) o)
        .findFirst()
        .orElseThrow(() -> new ActionNotAllowedException("Expected validation type to be provided"));

    var huooRole = Arrays.stream(validationHints)
        .filter(o -> HuooRole.class.equals(o.getClass()))
        .map(o -> (HuooRole) o)
        .findFirst()
        .orElseThrow(() -> new ActionNotAllowedException("Expected huoo role hint to be provided"));

    var pwaApplicationDetail = Arrays.stream(validationHints)
        .filter(o -> PwaApplicationDetail.class.equals(o.getClass()))
        .map(o -> (PwaApplicationDetail) o)
        .findFirst()
        .orElseThrow(() -> new ActionNotAllowedException("Expected application detail hint to be provided"));

    if (validationType.equals(PickHuooPipelineValidationType.ORGANISATIONS)) {
      validateOrgs(form, errors);
    } else if (validationType.equals(PickHuooPipelineValidationType.PIPELINES)) {
      validatePipelines(form, errors);
    } else {
      validateFull(pwaApplicationDetail, form, huooRole, errors);
    }

  }

  private void validateOrgs(PickHuooPipelinesForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "organisationUnitIds", "organisationUnitIds.required",
        "You must select at least one organisation");
  }

  private void validatePipelines(PickHuooPipelinesForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "pickedPipelineStrings", "pickedPipelineStrings.required",
        "You must select at least one pipeline");
  }

  private void validateFull(PwaApplicationDetail pwaApplicationDetail,
                            PickHuooPipelinesForm form,
                            HuooRole huooRole,
                            Errors errors) {

    validateOrgs(form, errors);
    validatePipelines(form, errors);

    // do sanity checking of pipeline data and organisation role
    if (!errors.hasErrors()) {
      var validOrgUnitIdsForRole = padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail).stream()
          .filter(padOrganisationRole -> huooRole.equals(padOrganisationRole.getRole()))
          // attempt to obey demeter, probably without merit.
          .map(o -> OrganisationUnitId.from(o.getOrganisationUnit()))
          .map(OrganisationUnitId::asInt)
          .collect(toSet());

      if (form.getOrganisationUnitIds().stream().anyMatch(ouId -> !validOrgUnitIdsForRole.contains(ouId))) {
        errors.rejectValue("organisationUnitIds", "organisationUnitIds.invalid", "Select valid organisation units");
      }

      var validPickablePipelineIds = pickablePipelineService.getAllPickablePipelinesForApplication(pwaApplicationDetail)
          .stream()
          .map(PickablePipelineOption::getPickableString)
          .collect(toSet());

      if (form.getPickedPipelineStrings().stream().anyMatch(
          pipelineString -> !validPickablePipelineIds.contains(pipelineString))) {
        errors.rejectValue("pickedPipelineStrings", "pickedPipelineStrings.invalid", "Select valid pipelines");
      }
    }

  }
}
