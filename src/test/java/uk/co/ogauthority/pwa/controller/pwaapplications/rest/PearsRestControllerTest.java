package uk.co.ogauthority.pwa.controller.pwaapplications.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PearsRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PearsRestControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PearsBlockService pearsBlockService;

  @MockBean
  private SearchSelectorService searchSelectorService;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    when(pearsBlockService.findOffshorePickablePearsBlocks(any(), any())).thenReturn(List.of());
    user = new AuthenticatedUserAccount(new WebUserAccount(), Set.of());
  }

  @Test
  public void searchBlocks_unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PearsRestController.class).searchBlocks("Test"))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void searchBlocks_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PearsRestController.class).searchBlocks("Test")))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

}