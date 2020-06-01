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
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(
    controllers = CampaignWorksController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class CampaignWorksControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;

  // Dont understand why this needs to be spybean and not a mock bean
  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private CampaignWorksService campaignWorksService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  @Before
  public void setup() {
    doCallRealMethod().when(applicationBreadcrumbService).fromCampaignWorksOverview(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION)
        .setAllowedRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);


    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));
  }

  @Test
  public void renderSummary_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderSummary(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderSummary(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderSummary(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

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
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderAddWorkSchedule(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderAddWorkSchedule(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .renderAddWorkSchedule(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddWorkSchedule_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CampaignWorksController.class).renderAddWorkSchedule(pwaApplicationDetail.getPwaApplicationType(),
                APP_ID, null, null)))
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().attribute("screenActionType", ScreenActionType.ADD));

    verify(padPipelineService, times(1)).getPipelineOverviews(pwaApplicationDetail);
  }

  @Test
  public void addWorkSchedule_appTypeSmokeTest() {

    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)

        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CampaignWorksController.class)
                .addWorkSchedule(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .addWorkSchedule(
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
            ReverseRouter.route(on(CampaignWorksController.class)
                .addWorkSchedule(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddWorkSchedule_serviceInteractions_whenValidationFail() throws Exception {
    ControllerTestUtils.failValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).addWorkSchedule(pwaApplicationDetail.getPwaApplicationType(),
                APP_ID, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().isOk());

    verify(campaignWorksService, times(0)).addCampaignWorkScheduleFromForm(any(), eq(pwaApplicationDetail));
  }

  @Test
  public void renderAddWorkSchedule_serviceInteractions_whenValidationPass() throws Exception {
    ControllerTestUtils.passValidationWhenPost(campaignWorksService, new WorkScheduleForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CampaignWorksController.class).addWorkSchedule(pwaApplicationDetail.getPwaApplicationType(),
                APP_ID, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(getWorkScheduleFormAsMap())
    )
        .andExpect(status().is3xxRedirection());

    verify(campaignWorksService, times(1)).addCampaignWorkScheduleFromForm(any(), eq(pwaApplicationDetail));
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
