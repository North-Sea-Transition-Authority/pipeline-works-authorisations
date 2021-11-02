package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddOrganisationTeamForm;
import uk.co.ogauthority.pwa.model.form.teammanagement.NewTeamForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;

@RunWith(MockitoJUnitRunner.class)
public class TeamCreationServiceTest {

  @Mock
  private SpringValidatorAdapter groupValidator;

  @Mock
  private PortalTeamAccessor portalTeamAccessor;

  @Mock
  private PwaOrganisationAccessor pwaOrganisationAccessor;

  @Mock
  private TeamManagementService teamManagementService;

  private TeamCreationService teamCreationService;


  private AuthenticatedUserAccount user;

  @Before
  public void setup() {
    teamCreationService = new TeamCreationService(
        groupValidator,
        portalTeamAccessor,
        pwaOrganisationAccessor
    );

    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }

  static class NewTeamTestForm implements NewTeamForm {}



  @Test
  public void validate() {

    final var form = new NewTeamTestForm();
    final var bindingResult = new BeanPropertyBindingResult(form, "form");
    final var validationType = ValidationType.FULL;

    teamCreationService.validate(form, bindingResult, validationType);
    verify(groupValidator, times(1)).validate(form, bindingResult, List.of(validationType.getValidationClass()).toArray());
  }





  @Test
  public void getOrCreateOrganisationGroupTeam_whenTeamExists_thenReturnExistingTeam() {

    final var organisationGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "name", "short name");
    final var portalTeamDto = PortalOrganisationTestUtils.createDefaultPortalOrgTeamDto();

    when(pwaOrganisationAccessor.getOrganisationGroupOrError(organisationGroup.getOrgGrpId())).thenReturn(organisationGroup);
    when(portalTeamAccessor.findPortalTeamByOrganisationGroup(organisationGroup)).thenReturn(Optional.of(portalTeamDto));

    final var form = new AddOrganisationTeamForm(organisationGroup.getSelectionId());

    var result = teamCreationService.getOrCreateOrganisationGroupTeam(form, user);

    assertThat(result).isEqualTo(portalTeamDto.getResId());
    verify(portalTeamAccessor, times(0)).createOrganisationGroupTeam(organisationGroup, user);
  }

  @Test
  public void getOrCreateOrganisationGroupTeam_whenTeamDoesntExists_thenCreateNewTeam() {

    final var organisationGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "name", "short name");

    when(pwaOrganisationAccessor.getOrganisationGroupOrError(organisationGroup.getOrgGrpId())).thenReturn(organisationGroup);
    when(portalTeamAccessor.findPortalTeamByOrganisationGroup(organisationGroup)).thenReturn(Optional.empty());

    final var form = new AddOrganisationTeamForm(organisationGroup.getSelectionId());

    var result = teamCreationService.getOrCreateOrganisationGroupTeam(form, user);

    assertThat(result).isNotNull();
    verify(portalTeamAccessor, times(1)).createOrganisationGroupTeam(organisationGroup, user);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getOrCreateOrganisationGroupTeam_whenOrganisationGroupDoesntExist_thenException() {

    final var organisationGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "name", "short name");

    doThrow(new PwaEntityNotFoundException("test"))
        .when(pwaOrganisationAccessor).getOrganisationGroupOrError(organisationGroup.getOrgGrpId());

    final var form = new AddOrganisationTeamForm(organisationGroup.getSelectionId());

    teamCreationService.getOrCreateOrganisationGroupTeam(form, user);

    verify(portalTeamAccessor, times(0)).createOrganisationGroupTeam(organisationGroup, user);

  }


}