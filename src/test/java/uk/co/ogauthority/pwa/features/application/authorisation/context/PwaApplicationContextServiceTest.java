package uk.co.ogauthority.pwa.features.application.authorisation.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationContextServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PwaApplicationPermissionService pwaApplicationPermissionService;

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private Timer timer;

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

    contextService = new PwaApplicationContextService(detailService, padPipelineService, padFileService, pwaApplicationPermissionService,
        metricsProvider);

    when(detailService.getTipDetail(1)).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of(PwaApplicationPermission.EDIT));

    var padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(detail);
    when(padPipelineService.getById(2)).thenReturn(padPipeline);

    var padFile = new PadFile();
    padFile.setPwaApplicationDetail(detail);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(detail, "valid-file")).thenReturn(padFile);

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaApplicationContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(timer);

  }

  @Test
  public void validateAndCreate_noChecks() {

    var contextBuilder = new PwaApplicationContextParams(1, user);
    var appContext = contextService.validateAndCreate(contextBuilder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);

  }

  @Test
  public void validateAndCreate_noChecks_userHasNoRolesForApp() {
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of());
    var contextBuilder = new PwaApplicationContextParams(1, user);
    contextService.validateAndCreate(contextBuilder);
  }

  @Test
  public void validateAndCreate_statusCheck_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT));

    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_statusCheck_invalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_permissionsCheck_valid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_permissionsCheck_invalid() {
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of(PwaApplicationPermission.VIEW));
    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_permissionsCheck_invalid_noPermissions() {

    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.SUBMIT));

    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of());

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
      assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);

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
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_allChecks_statusInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_typeInvalid() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_permissionInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
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

    when(padPipelineService.getById(3)).thenThrow(PwaEntityNotFoundException.class);

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(3);

    contextService.validateAndCreate(builder);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_withPadPipeline_pipeAppMismatch() {

    var otherAppPipe = new PadPipeline();
    otherAppPipe.setPwaApplicationDetail(new PwaApplicationDetail());

    when(padPipelineService.getById(4)).thenReturn(otherAppPipe);

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(4);

    contextService.validateAndCreate(builder);

  }

  @Test
  public void validateAndCreate_withFileId_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .withFileId("valid-file");

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getPadFile()).isNotNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_withFileId_fileNotFound() {

    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(detail, "bad-file")).thenThrow(PwaEntityNotFoundException.class);

    var builder = new PwaApplicationContextParams(1, user)
        .withFileId("bad-file");

    contextService.validateAndCreate(builder);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_withFileId_appDetailMismatch() {

    var otherAppFile = new PadFile();
    otherAppFile.setPwaApplicationDetail(new PwaApplicationDetail());

    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(detail, "other-file")).thenReturn(otherAppFile);

    var builder = new PwaApplicationContextParams(1, user)
        .withFileId("other-file");

    contextService.validateAndCreate(builder);

  }

  @Test
  public void validateAndCreate_timerMetricStarted_timeRecordedAndLogged() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));

    contextService.validateAndCreate(builder);
    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "Application Context");
  }

}
