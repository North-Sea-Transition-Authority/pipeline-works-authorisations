package uk.co.ogauthority.pwa.controller.search.consents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltSubmissionHistoryView;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltSubmissionHistoryViewUtil;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.PwaHuooHistoryItemType;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.PwaPipelineHistoryViewService;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.ViewablePipelineHuooVersionService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@WebMvcTest(controllers = PwaPipelineViewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaContextService.class}))
class PwaViewPipelineControllerTest extends PwaContextAbstractControllerTest {

  private PwaEndpointTestBuilder endpointTester;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;

  @MockBean
  protected PwaPermissionService pwaPermissionService;

  @MockBean
  protected PipelineDetailService pipelineDetailService;

  @MockBean
  protected PwaConsentService pwaConsentService;

  @MockBean
  protected PwaPipelineHistoryViewService pwaPipelineHistoryViewService;

  @MockBean
  protected ViewablePipelineHuooVersionService viewablePipelineHuooVersionService;

  @MockBean
  protected AsBuiltViewerService asBuiltViewerService;

  @MockBean
  protected UserTypeService userTypeService;

  @MockBean
  protected PadPipelineTransferService padPipelineTransferService;

  private static int PIPELINE_ID = 1;

  private final AsBuiltSubmissionHistoryView historyView = AsBuiltSubmissionHistoryViewUtil.createDefaultAsBuiltSubmissionHistoryView();

  @BeforeEach
  void setUp() {

    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA_PIPELINE);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        Set.of());

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);
    this.masterPwa.setCreatedTimestamp(Instant.MIN);
    when(masterPwaService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);

    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of(PwaPermission.VIEW_PWA_PIPELINE));

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setPipelineNumber("PL1");
    when(pipelineDetailService.getLatestByPipelineId(any())).thenReturn(pipelineDetail);

    var pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);
    pipeline.setMasterPwa(masterPwa);
    when(pipelineService.getPipelineFromId(new PipelineId(PIPELINE_ID))).thenReturn(pipeline);

    when(pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(any(), any())).thenReturn(Map.of());
    when(pwaPipelineHistoryViewService.getPipelinesVersionSearchSelectorItems(any())).thenReturn(Map.of());

    when(pwaConsentService.getLatestConsent(any())).thenReturn(new PwaConsent());


    when(viewablePipelineHuooVersionService.getHuooHistorySearchSelectorItems(any(), any())).thenReturn(Map.of(
        PwaHuooHistoryItemType.PWA_CONSENT.getItemPrefix() + 1, "org"));

    when(viewablePipelineHuooVersionService.getDiffableOrgRolePipelineGroupsFromHuooVersionString(any(), any(), any()))
        .thenReturn(new DiffedAllOrgRolePipelineGroups(List.of(), List.of(), List.of(), List.of()));

    when(asBuiltViewerService.getHistoricAsBuiltSubmissionView(PIPELINE_ID))
        .thenReturn(historyView);

    when(userTypeService.getUserTypes(user))
        .thenReturn(Set.of(UserType.OGA));
  }

  @Test
  void renderViewPwaPipeline_pipelineHistoryTab_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .renderViewPwaPipeline(1, PIPELINE_ID, PwaPipelineViewTab.PIPELINE_HISTORY, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderViewPwaPipeline_huooHistoryTab_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .renderViewPwaPipeline(1, PIPELINE_ID, PwaPipelineViewTab.HUOO_HISTORY, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderViewPwaPipeline_asBuiltSubmissionsTab_processingPermissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .renderViewPwaPipeline(1, PIPELINE_ID, PwaPipelineViewTab.AS_BUILT_NOTIFICATION_HISTORY, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderViewPwaPipeline_nullTab() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .renderViewPwaPipeline(1, PIPELINE_ID, null, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isNotFound(), status().isNotFound());

  }

  @Test
  void postViewPwaPipeline_pipelineHistoryTab_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .postViewPwaPipeline(1, PIPELINE_ID, PwaPipelineViewTab.PIPELINE_HISTORY, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postViewPwaPipeline_huooHistoryTab_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .postViewPwaPipeline(1, PIPELINE_ID, PwaPipelineViewTab.HUOO_HISTORY, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postViewPwaPipeline_nullTab() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaPipelineViewController.class)
                .postViewPwaPipeline(1, PIPELINE_ID, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isNotFound(), status().isNotFound());

  }

}