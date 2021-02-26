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
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingContextServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  @Mock
  private CaseSummaryViewService caseSummaryViewService;

  @Mock
  private AppFileService appFileService;

  private PwaAppProcessingContextService contextService;

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
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    contextService = new PwaAppProcessingContextService(
        detailService,
        appProcessingPermissionService,
        caseSummaryViewService,
        appFileService);

    when(detailService.getLatestDetailForUser(detail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(detail));

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    when(appProcessingPermissionService.getProcessingPermissionsDto(detail, user)).thenReturn(permissionsDto);

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
    when(appProcessingPermissionService.getProcessingPermissionsDto(detail, user)).thenReturn(
        PwaAppProcessingContextDtoTestUtils.emptyPermissionsDto());
    var contextBuilder = new PwaAppProcessingContextParams(1, user);
    contextService.validateAndCreate(contextBuilder);
  }

  @Test
  public void validateAndCreate_statusCheck_valid() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));

    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_statusCheck_invalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT));
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

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA));
    when(appProcessingPermissionService.getProcessingPermissionsDto(detail, user)).thenReturn(permissionsDto);
    var appContext = contextService.validateAndCreate(builder);

    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);

  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_permissionsCheck_invalid() {
    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW));
    when(appProcessingPermissionService.getProcessingPermissionsDto(detail, user)).thenReturn(permissionsDto);
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_allChecks_valid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    var appContext = contextService.validateAndCreate(builder);
    assertThat(appContext.getApplicationDetail()).isEqualTo(detail);
    assertThat(appContext.getUser()).isEqualTo(user);
    assertThat(appContext.getAppProcessingPermissions()).containsExactly(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validateAndCreate_allChecks_statusInvalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.DRAFT))
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_allChecks_permissionsInvalid() {
    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW));
    contextService.validateAndCreate(builder);
  }

  @Test
  public void validateAndCreate_caseSummaryCreated() {

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

    var caseSummaryView = CaseSummaryView.from(ApplicationDetailViewTestUtil.createGenericDetailView());
    when(caseSummaryViewService.getCaseSummaryViewForAppDetail(detail)).thenReturn(Optional.of(caseSummaryView));

    var processingContext = contextService.validateAndCreate(builder);

    assertThat(processingContext.getCaseSummaryView()).isEqualTo(caseSummaryView);

  }

  @Test
  public void validateAndCreate_caseSummaryNotFound() {

    when(caseSummaryViewService.getCaseSummaryViewForAppDetail(any())).thenReturn(Optional.empty());

    var builder = new PwaAppProcessingContextParams(1, user)
        .requiredAppStatuses(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))
        .requiredProcessingPermissions(Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

    var processingContext = contextService.validateAndCreate(builder);

    assertThat(processingContext.getCaseSummaryView()).isNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getProcessingContext_noLastSubmittedDetail(){
    when(detailService.getLatestDetailForUser(detail.getMasterPwaApplicationId(), user))
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

  @Test
  public void validateAndCreate_activeConsultationRequest_consultee_present() {

    var builder = new PwaAppProcessingContextParams(1, user);

    var request = new ConsultationRequest();

    var consultationInvolvement = new ConsultationInvolvementDto(
        ConsulteeGroupTestingUtils.createConsulteeGroup("n", "nn"),
        Set.of(),
        request,
        List.of(),
        false
    );

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(application, consultationInvolvement);
    var permissionsDto = new ProcessingPermissionsDto(appInvolvement, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));
    when(appProcessingPermissionService.getProcessingPermissionsDto(any(), any()))
        .thenReturn(permissionsDto);

    var context = contextService.validateAndCreate(builder);

    var requestOpt = context.getActiveConsultationRequest();
    assertThat(requestOpt).isPresent();

    assertThat(requestOpt.get())
        .extracting(ConsultationRequestDto::getConsulteeGroupName, ConsultationRequestDto::getConsultationRequest)
        .contains(consultationInvolvement.getConsulteeGroupDetail().getName(), request);

  }

  @Test
  public void validateAndCreate_noActiveConsultationRequest_consultee_notPresent() {

    var builder = new PwaAppProcessingContextParams(1, user);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(detail.getPwaApplication());
    var permissionsDto = new ProcessingPermissionsDto(appInvolvement, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));
    when(appProcessingPermissionService.getProcessingPermissionsDto(any(), any()))
        .thenReturn(permissionsDto);

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getActiveConsultationRequest()).isEmpty();

  }

  @Test
  public void validateAndCreate_activeConsultationRequest_notConsultee_notPresent() {

    var builder = new PwaAppProcessingContextParams(1, user);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    var permissionsDto = new ProcessingPermissionsDto(appInvolvement, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY));
    when(appProcessingPermissionService.getProcessingPermissionsDto(any(), any()))
        .thenReturn(permissionsDto);

    var context = contextService.validateAndCreate(builder);

    assertThat(context.getActiveConsultationRequest()).isEmpty();

  }

}
