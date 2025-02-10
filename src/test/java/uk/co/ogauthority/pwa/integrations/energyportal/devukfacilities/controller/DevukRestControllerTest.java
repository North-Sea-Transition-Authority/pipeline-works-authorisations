package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

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
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacilityService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DevukRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class DevukRestControllerTest extends PwaApplicationContextAbstractControllerTest {

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
    mockMvc.perform(get(ReverseRouter.route(on(DevukRestController.class).searchFacilities("Test"))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void searchFacilities_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(DevukRestController.class).searchFacilities("Test")))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

}