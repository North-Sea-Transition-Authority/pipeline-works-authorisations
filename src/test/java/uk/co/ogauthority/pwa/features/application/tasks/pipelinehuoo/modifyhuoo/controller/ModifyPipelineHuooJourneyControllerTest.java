package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller.ModifyPipelineHuooJourneyController.UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickablePipelineOptionTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineHuooViewTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = ModifyPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class ModifyPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {
  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final Set<String> PICKED_PIPELINE_IDS = Set.of("123++CONSENTED", "123++APPLICATION");
  private final Set<Integer> PICKED_ORG_IDS = Set.of(1, 100);
  private final String GROUP_KEY = "group-key";


  private final String FORM_PICKED_PIPELINE_ATTR = "pickedPipelineStrings";
  private final String FORM_PICKED_ORG_ATTR = "organisationUnitIds";
  private final String FORM_PICKED_TREATY_ATTR = "treatyAgreements";

  private Pipeline pipeline1;
  private Pipeline pipeline2;
  Set<PipelineIdentifier> pickedPipelines;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockitoBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockitoBean
  private PickableHuooPipelineService pickableHuooPipelineService;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  @BeforeEach
  void setup() {
    pipeline1 = new Pipeline();
    pipeline1.setId(1);
    pipeline2 = new Pipeline();
    pipeline2.setId(2);

     pickedPipelines = Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId());

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
            PwaApplicationType.OPTIONS_VARIATION,
            PwaApplicationType.PIPELINE_RECORD_MANAGEMENT
        )
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

  @Test
  void renderPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void selectPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void selectPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void selectPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void selectPipelinesForHuooAssignment_validForm_setsJourneyDataAndRedirects_thenLoadsJourneyDataIntoForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations"));

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null)))
        .with(user(user))
    )
        //  .andExpect(status().isOk())
        .andExpect(model().attributeExists("form"))
        .andReturn();
    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getPickedPipelineStrings()).isEqualTo(PICKED_PIPELINE_IDS);

  }

  @Test
  void selectPipelinesForHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk());


  }

  @Test
  void renderOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void selectOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void completeValidJourney_journeyCompleteSubmitServiceInteraction() throws Exception {

    Set<PipelineIdentifier> pickedPipelines = Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickableHuooPipelineService.getPickedPipelinesFromStrings(any(), any(), any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE,
            null, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
        .param(FORM_PICKED_TREATY_ATTR, String.format("%s", TreatyAgreement.ANY_TREATY_COUNTRY))
    )
        .andExpect(status().is3xxRedirection());

    verify(pickableHuooPipelineService, times(1)).getPickedPipelinesFromStrings(
        pwaApplicationDetail, DEFAULT_ROLE, PICKED_PIPELINE_IDS);

    verify(padPipelinesHuooService, times(1))
        .updatePipelineHuooLinks(
            pwaApplicationDetail,
            pickedPipelines,
            DEFAULT_ROLE,
            PICKED_ORG_IDS.stream().map(OrganisationUnitId::new)
                .collect(Collectors.toSet()),
            Set.of(TreatyAgreement.ANY_TREATY_COUNTRY)
        );

  }

  @Test
  void completeValidJourney_journeyDataGetsClearedOnCompletion() throws Exception {

    Set<PipelineIdentifier> pickedPipelines = Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickableHuooPipelineService.getPickedPipelinesFromStrings(any(), any(), any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    // Step 1: Mock loading and selecting of Pipelines
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection());

    // Step 2: select orgs and complete journey
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE,
            null, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
        .param(FORM_PICKED_TREATY_ATTR, String.format("%s,%s", TreatyAgreement.ANY_TREATY_COUNTRY, TreatyAgreement.ANY_TREATY_COUNTRY))
    )
        .andExpect(status().is3xxRedirection());

    // Step 3: loading up each journey page has nothing pre-selected
    assertPreLoadedJourneyDataMatches(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());


  }


  @Test
  void selectOrganisationsForPipelineHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE,
            null, null, null, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
    )
        .andExpect(status().isOk());

    verify(padPipelinesHuooService, times(0)).updatePipelineHuooLinks(any(), any(), any(), any(), any());

  }

  @Test
  void returnToPipelineSelection_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void returnToPipelineSelection_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void returnToPipelineSelection_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                applicationType, applicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void returnToPipelineSelection_selectedOrganisationsPersisted_andSelectedTreatiesPersisted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .returnToPipelineSelection(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(user(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
        .param(FORM_PICKED_TREATY_ATTR, TreatyAgreement.ANY_TREATY_COUNTRY.name())
        .param(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "")
    )
        .andExpect(status().is3xxRedirection());

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();

    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getOrganisationUnitIds()).isEqualTo(PICKED_ORG_IDS);
    assertThat(form.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);

  }


  @Test
  void assignUnassignedPipelines_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).assignUnassignedPipelines(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void assignUnassignedPipelines_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).assignUnassignedPipelines(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void assignUnassignedPipelines_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).assignUnassignedPipelines(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void editGroup_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroup(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                GROUP_KEY,
                null,
                null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void editGroup_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroup(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                GROUP_KEY,
                null,
                null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void editGroup_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((applicationDetail, applicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroup(
                applicationType,
                applicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                GROUP_KEY,
                null,
                null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }


  private MultiValueMap<String, String> generateIdParams(String paramName, int... ids) {

    var map = new LinkedMultiValueMap<String, String>();
    Arrays.stream(ids).forEach(o -> map.add(paramName, String.valueOf(o)));
    return map;
  }


  @Test
  void assignUnassignedPipelines_preloadsReconciledUnassignedPipelinesAndRedirectsToPipelineSelection() throws Exception {
    var unassignedPipelineId = new PipelineId(1);
    var reconciledPipeline = PickablePipelineOptionTestUtil.createReconciledPickablePipeline(unassignedPipelineId);

    when(padPipelinesHuooService.getUnassignedPipelineIdentifiersForRole(pwaApplicationDetail, DEFAULT_ROLE))
        .thenReturn(Set.of(unassignedPipelineId));
    when(padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(PickableHuooPipelineType.createPickableString(unassignedPipelineId))
    )).thenReturn(Set.of(reconciledPipeline));

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
            .assignUnassignedPipelines(PwaApplicationType.INITIAL, APP_ID, DEFAULT_ROLE, null)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines"));

    assertPreLoadedJourneyDataMatches(Set.of(reconciledPipeline.getPickableHuooPipelineId().asString()),
        Collections.emptySet(), Collections.emptySet());
  }

  @Test
  void editGroup_whenGroupFound_preloadsGroupPipelinesAndOrgsAndRedirectsToOrganisationSelection() throws Exception {
    var pipelineId = new PipelineId(1);
    var reconciledPipeline = PickablePipelineOptionTestUtil.createReconciledPickablePipeline(pipelineId);
    var orgUnitId = new OrganisationUnitId(5);
    var orgRoleOwner = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(orgUnitId);
    var treatyRoleOwner = OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(TreatyAgreement.ANY_TREATY_COUNTRY);
    var groupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        Set.of(pipelineId),
        Set.of(orgRoleOwner, treatyRoleOwner),
        List.of("PL1"),
        List.of("org", "treaty")
    );

    when(padPipelinesHuooService.getPipelinesAndOrgRoleGroupView(pwaApplicationDetail, DEFAULT_ROLE, GROUP_KEY))
        .thenReturn(Optional.of(groupView));
    when(padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(PickableHuooPipelineType.createPickableString(pipelineId))
    )).thenReturn(Set.of(reconciledPipeline));
    when(padPipelinesHuooService.reconcileOrganisationRoleOwnersFrom(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(orgUnitId.asInt()),
        Set.of(TreatyAgreement.ANY_TREATY_COUNTRY)
    )).thenReturn(Set.of(orgRoleOwner, treatyRoleOwner));

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
            .editGroup(PwaApplicationType.INITIAL, APP_ID, DEFAULT_ROLE, GROUP_KEY, null, null)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines/organisations"));

    assertPreLoadedJourneyDataMatches(Set.of(reconciledPipeline.getPickableHuooPipelineId().asString()),
        Set.of(orgUnitId.asInt()), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
  }

  @Test
  void editGroup_whenGroupNotFound_redirectsToSummary() throws Exception {
    when(padPipelinesHuooService.getPipelinesAndOrgRoleGroupView(pwaApplicationDetail, DEFAULT_ROLE, GROUP_KEY))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
            .editGroup(PwaApplicationType.INITIAL, APP_ID, DEFAULT_ROLE, GROUP_KEY, null, null)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo"));

    verify(padPipelinesHuooService, never()).reconcilePickablePipelinesFromPipelineIds(any(), any(), any());
  }


  private void assertPreLoadedJourneyDataMatches(Set<String> pickedPipelines,
                                                 Set<Integer> orgUnitsIds,
                                                 Set<TreatyAgreement> treatyAgreements) throws Exception {
    // load selected pipelines screen for same huoo journey and app and assert matching pipelines match
    MvcResult pipelineResult = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();
    var pipelinePageForm = (PickHuooPipelinesForm) pipelineResult.getModelAndView().getModelMap().getAttribute("form");
    if (pickedPipelines.isEmpty()) {
      assertThat(pipelinePageForm.getPickedPipelineStrings()).isEmpty();
    } else {
      assertThat(pipelinePageForm.getPickedPipelineStrings()).containsExactlyInAnyOrder(
          pickedPipelines.toArray(String[]::new));
    }

    // load select organisations screen for same huoo journey and app and assert expected orgs match
    MvcResult orgResult = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();
    var orgPageForm = (PickHuooPipelinesForm) orgResult.getModelAndView().getModelMap().getAttribute("form");
    if (orgUnitsIds.isEmpty()) {
      assertThat(orgPageForm.getOrganisationUnitIds()).isEmpty();
    } else {
      assertThat(orgPageForm.getOrganisationUnitIds()).containsExactlyInAnyOrder(orgUnitsIds.toArray(Integer[]::new));
    }

    if (treatyAgreements.isEmpty()) {
      assertThat(orgPageForm.getTreatyAgreements()).isEmpty();
    } else {
      assertThat(orgPageForm.getTreatyAgreements()).containsExactlyInAnyOrder(
          treatyAgreements.toArray(TreatyAgreement[]::new));
    }

  }

}
