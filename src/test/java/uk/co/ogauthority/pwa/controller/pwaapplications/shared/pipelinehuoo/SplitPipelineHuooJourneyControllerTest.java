package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickSplitPipelineFormValidator;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SplitPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class SplitPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {

  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;

  private final int PIPELINE_ID = 1;
  private final String PIPELINE_NAME = "PIPELINE";

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PickSplitPipelineFormValidator pickSplitPipelineFormValidator;

  @Mock
  private PipelineOverview pipelineOverview;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private Pipeline pipeline;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  @Before
  public void setup() {
    pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);

    when(pipelineOverview.getPipelineId()).thenReturn(PIPELINE_ID);
    when(pipelineOverview.getPipelineName()).thenReturn(PIPELINE_NAME);

    when(padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(any()))
        .thenReturn(List.of(pipelineOverview));
    when(padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(any(), eq(pipeline.getPipelineId())))
        .thenReturn(pipelineOverview);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID);
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);
  }

  @Test
  public void renderSelectPipelineToSplit_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSelectPipelineToSplit_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderSelectPipelineToSplit_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSelectPipelineToSplit_modelCheck_andServiceInteractions() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .renderSelectPipelineToSplit(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView.getModel()).containsKey("pipelineOptions");
    assertThat(((Map<String, String>)modelAndView.getModel().get("pipelineOptions")))
        .containsExactly(entry(String.valueOf(PIPELINE_ID), PIPELINE_NAME));

    verify(padPipelinesHuooService, times(1)).getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

  }

  @Test
  public void splitSelectedPipeline_smokeCheckRolesAccess() {
    mockSelectPipelineForSplitFailedValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void splitSelectedPipeline_smokeCheckAppStatus() {
    mockSelectPipelineForSplitFailedValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void splitSelectedPipeline_smokeCheckAppType() {
    mockSelectPipelineForSplitFailedValidation();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void splitSelectedPipeline_whenValidationFails() throws Exception {
    mockSelectPipelineForSplitFailedValidation();

    mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .splitSelectedPipeline(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("pipelineId", String.valueOf(pipeline.getId()))
        .param("numberOfSections", "0")

    )
        .andExpect(status().isOk());

    verify(pickSplitPipelineFormValidator, times(1)).validate(any(), any(), any());
    verify(padPipelinesHuooService, times(0)).removeSplitsForPipeline(any(), any(), any());


  }

  @Test
  public void splitSelectedPipeline_whenValidationPasses_withSingleSectionDefined() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .splitSelectedPipeline(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("pipelineId", String.valueOf(pipeline.getId()))
        .param("numberOfSections", "1")
    )
        .andExpect(status().is3xxRedirection());

    verify(pickSplitPipelineFormValidator, times(1)).validate(any(), any(), any());
    verify(padPipelinesHuooService, times(1)).removeSplitsForPipeline(pwaApplicationDetail, pipeline.getPipelineId(), DEFAULT_ROLE);


  }


  private void mockSelectPipelineForSplitFailedValidation(){
    doAnswer(invocation -> {((BindingResult)invocation.getArgument(1))
        .rejectValue("pipelineId", "pipelineId.required", "pipelineIdRequired");
      return invocation;
    }).when(pickSplitPipelineFormValidator).validate(any(), any(), any());
  }

}