package uk.co.ogauthority.pwa.service.pwaapplications.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.AssertionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationPermissionServiceTest {

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  private PwaApplicationPermissionService permissionService;

  private Person person;
  private PwaApplication app;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() throws Exception {

    permissionService = new PwaApplicationPermissionService(pwaContactService, pwaHolderTeamService);

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
  public void getPermissions_allRoles_allPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(EnumSet.allOf(PwaContactRole.class));
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(permissionService.getPermissions(detail, person))
        .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(PwaApplicationPermission.class));

  }

  @Test
  public void getPermissions_noRoles_noPermissions() {

    when(pwaContactService.getContactRoles(app, person)).thenReturn(Set.of());
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, person)).thenReturn(Set.of());

    assertThat(permissionService.getPermissions(detail, person)).isEmpty();

  }

}