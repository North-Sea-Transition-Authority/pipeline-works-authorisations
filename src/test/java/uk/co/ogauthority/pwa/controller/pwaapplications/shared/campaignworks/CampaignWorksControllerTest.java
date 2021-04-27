package uk.co.ogauthority.pwa.controller.pwaapplications.shared.campaignworks;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(
    controllers = CampaignWorksController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class CampaignWorksControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;
  private static final int SCHEDULE_ID = 101;

  // Dont understand why this needs to be spybean and not a mock bean
  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private CampaignWorksService campaignWorksService;

  @Mock
  private WorkScheduleView workScheduleViewMock;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PadCampaignWorkSchedule schedule;

  @Mock
  private SummaryScreenValidationResult campaignWorksSummaryValidationResult;

  @Before
  public void setup() {
    doCallRealMethod().when(applicationBreadcrumbService).fromCampaignWorksOverview(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);


    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    schedule = new PadCampaignWorkSchedule();
    when(workScheduleViewMock.getFormattedWorkEndDate()).thenReturn("end date");
    when(workScheduleViewMock.getFormattedWorkStartDate()).thenReturn("start date");

    when(campaignWorksService.getWorkScheduleOrError(any(), eq(SCHEDULE_ID))).thenReturn(schedule);
    when(campaignWorksService.createWorkScheduleView(schedule)).thenReturn(workScheduleViewMock);

    when(campaignWorksService.getCampaignWorksValidationResult(any()))
        .thenReturn(campaignWorksSummaryValidationResult);
  }

  @Test
  public void renderSummary_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderSummary(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSummary_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderSummary(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderSummary_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderSummary(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSummary_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CampaignWorksController.class).renderSummary(pwaApplicationDetail.getPwaApplicationType(), APP_ID,
                null)))
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk());

    verify(campaignWorksService, times(1)).getWorkScheduleViews(pwaApplicationDetail);
  }

  @Test
  public void renderAddWorkSchedule_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderAddWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddWorkSchedule_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderAddWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderAddWorkSchedule_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderAddWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddWorkSchedule_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CampaignWorksController.class).renderAddWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID,
                null,
                null)))
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().attribute("screenActionType", ScreenActionType.ADD));

    verify(padPipelineService, times(1)).getApplicationPipelineOverviews(pwaApplicationDetail);
  }

  @Test
  public void addWorkSchedule_appTypeSmokeTest() {

    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)

        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).addWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null,
                null)
            )
        );


    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void addWorkSchedule_appStatusSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).addWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void addWorkSchedule_contactRoleSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).addWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }


  @Test
  public void addWorkSchedule_serviceInteractions_whenValidationFail() throws Exception {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).addWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID,
                null,
                null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().isOk());

    verify(campaignWorksService, times(0)).addCampaignWorkScheduleFromForm(any(), eq(pwaApplicationDetail));
  }

  @Test
  public void addWorkSchedule_serviceInteractions_whenValidationPass() throws Exception {
    ControllerTestUtils.passValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class)
                .addWorkSchedule(
                    pwaApplicationDetail.getPwaApplicationType(),
                    APP_ID,
                    null,
                    null,
                    null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().is3xxRedirection());

    verify(campaignWorksService, times(1)).addCampaignWorkScheduleFromForm(any(), eq(pwaApplicationDetail));
  }

  @Test
  public void renderEditWorkSchedule_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderEditWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditWorkSchedule_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderEditWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderEditWorkSchedule_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderEditWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditWorkSchedule_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CampaignWorksController.class).renderEditWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID, SCHEDULE_ID,
                null,
                null)))
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().attribute("screenActionType", ScreenActionType.EDIT));

    verify(padPipelineService, times(1)).getApplicationPipelineOverviews(pwaApplicationDetail);
  }


  @Test
  public void editWorkSchedule_appTypeSmokeTest() {

    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)

        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).editWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null,
                null)
            )
        );


    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void editWorkSchedule_appStatusSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).editWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void editWorkSchedule_contactRoleSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).editWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null,
                null,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void editWorkSchedule_serviceInteractions_whenValidationFail() throws Exception {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).editWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID,
                SCHEDULE_ID,
                null,
                null,
                null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().isOk());

    verify(campaignWorksService, times(0)).updateCampaignWorksScheduleFromForm(any(), eq(schedule));
  }

  @Test
  public void editWorkSchedule_serviceInteractions_whenValidationPass() throws Exception {
    ControllerTestUtils.passValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).editWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID,
                SCHEDULE_ID,
                null,
                null,
                null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().is3xxRedirection());

    verify(campaignWorksService, times(1)).updateCampaignWorksScheduleFromForm(any(), eq(schedule));
  }


  @Test
  public void renderRemoveWorkSchedule_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderRemoveWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveWorkSchedule_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderRemoveWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRemoveWorkSchedule_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).renderRemoveWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveWorkSchedule_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CampaignWorksController.class).renderRemoveWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID, SCHEDULE_ID,
                null)))
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk());

    verify(campaignWorksService, times(1)).createWorkScheduleView(schedule);
  }


  @Test
  public void removeWorkSchedule_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)

        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).removeWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );


    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void removeWorkSchedule_appStatusSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).removeWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void removeWorkSchedule_contactRoleSmokeTest() {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class).removeWorkSchedule(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                SCHEDULE_ID,
                null)
            )
        );

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }


  @Test
  public void removeWorkSchedule_serviceInteractions() throws Exception {
    ControllerTestUtils.passValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).removeWorkSchedule(
                pwaApplicationDetail.getPwaApplicationType(),
                APP_ID,
                SCHEDULE_ID,
                null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().is3xxRedirection());

    verify(campaignWorksService, times(1)).removeCampaignWorksSchedule(eq(schedule));
  }


  private MultiValueMap<String, String> getWorkScheduleFormAsMap() {


    return new LinkedMultiValueMap<>() {{
      add("workStart.month", "99");
      add("workStart.year", "99");
      add("workEnd.month", "99");
      add("workEnd.year", "99");
      add("padPipelineIds", "");
    }};
  }
}
