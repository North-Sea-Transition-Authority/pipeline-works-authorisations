package uk.co.ogauthority.pwa.validators.pipelinehuoo;


import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.ReconciledHuooPickablePipeline;

@Service
public class PickHuooPipelinesFormValidator implements SmartValidator {

  private final PickableHuooPipelineService pickableHuooPipelineService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PickHuooPipelinesFormValidator(
      PickableHuooPipelineService pickableHuooPipelineService,
      PadOrganisationRoleService padOrganisationRoleService) {

    this.pickableHuooPipelineService = pickableHuooPipelineService;
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

    var orgValidationHint = getOrganisationValidationHint(pwaApplicationDetail, huooRole);

    if (validationType.equals(PickHuooPipelineValidationType.ORGANISATIONS)) {

      validateOrgsBasic(form, orgValidationHint, errors);
    } else if (validationType.equals(PickHuooPipelineValidationType.PIPELINES)) {
      validatePipelinesBasic(form, errors);
    } else {
      validateFull(pwaApplicationDetail, form, huooRole, orgValidationHint, errors);
    }

  }

  private OrganisationValidationHint getOrganisationValidationHint(PwaApplicationDetail pwaApplicationDetail,
                                                                   HuooRole huooRole) {
    var hasOrgUnitRoleOwners = padOrganisationRoleService.hasOrganisationUnitRoleOwnersInRole(
        pwaApplicationDetail, huooRole
    );
    var hasTreatyRoleOwners = padOrganisationRoleService.hasTreatyRoleOwnersInRole(
        pwaApplicationDetail, huooRole
    );

    if (hasOrgUnitRoleOwners && hasTreatyRoleOwners) {
      return OrganisationValidationHint.BOTH;
    } else if (hasOrgUnitRoleOwners) {
      return OrganisationValidationHint.ONLY_ORG_UNITS;
    } else if (hasTreatyRoleOwners) {
      return OrganisationValidationHint.ONLY_TREATIES;
    }

    // default to both if no roles found.
    return OrganisationValidationHint.BOTH;


  }

  private void validateOrgsBasic(PickHuooPipelinesForm form,
                                 OrganisationValidationHint organisationValidationHint,
                                 Errors errors) {

    if (organisationValidationHint.equals(OrganisationValidationHint.ONLY_ORG_UNITS)
        && SetUtils.emptyIfNull(form.getOrganisationUnitIds()).isEmpty()) {
      errors.rejectValue(
          "organisationUnitIds",
          "organisationUnitIds.required",
          "Select at least one organisation");
      return;
    }

    if (organisationValidationHint.equals(OrganisationValidationHint.ONLY_TREATIES)
        && SetUtils.emptyIfNull(form.getTreatyAgreements()).isEmpty()) {
      errors.rejectValue(
          "treatyAgreements",
          "treatyAgreements.required",
          "Select at least one treaty");
      return;
    }

    if (SetUtils.emptyIfNull(form.getOrganisationUnitIds()).isEmpty() && SetUtils.emptyIfNull(
        form.getTreatyAgreements()).isEmpty()) {

      errors.rejectValue(
          "treatyAgreements",
          "treatyAgreements.required",
          "Select at least one treaty if no organisation selected");

      errors.rejectValue(
          "organisationUnitIds",
          "organisationUnitIds.required",
          "Select at least one organisation if no treaty selected");
    }
  }

  private void validatePipelinesBasic(PickHuooPipelinesForm form, Errors errors) {
    if (SetUtils.emptyIfNull(form.getPickedPipelineStrings()).isEmpty()) {
      errors.rejectValue("pickedPipelineStrings", "pickedPipelineStrings.required",
          "Select at least one pipeline");

    }
  }

  private void validateFull(PwaApplicationDetail pwaApplicationDetail,
                            PickHuooPipelinesForm form,
                            HuooRole huooRole,
                            OrganisationValidationHint organisationValidationHint,
                            Errors errors) {

    validateOrgsBasic(form, organisationValidationHint, errors);
    validatePipelinesBasic(form, errors);

    // do sanity checking of pipeline data and organisation role
    if (!errors.hasErrors()) {
      var allPadOrgRoles = padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(pwaApplicationDetail, huooRole);
      var validOrgUnitIdsForRole = allPadOrgRoles.stream()
          // attempt to obey demeter, probably without merit.
          .filter(o -> o.getType().equals(HuooType.PORTAL_ORG))
          .map(o -> OrganisationUnitId.from(o.getOrganisationUnit()))
          .map(OrganisationUnitId::asInt)
          .collect(toSet());

      // ignore SelectableTreatyRolesValidationHint to use a consistent method for validity checking when doing full validation.
      var validTreaties = allPadOrgRoles.stream()
          // attempt to obey demeter, probably without merit.
          .filter(o -> o.getType().equals(HuooType.TREATY_AGREEMENT))
          .map(PadOrganisationRole::getAgreement)
          .collect(toSet());

      // add treaty invalid error if
      if (
          // if valid treaties has possible values, but some selected treaty does not exist within that set
          (!validTreaties.isEmpty()
              // need to use anyMatch here so we error if any of the picked treaties is not valid.
              && form.getTreatyAgreements().stream().anyMatch(treaty -> !validTreaties.contains(treaty))
          )
              // or no valid treaties, but some treaty selected
              || (validTreaties.isEmpty() && !form.getTreatyAgreements().isEmpty())
      ) {
        errors.rejectValue("treatyAgreements", "treatyAgreements.invalid", "Select a valid treaty");
      }


      if (!validOrgUnitIdsForRole.isEmpty()
          && form.getOrganisationUnitIds().stream().anyMatch(ouId -> !validOrgUnitIdsForRole.contains(ouId))) {
        errors.rejectValue("organisationUnitIds", "organisationUnitIds.invalid", "Select valid organisation units");
      }


      var validPickablePipelineIds = pickableHuooPipelineService.reconcilePickablePipelinesFromStrings(
          pwaApplicationDetail,
          huooRole,
          form.getPickedPipelineStrings()
      )
          .stream()
          .map(ReconciledHuooPickablePipeline::getPickableIdAsString)
          .collect(toSet());

      if (form.getPickedPipelineStrings().stream().anyMatch(
          pipelineString -> !validPickablePipelineIds.contains(pipelineString))) {
        errors.rejectValue("pickedPipelineStrings", "pickedPipelineStrings.invalid", "Select valid pipelines");
      }
    }

  }

  private enum OrganisationValidationHint {
    ONLY_TREATIES, ONLY_ORG_UNITS, BOTH
  }

}
