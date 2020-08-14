package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaResult;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabService;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;

@RunWith(SpringRunner.class)
@WebMvcTest(WorkAreaController.class)
public class WorkAreaControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  private WorkAreaService workAreaService;

  @MockBean
  private WorkAreaTabService workAreaTabService;

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  @Before
  public void setup() {

    var emptyResultPageView = setupFakeWorkAreaResultPageView(0);
    when(workAreaService.getWorkAreaResult(any(), eq(WorkAreaTab.OPEN_APPLICATIONS), anyInt())).thenReturn(new WorkAreaResult(emptyResultPageView, null));
    when(workAreaTabService.getTabsAvailableToPerson(any())).thenReturn(List.of(WorkAreaTab.values()));

  }

  @Test
  public void renderWorkArea_noWorkAreaPriv() throws Exception {
    var unauthorisedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(),
        EnumSet.noneOf(PwaUserPrivilege.class));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(authenticatedUserAndSession(unauthorisedUserAccount)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderWorkArea_noDefaultTab() throws Exception {

    when(workAreaTabService.getDefaultTabForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderWorkArea_defaultTab() throws Exception {

    when(workAreaTabService.getDefaultTabForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Optional.of(WorkAreaTab.OPEN_APPLICATIONS));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderWorkAreaTab_whenUserDoesNotHaveWorkAreaPriv() throws Exception {
    var unauthorisedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(),
        EnumSet.noneOf(PwaUserPrivilege.class));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, null, null)))
        .with(authenticatedUserAndSession(unauthorisedUserAccount)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderWorkAreaTab_WhenNoPageParamProvided_defaultsApplied() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.OPEN_APPLICATIONS, null)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().isOk());

    verify(workAreaService, times(1))
        .getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 0);
  }


  @Test
  public void renderWorkAreaTab_whenPageParamProvided() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.OPEN_APPLICATIONS, 100)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().isOk());

    verify(workAreaService, times(1))
        .getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 100);
  }

  @Test
  public void renderWorkAreaTab_notAllowedToAccessTab() throws Exception {

    when(workAreaTabService.getTabsAvailableToPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.OPEN_APPLICATIONS, null)))
        .with(authenticatedUserAndSession(authenticatedUserAccount)))
        .andExpect(status().isForbidden());

  }

  private PageView<PwaApplicationWorkAreaItem> setupFakeWorkAreaResultPageView(int page) {
    var fakePage = ApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(),
        PageRequest.of(page, 10)
    );

    return PageView.fromPage(
        fakePage,
        "workAreaUri",
        searchItem -> new PwaApplicationWorkAreaItem(searchItem, applicationDetailSearchItem -> "Fake_View_Url")
    );


  }


}