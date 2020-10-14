package uk.co.ogauthority.pwa.service.appprocessing.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingContextServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  @Mock
  private ApplicationDetailSearcher applicationDetailSearcher;

  @Mock
  private AppFileService appFileService;

  private PwaAppProcessingContextService contextService;

  private PwaApplicationDetail detail;
  private PwaApplication application;
  private AuthenticatedUserAccount user;

  private Instant startInstant;

  @Before
  public void setUp() {

    application = new PwaApplication();
    application.setId(1);
    application.setApplicationType(PwaApplicationType.INITIAL);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    detail = new PwaApplicationDetail(application, 1, 1, Instant.now());
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    contextService = new PwaAppProcessingContextService(detailService, appProcessingPermissionService, applicationDetailSearcher, appFileService);

    when(detailService.getLastSubmittedApplicationDetail(detail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(detail));
    when(appProcessingPermissionService.getProcessingPermissions(application, user)).thenReturn(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

    var searchItem = new ApplicationDetailSearchItem();
    startInstant = Instant.now();
    searchItem.setPadReference("PA/5/6");
    searchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    searchItem.setCaseOfficerPersonId(1);
    searchItem.setCaseOfficerName("Case Officer X");
    searchItem.setSubmittedAsFastTrackFlag(true);
    searchItem.setPadProposedStart(startInstant);
    searchItem.setPadFields(List.of("CAPTAIN", "PENGUIN"));
    searchItem.setPadHolderNameList(List.of("ROYAL DUTCH SHELL"));
    searchItem.setPwaHolderNameList(List.of("ROYAL DUTCH SHELL"));

    when(applicationDetailSearcher.searchByApplicationDetailId(any())).thenReturn(Optional.of(searchItem));

    var appFile = new AppFile();
    appFile.setPwaApplication(detail.getPwaApplication());
    when(appFileService.getAppFileByPwaApplicationAndFileId(detail.getPwaApplication(), "valid-file")).thenReturn(appFile);

  }

  @Test
  public void validateAndCreate_noChecks() {

    var contextBuilder = new PwaAppProcessingContextParams(1, user);
    var processingContext = contextService.validateAndCreate(contextBuilder);

    assertThat(processingContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(processingContext.getUser()).isEqualTo(user);
    assertThat(processingContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_noChecks_userHasNoProcessingPermissions() {
    when(appProcessingPermissionService.getProcessingPermissions(application, user)).thenReturn(Set.of());
    var contextBuilder = new PwaAppProcessingContextParams(1, user);
    contextService.validateAndCreate(contextBuilder);
  }

  @Test
  public void validateAndCreate_statusCheck_valid() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_statusCheck_invalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT);
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_permissionsCheck_valid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);
  }

  @Test
  public void validateAndCreate_permissionsCheck_atLeastOnePermission_valid() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredProcessingPermissions(Set.of(
            PwaAppProcessingPermission.CASE_MANAGEMENT_OGA,
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE,
            PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY));

    when(appProcessingPermissionService.getProcessingPermissions(application, user)).thenReturn(Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA));
    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_permissionsCheck_invalid() {
    when(appProcessingPermissionService.getProcessingPermissions(application, user)).thenReturn(Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW));
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_allChecks_valid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_allChecks_statusInvalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.DRAFT)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_permissionsInvalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_caseSummaryExists() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

    var processingContext = contextService.validateAndCreate(builder);

    assertThat(processingContext.getCaseSummaryView()).isNotNull();

    assertThat(processingContext.getCaseSummaryView().getPwaApplicationRef()).isEqualTo("PA/5/6");
    assertThat(processingContext.getCaseSummaryView().getPwaApplicationTypeDisplay()).isEqualTo(PwaApplicationType.CAT_1_VARIATION.getDisplayName());
    assertThat(processingContext.getCaseSummaryView().getHolderNames()).isEqualTo("ROYAL DUTCH SHELL");
    assertThat(processingContext.getCaseSummaryView().getFieldNames()).isEqualTo("CAPTAIN, PENGUIN");
    assertThat(processingContext.getCaseSummaryView().getCaseOfficerName()).isEqualTo("Case Officer X");
    assertThat(processingContext.getCaseSummaryView().getProposedStartDateDisplay()).isEqualTo(DateUtils.formatDate(startInstant));
    assertThat(processingContext.getCaseSummaryView().isFastTrackFlag()).isTrue();

  }

  @Test
  public void validateAndCreate_caseSummaryNotFound() {

    when(applicationDetailSearcher.searchByApplicationDetailId(any())).thenReturn(Optional.empty());

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

    var processingContext = contextService.validateAndCreate(builder);

    assertThat(processingContext.getCaseSummaryView()).isNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getProcessingContext_noLastSubmittedDetail(){
    when(detailService.getLastSubmittedApplicationDetail(detail.getMasterPwaApplicationId()))
        .thenReturn(Optional.empty());
    contextService.getProcessingContext(1, user);

  }

  @Test
  public void getProcessingContext_happyPath(){

    assertThat(contextService.getProcessingContext(1, user)).isNotNull();

  }

  @Test
  public void validateAndCreate_withFileId_valid() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .withFileId("valid-file");

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getAppFile()).isNotNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_withFileId_fileNotFound() {

    when(appFileService.getAppFileByPwaApplicationAndFileId(detail.getPwaApplication(), "bad-file")).thenThrow(PwaEntityNotFoundException.class);

    var builder = new PwaAppProcessingContextParams(1, user)
        .withFileId("bad-file");

    contextService.validateAndCreate(builder);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_withFileId_appDetailMismatch() {

    var otherAppFile = new AppFile();
    otherAppFile.setPwaApplication(new PwaApplication());

    when(appFileService.getAppFileByPwaApplicationAndFileId(detail.getPwaApplication(), "other-file")).thenReturn(otherAppFile);

    var builder = new PwaAppProcessingContextParams(1, user)
        .withFileId("other-file");

    contextService.validateAndCreate(builder);

  }

}
