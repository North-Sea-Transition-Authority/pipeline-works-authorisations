package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.controller;


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
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.DefinePipelineHuooSectionsFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.PickSplitPipelineFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.PickableHuooPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.PickableIdentLocationOption;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@WebMvcTest(controllers = SplitPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class SplitPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {

  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;

  private final PipelineId PIPELINE_ID = new PipelineId(1);
  private final String PIPELINE_NAME = "PIPELINE";

  private final int NUMBER_OF_SECTIONS = 3;
  private final long NUMBER_OF_ASSIGNABLE_ROLES = 3;

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PickSplitPipelineFormValidator pickSplitPipelineFormValidator;

  @MockBean
  private PickableHuooPipelineIdentService pickableHuooPipelineIdentService;

  @MockBean
  private DefinePipelineHuooSectionsFormValidator definePipelineHuooSectionsFormValidator;

  @Mock
  private PipelineOverview pipelineOverview;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private Pipeline pipeline;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  private PickableIdentLocationOption identOption1;
  private PickableIdentLocationOption identOption2;

  @BeforeEach
  void setup() {
    pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID.asInt());

    when(pipelineOverview.getPipelineId()).thenReturn(PIPELINE_ID.asInt());
    when(pipelineOverview.getPipelineName()).thenReturn(PIPELINE_NAME);

    when(padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(any()))
        .thenReturn(List.of(pipelineOverview));
    when(padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(any(), eq(pipeline.getPipelineId())))
        .thenReturn(pipelineOverview);

    doAnswer(invocation -> invocation.getArgument(1)).when(definePipelineHuooSectionsFormValidator).validate(any(), any(), any(Object[].class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    identOption1 = new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "FROM");
    identOption2 = new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.TO_LOCATION, "TO");
    when(pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(any(), eq(PIPELINE_ID)))
        .thenReturn(List.of(identOption1, identOption2));

    when(padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
        any(),
        eq(PIPELINE_ID)
    )).thenReturn(pipelineOverview);

    when(padPipelinesHuooService.countDistinctRoleOwnersForRole(any(), any())).thenReturn(NUMBER_OF_ASSIGNABLE_ROLES);
  }

  @Test
  void renderSelectPipelineToSplit_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderSelectPipelineToSplit_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderSelectPipelineToSplit_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderSelectPipelineToSplit_modelCheck_andServiceInteractions() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .renderSelectPipelineToSplit(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView.getModel()).containsKey("pipelineOptions");
    assertThat(((Map<String, String>)modelAndView.getModel().get("pipelineOptions")))
        .containsExactly(entry(String.valueOf(PIPELINE_ID.asInt()), PIPELINE_NAME));

    verify(padPipelinesHuooService, times(1)).getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

  }

  @Test
  void splitSelectedPipeline_smokeCheckRolesAccess() {
    mockSelectPipelineForSplitFailedValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void splitSelectedPipeline_smokeCheckAppStatus() {
    mockSelectPipelineForSplitFailedValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void splitSelectedPipeline_smokeCheckAppType() {
    mockSelectPipelineForSplitFailedValidation();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).splitSelectedPipeline(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void splitSelectedPipeline_whenValidationFails() throws Exception {
    mockSelectPipelineForSplitFailedValidation();

    mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .splitSelectedPipeline(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param("pipelineId", String.valueOf(pipeline.getId()))
        .param("numberOfSections", "0")

    )
        .andExpect(status().isOk());

    verify(pickSplitPipelineFormValidator, times(1)).validate(any(), any(), any(Object[].class));
    verify(padPipelinesHuooService, times(0)).removeSplitsForPipeline(any(), any(), any());


  }

  @Test
  void splitSelectedPipeline_whenValidationPasses_withSingleSectionDefined() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .splitSelectedPipeline(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param("pipelineId", String.valueOf(pipeline.getId()))
        .param("numberOfSections", "1")
    )
        .andExpect(status().is3xxRedirection());

    verify(pickSplitPipelineFormValidator, times(1)).validate(any(), any(), any(Object[].class));
    verify(padPipelinesHuooService, times(1)).removeSplitsForPipeline(pwaApplicationDetail, pipeline.getPipelineId(), DEFAULT_ROLE);


  }


  private void mockSelectPipelineForSplitFailedValidation(){
    doAnswer(invocation -> {((BindingResult)invocation.getArgument(1))
        .rejectValue("pipelineId", "pipelineId.required", "pipelineIdRequired");
      return invocation;
    }).when(pickSplitPipelineFormValidator).validate(any(), any(), any(Object[].class));
  }

  @Test
  void renderDefineSections_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderDefineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderDefineSections_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderDefineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderDefineSections_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderDefineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderDefineSections_modelCheck_andServiceInteractions() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .renderDefineSections(APP_TYPE, APP_ID, DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView.getModel()).containsKey("pickableIdentOptions");
    assertThat(modelAndView.getModel()).containsKey("firstSectionStartDescription");
    assertThat(modelAndView.getModel()).containsKey("lastSectionEndDescription");

    assertThat(((Map<String, String>)modelAndView.getModel().get("pickableIdentOptions")))
        .containsExactly(
            entry(String.valueOf(identOption1.getPickableString()), identOption1.getDisplayString()),
            entry(String.valueOf(identOption2.getPickableString()), identOption2.getDisplayString())
        );

    verify(pickableHuooPipelineIdentService, times(1))
        .getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

  }

  @Test
  void renderDefineSections_whenPipelineNotSplittable() throws Exception {

    when(padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
       any(),
        any())
    ).thenThrow(new PwaEntityNotFoundException("fake error"));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .renderDefineSections(APP_TYPE, APP_ID, DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isNotFound());
  }


  @Test
  void defineSections_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).defineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void defineSections_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).defineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void defineSections_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).defineSections(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void defineSections_modelCheck_andServiceInteractions() throws Exception {

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .defineSections(APP_TYPE, APP_ID, DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andReturn().getModelAndView();

    verify(pickableHuooPipelineIdentService, times(1)).generatePipelineSectionsFromForm(any(), any(), any());
    verify(padPipelinesHuooService, times(1)).replacePipelineSectionsForPipelineAndRole(any(), any(), any(), any());


  }

  @Test
  void defineSections_whenPipelineNotSplittable() throws Exception {

    when(padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
        any(),
        any())
    ).thenThrow(new PwaEntityNotFoundException("fake error"));

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .defineSections(APP_TYPE, APP_ID, DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isNotFound());
  }

  @Test
  void defineSections_whenValidationFails() throws Exception {

    doAnswer(invocation -> {
      ((BindingResult) invocation.getArgument(1)).rejectValue("pipelineSectionPoints", "pipelineSectionPoints.fake", "fake msg");
      return invocation;
    })
        .when(definePipelineHuooSectionsFormValidator).validate(any(), any(), any(Object[].class));

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .defineSections(APP_TYPE, APP_ID, DEFAULT_ROLE, PIPELINE_ID.asInt(), NUMBER_OF_SECTIONS, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk());

    verify(pickableHuooPipelineIdentService, times(0)).generatePipelineSectionsFromForm(any(), any(), any());
    verify(padPipelinesHuooService, times(0)).replacePipelineSectionsForPipelineAndRole(any(), any(), any(), any());

  }
}
