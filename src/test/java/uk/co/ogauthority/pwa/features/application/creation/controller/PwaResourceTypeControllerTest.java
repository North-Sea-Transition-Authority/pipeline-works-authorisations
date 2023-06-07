package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeFormValidator;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaResourceTypeController.class)
@Import(PwaMvcTestConfiguration.class)
public class PwaResourceTypeControllerTest extends AbstractControllerTest {

  @MockBean
  PwaResourceTypeFormValidator validator;


  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

  private AuthenticatedUserAccount userNoPrivs = new AuthenticatedUserAccount(new WebUserAccount(999),
      Collections.emptyList());

  @Test
  public void renderResourceScreen_withAuthenticatedUser() throws Exception {
    var resourceOptions = Arrays.stream(PwaResourceType.values())
        .sorted(Comparator.comparingInt(PwaResourceType::getDisplayOrder))
        .collect(Collectors.toList());

    mockMvc.perform(get(ReverseRouter.route(on(PwaResourceTypeController.class)
        .renderResourceTypeForm(PwaApplicationType.INITIAL, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("resourceOptions", resourceOptions));
  }

  @Test
  public void renderResourceScreen_noPrivileges() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PwaResourceTypeController.class)
        .renderResourceTypeForm(PwaApplicationType.INITIAL, null, null)))
        .with(authenticatedUserAndSession(userNoPrivs)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postResourceScreen_noPrivileges() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(PwaApplicationType.INITIAL, null, null, null)))
        .with(authenticatedUserAndSession(userNoPrivs)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postResourceScreen_valid() throws Exception {
    var form = new PwaResourceTypeForm();
    form.setResourceType(PwaResourceType.HYDROGEN);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(PwaApplicationType.INITIAL, form, bindingResult, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("resourceType", PwaResourceType.HYDROGEN.name()));
    verify(validator).validate(any(), any());
    verify(pwaApplicationRedirectService).getStartApplicationRedirect(PwaApplicationType.INITIAL, PwaResourceType.HYDROGEN);
  }
}
