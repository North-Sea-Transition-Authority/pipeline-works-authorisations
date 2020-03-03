package uk.co.ogauthority.pwa.controller.pwaapplications.options;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OptionsVariationTaskListController.class)
public class OptionsVariationTaskListControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PwaApplicationService pwaApplicationService;

  private AuthenticatedUserAccount user;
  private MasterPwa masterPwa;
  private PwaApplication pwaApplication;

  @Before
  public void setUp() {
    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());
    masterPwa = new MasterPwa(Instant.now());
    pwaApplication = new PwaApplication(masterPwa, PwaApplicationType.OPTIONS_VARIATION, 0);
    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(ModelAndView.class), eq("Task list"));
  }

  @Test
  public void viewTaskList_Authenticated() throws Exception {
    when(pwaApplicationService.getApplicationFromId(1)).thenReturn(pwaApplication);
    var modelAndView = mockMvc.perform(get(
        ReverseRouter.route(on(OptionsVariationTaskListController.class).viewTaskList(1, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        // TODO: Remove hard-coded "PWA-Example-BP-2" once PWA references are in place.
        .andExpect(model().attribute("masterPwaReference", "PWA-Example-BP-2"))
        .andReturn()
        .getModelAndView();
    var informationTasks = (List<TaskListEntry>) modelAndView.getModel().get("informationTasks");
    var applicationTasks = (List<TaskListEntry>) modelAndView.getModel().get("applicationTasks");
    assertThat(informationTasks).extracting(TaskListEntry::getTaskName).containsExactlyInAnyOrder("No tasks");
    assertThat(applicationTasks).extracting(TaskListEntry::getTaskName).containsExactlyInAnyOrder("No tasks");
  }

  @Test
  public void viewTaskList_Unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(OptionsVariationTaskListController.class).viewTaskList(1, null))))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(OptionsVariationTaskListController.class).viewTaskList(1, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void viewTaskList_WrongApplicationType() throws Exception {
    var incorrectApplicationTypes = getIncorrectApplicationTypes(PwaApplicationType.OPTIONS_VARIATION);
    for (PwaApplicationType wrongType : incorrectApplicationTypes) {
      var invalidApplication = new PwaApplication(masterPwa, wrongType, 0);
      when(pwaApplicationService.getApplicationFromId(1)).thenReturn(invalidApplication);
      mockMvc.perform(get(ReverseRouter.route(on(OptionsVariationTaskListController.class).viewTaskList(1, null)))
          .with(authenticatedUserAndSession(user))
          .with(csrf()))
          // TODO: Remove hard-coded "PWA-Example-BP-2" once PWA references are in place.
          .andExpect(status().is4xxClientError());
    }
  }

  private List<PwaApplicationType> getIncorrectApplicationTypes(PwaApplicationType correctApplicationType) {
    return Arrays.stream(PwaApplicationType.values())
        .filter(pwaApplicationType -> !pwaApplicationType.equals(correctApplicationType))
        .collect(Collectors.toList());
  }

}