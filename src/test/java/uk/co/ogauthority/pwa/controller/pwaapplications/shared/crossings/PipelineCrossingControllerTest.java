package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingOwnerService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.PipelineCrossingFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private int APP_ID = 100;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;
  private PwaApplicationEndpointTestBuilder endpointTester;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineCrossingService padPipelineCrossingService;

  @MockBean
  private PipelineCrossingFileService pipelineCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;

  @MockBean
  private PipelineCrossingFormValidator pipelineCrossingFormValidator;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING
        )
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);
  }

  @Test
  public void renderOverview_appTypeSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getAllowedTypes().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.PIPELINE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void renderOverview_appStatusSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    verify(padFileService, times(endpointTester.getAllowedStatuses().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.PIPELINE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void renderOverview_appContactRoleSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getContactRoles().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.PIPELINE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void postOverview_appTypeSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void postOverview_appStatusSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  public void postOverview_appContactRoleSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());
  }

}