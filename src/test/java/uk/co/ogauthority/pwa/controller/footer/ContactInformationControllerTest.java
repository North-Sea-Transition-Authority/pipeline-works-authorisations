package uk.co.ogauthority.pwa.controller.footer;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(ContactInformationController.class)
public class ContactInformationControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;


  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  private static final AuthenticatedUserAccount unAuthenticatedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      Set.of());


  @Test
  public void getContactInformation_whenAuthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformation(null)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().isOk());
  }

  @Test
  public void getContactInformation_whenUnauthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformation(null)))
        .with(authenticatedUserAndSession(unAuthenticatedUser)))
        .andExpect(status().isOk());
  }

}