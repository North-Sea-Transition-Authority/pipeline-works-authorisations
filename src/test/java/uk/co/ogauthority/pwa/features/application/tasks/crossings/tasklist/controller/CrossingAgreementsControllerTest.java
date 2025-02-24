package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = CrossingAgreementsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class CrossingAgreementsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 10;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplicationContext pwaApplicationContext;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
        pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var wua = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_ACCESS));

    // support application context service
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    when(crossingAgreementsService.getValidationResult(pwaApplicationDetail)).thenReturn(new CrossingAgreementsValidationResult(Set.of()));
  }

  @Test
  void renderCrossingAgreementsOverview_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, APP_ID, null, null)))
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  void renderCrossingAgreementsOverview() throws Exception {
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    entity.setNegotiatorName("Name");
    entity.setNegotiatorEmail("Email");
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, APP_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }
}
