package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.AssertionTestUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationPermissionServiceTest {

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private TeamService teamService;

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  private PwaApplicationPermissionService permissionService;

  private Person person;
  private PwaApplication app;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() throws Exception {

    permissionService = new PwaApplicationPermissionService(pwaContactService, pwaHolderTeamService, teamService, applicationInvolvementService);

    app = new PwaApplication();
    detail = new PwaApplicationDetail();
    detail.setPwaApplication(app);
    person = PersonTestUtil.createDefaultPerson();

  }

  @Test
  public void getPermissions_permissionAllowsContacts_userHasAContactRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getContactRoles().isEmpty())
        .forEach(permission -> {

          var contactRole = permission.getContactRoles().iterator().next();

          when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of(contactRole));
          when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(Set.of());

          var permissions = permissionService.getPermissions(detail, person);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  public void getPermissions_permissionAllowsOrgRoles_userHasOrgRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getHolderTeamRoles().isEmpty())
        .forEach(permission -> {

          var holderRole = permission.getHolderTeamRoles().iterator().next();

          when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of());
          when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(Set.of(holderRole));

          var permissions = permissionService.getPermissions(detail, person);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  public void getPermissions_permissionAllowsRegRoles_userHasRegRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getRegulatorRoles().isEmpty())
        .forEach(permission -> {

          var regRole = permission.getRegulatorRoles().iterator().next();

          var teamMember = new PwaTeamMember(null, person, Set.of(new PwaRole(regRole.getPortalTeamRoleName(), null, null, 10)));
          when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.of(teamMember));

          var permissions = permissionService.getPermissions(detail, person);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  public void getPermissions_permissionAllowsConsulteeRoles_userHasConsulteeRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getConsulteeRoles().isEmpty())
        .forEach(permission -> {

          var consulteeRole = permission.getConsulteeRoles().iterator().next();

          when(applicationInvolvementService.getConsultationInvolvement(app, person))
              .thenReturn(Optional.of(new ConsultationInvolvementDto(null, Set.of(consulteeRole), null, null, false)));

          var permissions = permissionService.getPermissions(detail, person);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  public void getPermissions_allRoles_allStandardPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.allOf(PwaContactRole.class));
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(permissionService.getPermissions(detail, person))
        .containsExactlyInAnyOrderElementsOf(EnumSet.of(
            PwaApplicationPermission.SUBMIT,
            PwaApplicationPermission.EDIT,
            PwaApplicationPermission.VIEW,
            PwaApplicationPermission.MANAGE_CONTACTS));

  }

  @Test
  public void getPermissions_setPipelineReferencePermission_whenAppContact_andNotRegulator() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.allOf(PwaContactRole.class));

    assertThat(permissionService.getPermissions(detail, person))
        .doesNotContain(PwaApplicationPermission.SET_PIPELINE_REFERENCE);

  }

  @Test
  public void getPermissions_setPipelineReferencePermission_whenAppContactPreparer_andRegulator() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.of(PwaContactRole.PREPARER));
    var regTeam =TeamTestingUtils.getRegulatorTeam();
    var regTeamMember = TeamTestingUtils.createRegulatorTeamMember(regTeam, person, EnumSet.allOf(PwaRegulatorRole.class));
    when(teamService.getRegulatorTeam()).thenReturn(regTeam);
    when(teamService.getMembershipOfPersonInTeam(regTeam, person)).thenReturn(Optional.of(regTeamMember));

    assertThat(permissionService.getPermissions(detail, person))
        .contains(PwaApplicationPermission.SET_PIPELINE_REFERENCE);

  }

  @Test
  public void getPermissions_noRoles_noPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of());
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(Set.of());

    assertThat(permissionService.getPermissions(detail, person)).isEmpty();

  }

}