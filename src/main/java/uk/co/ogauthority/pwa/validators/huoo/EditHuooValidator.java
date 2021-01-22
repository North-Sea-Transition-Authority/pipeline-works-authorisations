package uk.co.ogauthority.pwa.validators.huoo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;


@Service
public class EditHuooValidator implements SmartValidator {

  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;

  @Autowired
  public EditHuooValidator(
      PadOrganisationRoleService padOrganisationRoleService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      TeamService teamService) {
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(HuooForm.class);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (HuooForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    var huooValidationView = (HuooValidationView) validationHints[1];
    var roles = padOrganisationRoleService.getOrgRolesForDetail(detail);
    if (form.getHuooType() == null) {
      errors.rejectValue("huooType", "huooType.required",
          "Select the entity type");
    }

    // TREATY type automatically assigned to USER role, so only relevant here
    if (form.getHuooType() == HuooType.PORTAL_ORG && SetUtils.emptyIfNull(form.getHuooRoles()).isEmpty()) {
      errors.rejectValue("huooRoles", "huooRoles.required",
          "Select one or more roles");
    }

    Optional<OrganisationUnitId> formOrgUnitIdOpt = Optional.ofNullable(form.getOrganisationUnitId())
        .map(OrganisationUnitId::fromInt);

    if (form.getHuooType() == HuooType.PORTAL_ORG && formOrgUnitIdOpt.isEmpty()) {
      errors.rejectValue("organisationUnitId", "organisationUnitId.required",
          "Select an organisation");
    } else if (form.getHuooType() == HuooType.PORTAL_ORG && formOrgUnitIdOpt.isPresent()) {
      var formOrgUnitId = formOrgUnitIdOpt.get();

      if (!portalOrganisationsAccessor.organisationUnitExistsForId(formOrgUnitId)) {
        errors.rejectValue("organisationUnitId",
            "organisationUnitId" + FieldValidationErrorCodes.INVALID.getCode(),
            "The selected organisation is invalid");
      }

      boolean holderSelected = SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)
          || (!SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)
          && SetUtils.emptyIfNull(huooValidationView.getRoles()).contains(HuooRole.HOLDER));

      if (validationHints.length >= 3 && validationHints[2] instanceof AuthenticatedUserAccount
          && detail.getPwaApplicationType().equals(PwaApplicationType.INITIAL) && holderSelected) {

        var user = (AuthenticatedUserAccount) validationHints[2];
        var userCanAccessOrgUnit = getOrgUnitsUserCanAccess(user).stream()
            .anyMatch(organisationUnit -> formOrgUnitId.equals(OrganisationUnitId.from(organisationUnit)));

        if (!userCanAccessOrgUnit) {
          errors.rejectValue("organisationUnitId",
              "organisationUnitId" + FieldValidationErrorCodes.INVALID.getCode(),
              "You must be a member of this organisation's team to assign this organisation");
        }
      }

      roles.stream()
          .filter(role -> role.getType().equals(HuooType.PORTAL_ORG))
          .filter(
              // we aren't editing an org at all, but a treaty
              padOrganisationRole -> huooValidationView.getPortalOrganisationUnit() == null
                  || (padOrganisationRole.getOrganisationUnit().getOuId() != huooValidationView.getPortalOrganisationUnit().getOuId()))
          .filter(padOrganisationRole ->
              formOrgUnitId.equals(OrganisationUnitId.from(padOrganisationRole.getOrganisationUnit()))
          )
          .findAny()
          .ifPresent(padOrganisationRole -> errors.rejectValue("organisationUnitId", "organisationUnitId.alreadyUsed",
              "The selected organisation is already added to the application"));
    }

    var holderCount = roles.stream()
        .filter(padOrgRole -> padOrgRole.getType().equals(huooValidationView.getHuooType()))
        .filter(padOrgRole -> padOrgRole.getType().equals(HuooType.PORTAL_ORG))
        .filter(padOrgRole -> padOrgRole.getRole().equals(HuooRole.HOLDER))
        .filter(
            padOrgRole -> padOrgRole.getOrganisationUnit().getOuId() != huooValidationView.getPortalOrganisationUnit().getOuId())
        .count();
    if (holderCount >= detail.getNumOfHolders()) {
      if (SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
        var holdersTxt = detail.getNumOfHolders() > 1 ? "holders" : "holder";
        errors.rejectValue("huooRoles", "huooRoles.alreadyUsed",
            "You may only have " + detail.getNumOfHolders() + " " + holdersTxt + " on an application");
      }
    }

    if (huooValidationView.getHuooType() != form.getHuooType()) {
      errors.rejectValue("huooType", "huooType.differentType",
          "Entity cannot have a different type");
    }

    var orgWasHolder = roles.stream()
        .filter(padOrgRole -> padOrgRole.getType().equals(huooValidationView.getHuooType()))
        .filter(padOrgRole -> padOrgRole.getType().equals(HuooType.PORTAL_ORG))
        .filter(
            padOrgRole -> padOrgRole.getOrganisationUnit().getOuId() == huooValidationView.getPortalOrganisationUnit().getOuId())
        .anyMatch(padOrgRole -> padOrgRole.getRole().equals(HuooRole.HOLDER));

    if (orgWasHolder) {
      if (!SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER) && holderCount == 0) {
        errors.rejectValue("huooRoles", "huooRoles.requiresOneHolder",
            "You can't remove the final holder on an application");
      }
    }
  }


  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {
    var orgGroupsUserCanAccess = teamService.getOrganisationTeamListIfPersonInRole(
        user.getLinkedPerson(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());
    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(orgGroupsUserCanAccess);
  }

}
