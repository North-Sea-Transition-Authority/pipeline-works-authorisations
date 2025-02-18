package uk.co.ogauthority.pwa.features.application.authorisation.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaApplicationContextServiceTest {

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

  @BeforeEach
  void setUp() {

    application = new PwaApplication();
    application.setId(1);
    application.setApplicationType(PwaApplicationType.INITIAL);
    application.setResourceType(PwaResourceType.PETROLEUM);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    detail = new PwaApplicationDetail(application, 1, 1, Instant.now());
    detail.setStatus(PwaApplicationStatus.DRAFT);

    contextService = new PwaApplicationContextService(detailService, padPipelineService, padFileService, pwaApplicationPermissionService,
        metricsProvider);

    when(detailService.getTipDetailByAppId(1)).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of(PwaApplicationPermission.EDIT));

    var padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(detail);
    when(padPipelineService.getById(2)).thenReturn(padPipeline);

    var padFile = new PadFile();
    padFile.setPwaApplicationDetail(detail);
    when(padFileService.getAllByFileId("valid-file")).thenReturn(List.of(padFile));

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaApplicationContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(timer);

  }

  @Test
  void validateAndCreate_noChecks() {

    var contextBuilder = new PwaApplicationContextParams(1, user);
    var appContext = contextService.validateAndCreate(contextBuilder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);

  }

  @Test
  void validateAndCreate_noChecks_userHasNoRolesForApp() {
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of());
    var contextBuilder = new PwaApplicationContextParams(1, user);
    contextService.validateAndCreate(contextBuilder);
  }

  @Test
  void validateAndCreate_statusCheck_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT));

    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);

  }

  @Test
  void validateAndCreate_statusCheck_invalid() {
    var builder = new PwaApplicationContextParams(1, user)
          .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));
    assertThrows(PwaEntityNotFoundException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_permissionsCheck_valid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);
  }

  @Test
  void validateAndCreate_permissionsCheck_invalid() {
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of(PwaApplicationPermission.VIEW));
    var builder = new PwaApplicationContextParams(1, user)
          .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_permissionsCheck_invalid_noPermissions() {
    var builder = new PwaApplicationContextParams(1, user)
          .requiredUserPermissions(Set.of(PwaApplicationPermission.SUBMIT));
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of());
    assertThrows(AccessDeniedException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_appTypesCheck_valid() {

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

  @Test
  void validateAndCreate_appTypesCheck_invalid() {
    var invalidType = PwaApplicationType.HUOO_VARIATION;
    detail.getPwaApplication().setApplicationType(invalidType);
    var builder = new PwaApplicationContextParams(1, user)
          .requiredAppTypes(Set.of(PwaApplicationType.INITIAL));
    assertThrows(AccessDeniedException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_resourceTypesCheck_valid() {

    var allowedTypes = EnumSet.allOf(PwaResourceType.class);

    allowedTypes.forEach(type -> {

      detail.getPwaApplication().setResourceType(type);
      var builder = new PwaApplicationContextParams(1, user)
          .requiredResourceTypes(allowedTypes);

      var appContext = contextService.validateAndCreate(builder);

      assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
      assertThat(appContext.getUser()).isEqualTo(user);
      assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);
    });

  }

  @Test
  void validateAndCreate_resourceTypesCheck_invalid() {
    var invalidType = PwaResourceType.CCUS;
    detail.getPwaApplication().setResourceType(invalidType);
    var builder = new PwaApplicationContextParams(1, user)
          .requiredResourceTypes(EnumSet.complementOf(EnumSet.of(PwaResourceType.CCUS)));
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_allChecks_valid() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getPermissions()).containsExactly(PwaApplicationPermission.EDIT);
  }

  @Test
  void validateAndCreate_allChecks_statusInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
          .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
          .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
          .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    assertThrows(PwaEntityNotFoundException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_allChecks_typeInvalid() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    var builder = new PwaApplicationContextParams(1, user)
          .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
          .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
          .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_allChecks_permissionInvalid() {
    var builder = new PwaApplicationContextParams(1, user)
          .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
          .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
          .requiredUserPermissions(Set.of(PwaApplicationPermission.MANAGE_CONTACTS));
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(builder));
  }

  @Test
  void validateAndCreate_withPadPipeline_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .withPadPipelineId(2);

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getPadPipeline()).isNotNull();

  }

  @Test
  void validateAndCreate_withPadPipeline_pipeNotFound() {
    when(padPipelineService.getById(3)).thenThrow(PwaEntityNotFoundException.class);
    var builder = new PwaApplicationContextParams(1, user)
          .withPadPipelineId(3);
    assertThrows(PwaEntityNotFoundException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_withPadPipeline_pipeAppMismatch() {
    var otherAppPipe = new PadPipeline();
    otherAppPipe.setPwaApplicationDetail(new PwaApplicationDetail());
    when(padPipelineService.getById(4)).thenReturn(otherAppPipe);
    var builder = new PwaApplicationContextParams(1, user)
          .withPadPipelineId(4);
    assertThrows(AccessDeniedException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_withFileId_valid() {

    var builder = new PwaApplicationContextParams(1, user)
        .withFileId("valid-file");

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getPadFile()).isNotNull();

  }

  @Test
  void validateAndCreate_withFileId_fileNotFound() {
    var builder = new PwaApplicationContextParams(1, user)
          .withFileId("bad-file");
    assertThrows(PwaEntityNotFoundException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_withFileId_appDetailMismatch() {
    var otherAppFile = new PadFile();
    var applicationDetail = new PwaApplicationDetail();
    var application = new PwaApplication();
    application.setId(1000);
    applicationDetail.setPwaApplication(application);
    otherAppFile.setPwaApplicationDetail(applicationDetail);
    when(padFileService.getAllByFileId("other-file")).thenReturn(List.of(otherAppFile));
    var builder = new PwaApplicationContextParams(1, user)
          .withFileId("other-file");
    assertThrows(AccessDeniedException.class, () ->

      contextService.validateAndCreate(builder));

  }

  @Test
  void validateAndCreate_timerMetricStarted_timeRecordedAndLogged() {
    var builder = new PwaApplicationContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredAppTypes(Set.of(PwaApplicationType.INITIAL))
        .requiredUserPermissions(Set.of(PwaApplicationPermission.EDIT));

    contextService.validateAndCreate(builder);
    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "Application Context");
  }

}
