package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadProjectInformationService;
import uk.co.ogauthority.pwa.validators.FastTrackValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = FastTrackController.class)
public class FastTrackControllerTest extends AbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PadProjectInformationService padProjectInformationService;

  @MockBean
  private FastTrackValidator fastTrackValidator;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);
  }

  @Test
  public void authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class).renderFastTrack(PwaApplicationType.INITIAL, 1, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    ).andExpect(status().isOk());


    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class).renderFastTrack(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

  @Test
  public void renderFastTrack() {
  }

  @Test
  public void postCompleteFastTrack() {
  }

  @Test
  public void postContinueFastTrack() {
  }
}