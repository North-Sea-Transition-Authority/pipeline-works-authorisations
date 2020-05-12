package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingOwnerService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingFileService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.PipelineCrossingFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;

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
        PwaApplicationType.DEPOSIT_CONSENT);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());
  }

  @Test
  public void renderBlockCrossingOverview_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(PipelineCrossingController.class).renderOverview(PwaApplicationType.INITIAL, 1, null,
                null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void renderBlockCrossingOverview_authenticated() throws Exception {

    var validationResult = new CrossingAgreementsValidationResult(Set.of());
    when(crossingAgreementsService.getValidationResult(pwaApplicationDetail)).thenReturn(validationResult);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(PipelineCrossingController.class).renderOverview(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void postOverview_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(PipelineCrossingController.class)
                .postOverview(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postOverview_authenticated() throws Exception {

    var validationResult = new CrossingAgreementsValidationResult(Set.of());
    when(crossingAgreementsService.getValidationResult(pwaApplicationDetail)).thenReturn(validationResult);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(PipelineCrossingController.class)
                .postOverview(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

}