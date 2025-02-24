package uk.co.ogauthority.pwa.features.webapp.footer.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(ContactInformationController.class)
@Import(PwaMvcTestConfiguration.class)
class ContactInformationControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_ACCESS, PwaUserPrivilege.PWA_WORKAREA));

  private static final AuthenticatedUserAccount unAuthenticatedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      Set.of());


  @Test
  void getContactInformation_whenAuthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformation(null)))
        .with(user(authenticatedUserAccount)))
        .andExpect(status().isOk());
  }

  @Test
  void getContactInformation_whenUnauthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformation(null)))
        .with(user(unAuthenticatedUser)))
        .andExpect(status().isForbidden());
  }

}