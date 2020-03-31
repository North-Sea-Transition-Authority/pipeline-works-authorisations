package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadMedianLineAgreementService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CrossingAgreementsController.class)
public class CrossingAgreementsControllerTest extends AbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplicationContext pwaApplicationContext;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, 1);
    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    var wua = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(wua, Set.of());

    pwaApplicationContext = new PwaApplicationContext(pwaApplicationDetail, wua, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any()))
        .thenReturn(pwaApplicationContext);
  }

  @Test
  public void renderCrossingAgreementsOverview_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, null, null), Map.of("applicationId", 1)))
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  public void renderCrossingAgreementsOverview() throws Exception {

    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    entity.setNegotiatorName("Name");
    entity.setNegotiatorEmail("Email");
    when(padMedianLineAgreementService.getMedianLineAgreementForDraft(pwaApplicationDetail)).thenReturn(entity);

    var model = Objects.requireNonNull(mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, null, null), Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView())
        .getModel();
    assertThat(model).containsKeys("medianLineAgreementView");
  }
}