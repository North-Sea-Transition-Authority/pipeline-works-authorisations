package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
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
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ModifyPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class ModifyPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {
  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final Set<String> PICKED_PIPELINE_IDS = Set.of("123++CONSENTED", "123++APPLICATION");
  private final Set<Integer> PICKED_ORG_IDS = Set.of(1, 100);


  private final String FORM_PICKED_PIPELINE_ATTR = "pickedPipelineStrings";
  private final String FORM_PICKED_ORG_ATTR = "organisationUnitIds";
  private final String FORM_PICKED_TREATY_ATTR = "treatyAgreements";

  private Pipeline pipeline1;
  private Pipeline pipeline2;
  Set<PipelineIdentifier> pickedPipelines;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PickableHuooPipelineService pickableHuooPipelineService;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  @Before
  public void setup() {
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
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_validForm_setsJourneyDataAndRedirects_thenLoadsJourneyDataIntoForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations"));

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null)))
        .with(authenticatedUserAndSession(user))
    )
        //  .andExpect(status().isOk())
        .andExpect(model().attributeExists("form"))
        .andReturn();
    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getPickedPipelineStrings()).isEqualTo(PICKED_PIPELINE_IDS);

  }

  @Test
  public void selectPipelinesForHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk());


  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE,
                    null, null, null, null, null
                ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void completeValidJourney_journeyCompleteSubmitServiceInteraction() throws Exception {

    Set<PipelineIdentifier> pickedPipelines = Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickableHuooPipelineService.getPickedPipelinesFromStrings(any(), any(), any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE,
            null, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
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
  public void completeValidJourney_journeyDataGetsClearedOnCompletion() throws Exception {

    Set<PipelineIdentifier> pickedPipelines = Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickableHuooPipelineService.getPickedPipelinesFromStrings(any(), any(), any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    // Step 1: Mock loading and selecting of Pipelines
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_IDS.toArray(new String[0]))
    )
        .andExpect(status().is3xxRedirection());

    // Step 2: select orgs and complete journey
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE,
            null, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
        .param(FORM_PICKED_TREATY_ATTR, String.format("%s,%s", TreatyAgreement.ANY_TREATY_COUNTRY, TreatyAgreement.ANY_TREATY_COUNTRY))
    )
        .andExpect(status().is3xxRedirection());

    // Step 3: loading up each journey page has nothing pre-selected
    assertPreLoadedJourneyDataMatches(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());


  }


  @Test
  public void selectOrganisationsForPipelineHuooAssignment_invalidForm() throws Exception {

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
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
    )
        .andExpect(status().isOk());

    verify(padPipelinesHuooService, times(0)).updatePipelineHuooLinks(any(), any(), any(), any(), any());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_selectedOrganisationsPersisted_andSelectedTreatiesPersisted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .returnToPipelineSelection(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_IDS.stream().map(Object::toString).toArray(String[]::new))
        .param(FORM_PICKED_TREATY_ATTR, TreatyAgreement.ANY_TREATY_COUNTRY.name())
        .param(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "")
    )
        .andExpect(status().is3xxRedirection());

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();

    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getOrganisationUnitIds()).isEqualTo(PICKED_ORG_IDS);
    assertThat(form.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);

  }


  @Test
  public void editGroupRouter_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
                null,
                null
            ))));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void editGroupRouter_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
                null,
                null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void editGroupRouter_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
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
  public void editGroupRouter_pipelineSelectionPage_preventsInvalidPipelinesPopulatingJourney() throws Exception {
    var pipelineId = new PipelineId(1);
    var pickablePipelineStrings = Set.of(
        "INVALID ID STRING",
        PickableHuooPipelineType.createPickableString(pipelineId)
    );
    var reconciledPipeline = PickablePipelineOptionTestUtil.createReconciledPickablePipeline(
        pipelineId
    );

    when(padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        pickablePipelineStrings
    )).thenReturn(Set.of(reconciledPipeline));

    // ReverseRouter.route uri encodes the string here
    String postUrlEncoded = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        PwaApplicationType.INITIAL,
        pwaApplicationDetail.getMasterPwaApplicationId(),
        DEFAULT_ROLE,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
        encodeStringSet(pickablePipelineStrings),
        null,
        null
    ));

    // decode the uri encoded string here (mimic Spring) otherwise the Base64 decode fails on the
    // picked pipeline strings
    String postUrlDecoded = URLDecoder.decode(postUrlEncoded, StandardCharsets.UTF_8);

    // check redirect target as expected
    mockMvc.perform(post(postUrlDecoded)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines"))
        .andReturn();

    assertPreLoadedJourneyDataMatches(Set.of(reconciledPipeline.getPickableHuooPipelineId().asString()),
        Collections.emptySet(), Collections.emptySet());

  }



  @Test
  public void editGroupRouter_pipelineJourneyPageParam_redirect() throws Exception {

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
            null,
            null,
            null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines"));
  }

  @Test
  public void editGroupRouter_organisationJourneyPageParam_redirect() throws Exception {

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.ORGANISATION_SELECTION,
            null,
            null,
            null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines/organisations"));
  }


  private void assertPreLoadedJourneyDataMatches(Set<String> pickedPipelines,
                                                 Set<Integer> orgUnitsIds,
                                                 Set<TreatyAgreement> treatyAgreements) throws Exception {
    // load selected pipelines screen for same huoo journey and app and assert matching pipelines match
    MvcResult pipelineResult = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
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
        .with(authenticatedUserAndSession(user))
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

  // have to mimic encoding done by url factory. Yuck.
  private Set<String> encodeStringSet(Set<String> stringSet){
    var encoder = Base64.getUrlEncoder();
    return stringSet.stream()
        .map(s -> encoder.encodeToString(s.getBytes()))
        .collect(Collectors.toSet());
  }

}
