package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.teams.TeamType.REGULATOR;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;
import uk.co.ogauthority.pwa.testutils.AssertionTestUtils;

@ExtendWith(MockitoExtension.class)
class PwaApplicationPermissionServiceTest {

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private PwaApplicationPermissionService permissionService;

  private Person person;
  private PwaApplication app;
  private PwaApplicationDetail detail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    app = new PwaApplication();
    detail = new PwaApplicationDetail();
    detail.setPwaApplication(app);
    user = AuthenticatedUserAccountTestUtil.createAllPrivWebUserAccount(1, person);
    person = user.getLinkedPerson();

  }

  @Test
  void getPermissions_permissionAllowsContacts_userHasAContactRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getContactRoles().isEmpty())
        .forEach(permission -> {

          var contactRole = permission.getContactRoles().iterator().next();

          when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of(contactRole));
          when(pwaHolderTeamService.getRolesInHolderTeam(detail, user)).thenReturn(Set.of());

          var permissions = permissionService.getPermissions(detail, user);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  void getPermissions_permissionAllowsOrgRoles_userHasOrgRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getHolderTeamRoles().isEmpty())
        .forEach(permission -> {

          var holderRole = permission.getHolderTeamRoles().iterator().next();

          when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of());
          when(pwaHolderTeamService.getRolesInHolderTeam(detail, user)).thenReturn(Set.of(holderRole));

          var permissions = permissionService.getPermissions(detail, user);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  void getPermissions_permissionAllowsRegRoles_userHasRegRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getRegulatorRoles().isEmpty())
        .forEach(permission -> {

          var userTeamRolesView = new UserTeamRolesView(2L, null, null, List.of(Role.PWA_MANAGER));
          when(teamQueryService.getTeamRolesViewsByUserAndTeamType(user.getWuaId(), REGULATOR)).thenReturn(List.of(userTeamRolesView));

          var permissions = permissionService.getPermissions(detail, user);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  void getPermissions_permissionAllowsConsulteeRoles_userHasConsulteeRole_hasPermission() {

    PwaApplicationPermission.stream()
        .filter(p -> !p.getConsulteeRoles().isEmpty())
        .forEach(permission -> {

          var consulteeRole = permission.getConsulteeRoles().iterator().next();

          when(applicationInvolvementService.getConsultationInvolvement(app, user))
              .thenReturn(Optional.of(new ConsultationInvolvementDto(null, Set.of(consulteeRole), null, null, false)));

          var permissions = permissionService.getPermissions(detail, user);
          AssertionTestUtils.assertNotEmptyAndContains(permissions, permission);

        });

  }

  @Test
  void getPermissions_allRoles_allStandardPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.allOf(PwaContactRole.class));
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, user)).thenReturn(EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles()));

    assertThat(permissionService.getPermissions(detail, user))
        .containsExactlyInAnyOrderElementsOf(EnumSet.of(
            PwaApplicationPermission.SUBMIT,
            PwaApplicationPermission.EDIT,
            PwaApplicationPermission.VIEW,
            PwaApplicationPermission.MANAGE_CONTACTS));

  }

  @Test
  void getPermissions_setPipelineReferencePermission_whenAppContact_andNotRegulator() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.allOf(PwaContactRole.class));

    assertThat(permissionService.getPermissions(detail, user))
        .doesNotContain(PwaApplicationPermission.SET_PIPELINE_REFERENCE);

  }

  @Test
  void getPermissions_setPipelineReferencePermission_whenAppContactPreparer_andRegulator() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.of(PwaContactRole.PREPARER));
    var userTeamRolesView = new UserTeamRolesView(2L, null, null, List.of(Role.PWA_MANAGER));
    when(teamQueryService.getTeamRolesViewsByUserAndTeamType(user.getWuaId(), REGULATOR)).thenReturn(List.of(userTeamRolesView));

    assertThat(permissionService.getPermissions(detail, user))
        .contains(PwaApplicationPermission.SET_PIPELINE_REFERENCE);

  }

  @Test
  void getPermissions_noRoles_noPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of());
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, user)).thenReturn(Set.of());

    assertThat(permissionService.getPermissions(detail, user)).isEmpty();

  }

}