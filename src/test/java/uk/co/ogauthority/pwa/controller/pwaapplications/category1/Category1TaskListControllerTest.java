package uk.co.ogauthority.pwa.controller.pwaapplications.category1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.TaskListControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = Category1TaskListController.class)
public class Category1TaskListControllerTest extends TaskListControllerTest {

  private AuthenticatedUserAccount user;
  private MasterPwa masterPwa;
  private PwaApplication pwaApplication;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());
    masterPwa = new MasterPwa(Instant.now());
    pwaApplication = new PwaApplication(masterPwa, PwaApplicationType.CAT_1_VARIATION, 0);
    detail = new PwaApplicationDetail(pwaApplication, 1, user.getWuaId(), Instant.now());

    when(pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT)).thenReturn(detail);
    when(pwaApplicationDetailService.withDraftTipDetail(any(), any(), any())).thenCallRealMethod();
    when(taskListService.getTaskListModelAndView(pwaApplication)).thenCallRealMethod();

  }

  @Test
  public void viewTaskList_Authenticated() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(Category1TaskListController.class).viewTaskList(1, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void viewTaskList_Unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(Category1TaskListController.class).viewTaskList(1, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void viewTaskList_WrongApplicationType() throws Exception {
    var incorrectApplicationTypes = EnumSet.allOf(PwaApplicationType.class);
    incorrectApplicationTypes.remove(PwaApplicationType.CAT_1_VARIATION);
    for (PwaApplicationType wrongType : incorrectApplicationTypes) {
      var invalidApplication = new PwaApplication(masterPwa, wrongType, 0);
      var invalidDetail = new PwaApplicationDetail(invalidApplication, 1, user.getWuaId(), Instant.now());
      when(pwaApplicationDetailService.getTipDetailWithStatus(1,  PwaApplicationStatus.DRAFT)).thenReturn(invalidDetail);
      mockMvc.perform(get(ReverseRouter.route(on(Category1TaskListController.class).viewTaskList(1, null)))
          .with(authenticatedUserAndSession(user))
          .with(csrf()))
          .andExpect(status().is4xxClientError());
    }
  }
}