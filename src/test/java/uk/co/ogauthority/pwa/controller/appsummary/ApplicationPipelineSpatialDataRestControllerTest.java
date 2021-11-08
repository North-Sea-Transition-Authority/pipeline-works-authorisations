package uk.co.ogauthority.pwa.controller.appsummary;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.geojsonview.ApplicationPipelineGeoJsonViewFactory;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionAccessRequester;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionRequestType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ApplicationPipelineSpatialDataRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ApplicationPipelineSpatialDataRestControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationPipelineGeoJsonViewFactory applicationPipelineGeoJsonViewFactory;

  @MockBean
  private ApplicationVersionAccessRequester applicationVersionAccessRequester;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() throws Exception {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService,
        pwaAppProcessingPermissionService)
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

    when(applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(any(), any()))
        .thenReturn(Optional.of(pwaApplicationDetail));

  }

  @Test
  public void getLatestAvailableAppPipelinesForUserAsGeoJson_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationPipelineSpatialDataRestController.class)
                .getLatestAvailableAppPipelinesForUserAsGeoJson(applicationDetail.getMasterPwaApplicationId(), type,
                    null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void getLatestAvailableAppPipelinesForUserAsGeoJson_responseCheck() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);
    when(pwaApplicationDetailService.getLatestDetailForUser(eq(25), any())).thenReturn(Optional.of(pwaApplicationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPipelineSpatialDataRestController.class)
        .getLatestAvailableAppPipelinesForUserAsGeoJson(pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(authenticatedUserAndSession(user))
    )
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/geo+json"))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"APP_REFERENCE-25_v1_")))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".geojson")))
        .andExpect(status().isOk());

  }

  @Test
  public void getLatestAvailableAppPipelinesForUserAsGeoJson_pipelineDataUsesRequestedAppDetailVersion() throws Exception {

    var currentDraftDetail = new PwaApplicationDetail();
    when(applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(any(), any())).thenReturn(Optional.of(currentDraftDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);
    when(pwaApplicationDetailService.getLatestDetailForUser(eq(25), any())).thenReturn(Optional.of(pwaApplicationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPipelineSpatialDataRestController.class)
        .getLatestAvailableAppPipelinesForUserAsGeoJson(pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(), ApplicationVersionRequestType.CURRENT_DRAFT, null)))
        .with(authenticatedUserAndSession(user))
    );

    verify(applicationPipelineGeoJsonViewFactory).createApplicationPipelinesAsLineFeatures(currentDraftDetail);
    verifyNoMoreInteractions(applicationPipelineGeoJsonViewFactory);

  }
}