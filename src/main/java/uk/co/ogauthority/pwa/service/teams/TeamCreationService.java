package uk.co.ogauthority.pwa.service.teams;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamDto;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddOrganisationTeamForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;

@Service
public class TeamCreationService {

  private final SpringValidatorAdapter groupValidator;
  private final PortalTeamAccessor portalTeamAccessor;
  private final PwaOrganisationAccessor pwaOrganisationAccessor;

  @Autowired
  public TeamCreationService(SpringValidatorAdapter groupValidator,
                             PortalTeamAccessor portalTeamAccessor,
                             PwaOrganisationAccessor pwaOrganisationAccessor) {
    this.groupValidator = groupValidator;
    this.portalTeamAccessor = portalTeamAccessor;
    this.pwaOrganisationAccessor = pwaOrganisationAccessor;
  }

  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType) {

    groupValidator.validate(form, bindingResult, List.of(validationType.getValidationClass()).toArray());
    return bindingResult;
  }


  public Integer getOrCreateOrganisationGroupTeam(AddOrganisationTeamForm form,
                                                  AuthenticatedUserAccount user) {

    var organisationGroup = pwaOrganisationAccessor.getOrganisationGroupOrError(
        Integer.parseInt(form.getOrganisationGroup())
    );

    var organisationTeam = portalTeamAccessor.findPortalTeamByOrganisationGroup(organisationGroup);

    return organisationTeam.map(PortalTeamDto::getResId).orElseGet(
        () -> portalTeamAccessor.createOrganisationGroupTeam(organisationGroup, user));
  }


}