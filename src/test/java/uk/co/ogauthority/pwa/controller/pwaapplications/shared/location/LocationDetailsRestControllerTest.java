package uk.co.ogauthority.pwa.controller.pwaapplications.shared.location;

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
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LocationDetailsRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class LocationDetailsRestControllerTest extends AbstractControllerTest {

  @MockBean
  private DevukFacilityService devukFacilityService;

  @MockBean
  private SearchSelectorService searchSelectorService;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    when(devukFacilityService.getFacilities(any())).thenReturn(List.of());
    user = new AuthenticatedUserAccount(new WebUserAccount(), Set.of());
  }

  @Test
  public void searchFacilities_unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(LocationDetailsRestController.class).searchFacilities("Test"))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void searchFacilities_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(LocationDetailsRestController.class).searchFacilities("Test")))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

}