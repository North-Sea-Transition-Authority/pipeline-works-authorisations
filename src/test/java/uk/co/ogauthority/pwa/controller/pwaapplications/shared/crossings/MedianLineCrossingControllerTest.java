package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

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
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.MedianLineAgreementValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MedianLineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class MedianLineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @MockBean
  private MedianLineAgreementValidator medianLineAgreementValidator;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private MedianLineCrossingFileService medianLineCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;
  private PadMedianLineAgreement agreement;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private int APP_ID = 100;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);

    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.DECOMMISSIONING);

    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.DECOMMISSIONING
        )
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    agreement = new PadMedianLineAgreement();
    agreement.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    when(padMedianLineAgreementService.getMedianLineAgreement(any())).thenReturn(agreement);
  }

  @Test
  public void renderMedianLineForm_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(MedianLineCrossingController.class).renderMedianLineForm(invalidAppType, APP_ID, null, null)))
                    .with(authenticatedUserAndSession(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  public void renderMedianLineForm_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, APP_ID, null,
                null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void renderMedianLineOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getAllowedTypes().size())).getUploadedFileViews(any(),
        eq(ApplicationFilePurpose.MEDIAN_LINE_CROSSING), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void renderMedianLineOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    verify(padFileService, times(endpointTester.getAllowedStatuses().size())).getUploadedFileViews(any(),
        eq(ApplicationFilePurpose.MEDIAN_LINE_CROSSING), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void renderMedianLineOverview_appContactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getContactRoles().size())).getUploadedFileViews(any(),
        eq(ApplicationFilePurpose.MEDIAN_LINE_CROSSING), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  public void postOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  public void postOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void postOverview_appContactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void postAddContinueMedianLine_appTypeSmokeTest_complete() {

    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void postAddContinueMedianLine_appStatusSmokeTest() {
    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  public void postAddContinueMedianLine_appContactRoleSmokeTest() {
    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());
  }

}