package uk.co.ogauthority.pwa.service.pwaapplications.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationContextServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PwaContactService contactService;

  private PwaApplicationContextService contextService;

  private PwaApplicationDetail detail;
  private PwaApplication application;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    application = new PwaApplication();
    application.setId(1);
    application.setApplicationType(PwaApplicationType.INITIAL);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    detail = new PwaApplicationDetail(application, 1, 1, Instant.now());
    detail.setStatus(PwaApplicationStatus.DRAFT);

    contextService = new PwaApplicationContextService(detailService, contactService);

    when(detailService.getTipDetail(1)).thenReturn(detail);
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.PREPARER));

  }

  @Test
  public void createAndPerformApplicationContextChecks_noChecks() {

    var appContext = contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), null, Set.of());

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test(expected = AccessDeniedException.class)
  public void createAndPerformApplicationContextChecks_noChecks_userHasNoRolesForApp() {
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), null, Set.of());
  }

  @Test
  public void createAndPerformApplicationContextChecks_statusCheck_valid() {

    var appContext = contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), PwaApplicationStatus.DRAFT, Set.of());

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void createAndPerformApplicationContextChecks_statusCheck_invalid() {
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), PwaApplicationStatus.SUBMITTED, Set.of());
  }

  @Test
  public void createAndPerformApplicationContextChecks_permissionsCheck_valid() {
    var appContext = contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.EDIT), null, Set.of());
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);
  }

  @Test(expected = AccessDeniedException.class)
  public void createAndPerformApplicationContextChecks_permissionsCheck_invalid() {
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.VIEWER));
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.EDIT), null, Set.of());
  }

  @Test
  public void createAndPerformApplicationContextChecks_appTypesCheck_valid() {

    var allowedTypes = Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION);

    allowedTypes.forEach(type -> {

      detail.getPwaApplication().setApplicationType(type);

      var appContext = contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), null, allowedTypes);

      assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
      assertThat(appContext.getUser()).isEqualTo(user);
      assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

    });

  }

  @Test(expected = AccessDeniedException.class)
  public void createAndPerformApplicationContextChecks_appTypesCheck_invalid() {

    var invalidType = PwaApplicationType.HUOO_VARIATION;

    detail.getPwaApplication().setApplicationType(invalidType);

    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(), null, Set.of(PwaApplicationType.INITIAL));

  }

  @Test
  public void createAndPerformApplicationContextChecks_allChecks_valid() {
    var appContext = contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.EDIT), PwaApplicationStatus.DRAFT, Set.of(PwaApplicationType.INITIAL));
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void createAndPerformApplicationContextChecks_allChecks_statusInvalid() {
    //TODO PWA-66
   // when(detailService.getTipDetail(1)).thenThrow(PwaEntityNotFoundException.class);
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.EDIT), PwaApplicationStatus.SUBMITTED, Set.of(PwaApplicationType.INITIAL));
  }

  @Test(expected = AccessDeniedException.class)
  public void createAndPerformApplicationContextChecks_allChecks_typeInvalid() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.EDIT), PwaApplicationStatus.DRAFT, Set.of(PwaApplicationType.INITIAL));
  }

  @Test(expected = AccessDeniedException.class)
  public void createAndPerformApplicationContextChecks_allChecks_permissionInvalid() {
    contextService.createAndPerformApplicationContextChecks(1, user, Set.of(PwaApplicationPermission.MANAGE_CONTACTS), PwaApplicationStatus.DRAFT, Set.of(PwaApplicationType.INITIAL));
  }

}
