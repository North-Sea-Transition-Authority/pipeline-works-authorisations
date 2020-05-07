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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinesService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationContextServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PwaContactService contactService;

  @Mock
  private PadPipelinesService padPipelinesService;

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

    contextService = new PwaApplicationContextService(detailService, contactService, padPipelinesService);

    when(detailService.getTipDetail(1)).thenReturn(detail);
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.PREPARER));

    var padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(detail);
    when(padPipelinesService.getById(2)).thenReturn(padPipeline);

  }

  @Test
  public void validateAndCreate_noChecks() {

    var contextBuilder = new PwaApplicationContextParams(1, user);
    var appContext = contextService.validateAndCreate(contextBuilder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_noChecks_userHasNoRolesForApp() {
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());
    var contextBuilder = new PwaApplicationContextParams(1, user);
    contextService.validateAndCreate(contextBuilder);
  }

  @Test
  public void validateAndCreate_statusCheck_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT);

    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_statusCheck_invalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_permissionsCheck_valid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_permissionsCheck_invalid() {
    when(contactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.VIEWER));
    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_appTypesCheck_valid() {

    var allowedTypes = Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION);

    allowedTypes.forEach(type -> {

      detail.getPwaApplication().setApplicationType(type);

      var builder = new PwaApplicationContextParams(1, user)
          .requiredAppTypes(allowedTypes);

      var appContext = contextService.validateAndCreate(builder);

      assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
      assertThat(appContext.getUser()).isEqualTo(user);
      assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);

    });

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_appTypesCheck_invalid() {

    var invalidType = PwaApplicationType.HUOO_VARIATION;

    detail.getPwaApplication().setApplicationType(invalidType);

    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL));

    contextService.validateAndCreate(builder);

  }

  @Test
  public void validateAndCreate_allChecks_valid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT)
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getUserRoles()).containsExactly(PwaContactRole.PREPARER);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_allChecks_statusInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_typeInvalid() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT)
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_permissionInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT)
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.MANAGE_CONTACTS));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_withPadPipeline_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(2);

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getPadPipeline()).isNotNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_withPadPipeline_pipeNotFound() {

    when(padPipelinesService.getById(3)).thenThrow(PwaEntityNotFoundException.class);

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(3);

    contextService.validateAndCreate(builder);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_withPadPipeline_pipeAppMismatch() {

    var otherAppPipe = new PadPipeline();
    otherAppPipe.setPwaApplicationDetail(new PwaApplicationDetail());

    when(padPipelinesService.getById(4)).thenReturn(otherAppPipe);

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(4);

    contextService.validateAndCreate(builder);

  }

}
