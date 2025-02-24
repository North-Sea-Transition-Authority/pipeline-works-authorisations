package uk.co.ogauthority.pwa.features.feemanagement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.feemanagement.display.FeePeriodDisplayService;
import uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeePeriodDetail;
import uk.co.ogauthority.pwa.features.feemanagement.service.FeePeriodService;
import uk.co.ogauthority.pwa.features.feemanagement.service.FeePeriodValidator;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;

@WebMvcTest(FeeManagementController.class)
@Import(PwaMvcTestConfiguration.class)
class FeeManagementControllerTest extends AbstractControllerTest {

  @MockBean
  FeePeriodDisplayService displayService;

  @MockBean
  FeePeriodValidator validator;

  @MockBean
  ControllerHelperService helperService;

  @MockBean
  FeePeriodService feePeriodService;

  AuthenticatedUserAccount userAccount;

  @BeforeEach
  void setup() {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()),
        EnumSet.of(PwaUserPrivilege.PWA_ACCESS, PwaUserPrivilege.PWA_MANAGER));

    var detailList = new ArrayList<DisplayableFeePeriodDetail>();
    var displayableDetail = new DisplayableFeePeriodDetail();
    displayableDetail.setFeePeriodId(1000);
    displayableDetail.setDescription("Test Fee Period");
    displayableDetail.setPeriodStartTimestamp(Instant.now());
    detailList.add(displayableDetail);

    when(displayService.listAllPeriods()).thenReturn(detailList);
    when(displayService.findPeriodById(any())).thenReturn(Optional.of(displayableDetail));
  }

  @Test
  void feeManagementController_authenticatedTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderFeeManagementOverview(userAccount)))
            .with(user(userAccount))).andExpect(status().isOk());
  }

  @Test
  void feeManagementController_unauthenticatedTest() throws Exception {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()), Set.of());

    mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderFeeManagementOverview(userAccount)))
        .with(user(userAccount))).andExpect(status().isForbidden());
  }

  @Test
  void renderOverview_smokeTest() throws Exception {
    var mvc = mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderFeeManagementOverview(userAccount)))
        .with(user(userAccount)))
        .andReturn()
        .getModelAndView()
        .getModel();

    var feePeriods = ((ArrayList<DisplayableFeePeriodDetail>) mvc.get("feePeriods"));
    assertThat(feePeriods.get(0).getDescription()).isEqualTo("Test Fee Period");
    assertThat(mvc)
        .containsEntry("newPeriodUrl", "/fee-management/new")
        .containsEntry("editPeriodUrl", "/fee-management/edit/");
  }

  @Test
  void renderPeriodDetail_smokeTest() throws Exception {
    var mvc = mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderFeePeriodDetail(userAccount, 1000)))
        .with(user(userAccount)))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc).containsEntry("backUrl", "/fee-management");
  }

  @Test
  void renderNewPeriodForm_smokeTest() throws Exception {
    var mvc = mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderNewPeriodForm(userAccount, new FeePeriodForm())))
        .with(user(userAccount)))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc)
        .containsEntry("applicationTypes", PwaApplicationType.values())
        .containsEntry("applicationFeeTypes", PwaApplicationFeeType.values());
  }

  @Test
  void renderNewPeriodForm_NotAllowedPendingPeriodExists() throws Exception {
    when(feePeriodService.pendingPeriodExists()).thenReturn(true);

    var mvc = mockMvc.perform(get(ReverseRouter.route(
            on(FeeManagementController.class)
                .renderNewPeriodForm(userAccount, new FeePeriodForm())))
            .with(user(userAccount)))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void renderEditPeriodForm_smokeTest() throws Exception {
    var mvc = mockMvc.perform(get(ReverseRouter.route(
        on(FeeManagementController.class)
            .renderEditPeriodForm(userAccount, 1000, new FeePeriodForm())))
        .with(user(userAccount)))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc)
        .containsEntry("applicationTypes", PwaApplicationType.values())
        .containsEntry("applicationFeeTypes", PwaApplicationFeeType.values());
  }
}
