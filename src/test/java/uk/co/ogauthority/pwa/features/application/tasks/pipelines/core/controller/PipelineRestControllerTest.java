package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.externalapi.PipelineDtoRepository;
import uk.co.ogauthority.pwa.externalapi.PipelineDtoTestUtil;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = PipelineRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PipelineRestControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockitoBean
  private SearchSelectorService searchSelectorService;

  @MockitoBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockitoBean
  private PipelineDtoRepository pipelineDtoRepository;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private int APP_ID = 1;

  @BeforeEach
  void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(padPipelineService.getPipelineBundleNamesByDetail(any())).thenReturn(List.of());

    var context = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());
    when(pwaApplicationContextService.validateAndCreate(any())).thenReturn(context);
  }

  @Test
  void searchBundleNames_authenticationTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineRestController.class)
                .searchBundleNames(applicationDetail.getMasterPwaApplicationId(), null, "term")));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isOk());
  }

  @Test
  void searchPipelines() throws Exception {
    var searchTerm = "PL10";
    var pipelineDto = PipelineDtoTestUtil.builder()
        .withId(100)
        .withNumber("PL10")
        .build();

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(any(), any())).thenReturn(true);
    when(pipelineDtoRepository.searchPipelines(null, searchTerm, null))
        .thenReturn(List.of(pipelineDto));

    mockMvc.perform(get(ReverseRouter.route(on(PipelineRestController.class)
            .searchPipelines(searchTerm)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
          {"results":[{"id": "100", "text":"PL10"}]}
         """))
        .andReturn();
  }

  @Test
  void searchPipelines_whenInvalidTerm_returnEmptyList() throws Exception {
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(any(), any())).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(PipelineRestController.class)
            .searchPipelines("a")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
          {"results":[]}
         """))
        .andReturn();

    verify(pipelineDtoRepository, never()).searchPipelines(any(), any(), any());
  }

}
