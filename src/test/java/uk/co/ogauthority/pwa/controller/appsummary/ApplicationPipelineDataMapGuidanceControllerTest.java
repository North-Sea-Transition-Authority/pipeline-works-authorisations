package uk.co.ogauthority.pwa.controller.appsummary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionAccessRequester;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionRequestType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = ApplicationPipelineDataMapGuidanceController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ApplicationPipelineDataMapGuidanceControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationVersionAccessRequester applicationVersionAccessRequester;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() throws Exception {

    endpointTester = new PwaApplicationEndpointTestBuilder(
        mockMvc,
        pwaApplicationDetailService,
        pwaAppProcessingPermissionService
    )
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.CAT_1_VARIATION, 25, 26);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(
            1,
            PersonTestUtil.createDefaultPerson()
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );

    when(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(any()))
        .thenReturn(EnumSet.of(ApplicationVersionRequestType.LAST_SUBMITTED));

  }


  @Test
  void getLatestAvailableAppPipelinesForUserAsGeoJson_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationPipelineDataMapGuidanceController.class)
                .renderMappingGuidance(
                    applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void getLatestAvailableAppPipelinesForUserAsGeoJson_modelCheck() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);
    when(pwaApplicationDetailService.getLatestDetailForUser(eq(25), any())).thenReturn(Optional.of(pwaApplicationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPipelineDataMapGuidanceController.class).renderMappingGuidance(
            pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("caseSummaryView"))
        .andExpect(model().attributeExists("pipelineDataDownloadOptionItems"))
        .andExpect(model().attributeExists("serviceName"))
        .andExpect(model().attributeExists("regulatorMapsAndToolsUrl"))
        .andExpect(model().attributeExists("regulatorMapsAndToolsLabel"))
        .andExpect(model().attributeExists("offshoreMapLabel"));

  }


}