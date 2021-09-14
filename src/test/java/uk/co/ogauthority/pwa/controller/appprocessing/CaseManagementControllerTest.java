package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTabService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskRequirement;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEvent;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CaseManagementController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class CaseManagementControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  @MockBean
  private AppProcessingTabService appProcessingTabService;
  
  @MockBean
  private PwaApplicationEventService pwaApplicationEventService;

  @MockBean
  private CaseManagementController caseManagementController;

  private PwaApplicationEvent consentIssueFailedEvent;

  @Before
  public void setUp() {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(
            PwaAppProcessingPermission.CASE_MANAGEMENT_OGA,
            PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);

    var app = new PwaApplication();
    var user = new WebUserAccount();
    consentIssueFailedEvent = new PwaApplicationEvent(app, PwaApplicationEventType.CONSENT_ISSUE_FAILED, Instant.now(), user);

  }
  
  @Test
  public void renderCaseManagement_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CaseManagementController.class)
                .renderCaseManagement(applicationDetail.getMasterPwaApplicationId(), type, null,null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderCaseManagement_permissionSmokeTest() {

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));

    var map = Map.of(
        "taskListGroups", List.of(),
        "industryFlag", false,
        "taskListUrl", "#"
    );

    doReturn(map).when(appProcessingTabService).getTabContentModelMap(any(), any());

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CaseManagementController.class)
                .renderCaseManagement(applicationDetail.getMasterPwaApplicationId(), type, AppProcessingTab.TASKS,null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderCaseManagement_taskGroupNameWarningMessageMap_hasWarningMsgForCaseOfficer() {

    var caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(10), EnumSet.of(PwaUserPrivilege.PWA_CASE_OFFICER));
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        detail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED));

    var context = new PwaAppProcessingContext(
        detail,
        caseOfficer,
        Set.of(),
        null,
        appInvolvement, Set.of());

    caseManagementController = new CaseManagementController(appProcessingTabService, confirmSatisfactoryApplicationService, pwaApplicationEventService);

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));
    when(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(context.getApplicationDetail())).thenReturn(true);

    var modelAndView =  caseManagementController.renderCaseManagement(1, null, AppProcessingTab.TASKS, context, caseOfficer);

    var warningMap = (Map<String, String>) modelAndView.getModel().get("taskGroupNameWarningMessageMap");
    assertThat(warningMap.get(TaskRequirement.REQUIRED.getDisplayName())).isEqualTo(
        "This updated application should be confirmed as satisfactory before performing other tasks.");

  }

  @Test
  public void renderCaseManagement_taskGroupNameWarningMessageMap_userIsNotAssignedCaseOfficer_noWarningMessage() {

    var user = new AuthenticatedUserAccount(new WebUserAccount(10), EnumSet.of(PwaUserPrivilege.PWA_INDUSTRY));
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        detail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));

    var context = new PwaAppProcessingContext(
        detail,
        user,
        Set.of(),
        null,
        appInvolvement, Set.of());

    caseManagementController = new CaseManagementController(appProcessingTabService, confirmSatisfactoryApplicationService, pwaApplicationEventService);

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));
    when(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(context.getApplicationDetail())).thenReturn(true);

    var modelAndView =  caseManagementController.renderCaseManagement(1, null, AppProcessingTab.TASKS, context, user);

    var warningMap = (Map<String, String>) modelAndView.getModel().get("taskGroupNameWarningMessageMap");
    assertThat(warningMap).isEmpty();
  }

  @Test
  public void renderCaseManagement_taskGroupNameWarningMessageMap_consentIssueFailed_isOga_hasWarningMessage() {

    var caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(10), EnumSet.of(PwaUserPrivilege.PWA_CASE_OFFICER));
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        detail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED));

    var context = new PwaAppProcessingContext(
        detail,
        caseOfficer,
        Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA),
        null,
        appInvolvement, Set.of());

    caseManagementController = new CaseManagementController(appProcessingTabService, confirmSatisfactoryApplicationService, pwaApplicationEventService);

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));
    when(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(context.getApplicationDetail())).thenReturn(false);

    when(pwaApplicationEventService.getUnclearedEventsByApplicationAndType(any(), eq(PwaApplicationEventType.CONSENT_ISSUE_FAILED)))
        .thenReturn(List.of(consentIssueFailedEvent));

    var modelAndView =  caseManagementController.renderCaseManagement(1, null, AppProcessingTab.TASKS, context, caseOfficer);

    var warningMap = (Map<String, String>) modelAndView.getModel().get("taskGroupNameWarningMessageMap");
    assertThat(warningMap.get(TaskRequirement.REQUIRED.getDisplayName())).isEqualTo(
        String.format(
            "The consent issue process failed on %s. Contact Support before attempting to issue the consent again.",
            DateUtils.formatDateTime(consentIssueFailedEvent.getEventInstant()))
    );

  }

  @Test
  public void renderCaseManagement_taskGroupNameWarningMessageMap_consentIssueFailed_userNotOga_noWarningMessage() {

    var user = new AuthenticatedUserAccount(new WebUserAccount(10), EnumSet.of(PwaUserPrivilege.PWA_INDUSTRY));
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        detail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));

    var context = new PwaAppProcessingContext(
        detail,
        user,
        Set.of(),
        null,
        appInvolvement, Set.of());

    caseManagementController = new CaseManagementController(appProcessingTabService, confirmSatisfactoryApplicationService, pwaApplicationEventService);

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));
    when(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(context.getApplicationDetail())).thenReturn(false);

    when(pwaApplicationEventService.getUnclearedEventsByApplicationAndType(any(), eq(PwaApplicationEventType.CONSENT_ISSUE_FAILED)))
        .thenReturn(List.of(consentIssueFailedEvent));

    var modelAndView =  caseManagementController.renderCaseManagement(1, null, AppProcessingTab.TASKS, context, user);

    var warningMap = (Map<String, String>) modelAndView.getModel().get("taskGroupNameWarningMessageMap");

    assertThat(warningMap).isEmpty();

  }

}
