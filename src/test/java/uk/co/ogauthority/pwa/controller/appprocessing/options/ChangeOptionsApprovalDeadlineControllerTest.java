package uk.co.ogauthority.pwa.controller.appprocessing.options;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ChangeOptionsApprovalDeadlineForm;
import uk.co.ogauthority.pwa.model.view.appprocessing.options.OptionsApprovalDeadlineView;
import uk.co.ogauthority.pwa.model.view.appprocessing.options.OptionsApprovalDeadlineViewTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ChangeOptionsApprovalDeadlineTaskService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.appprocessing.options.ChangeOptionsApprovalDeadlineFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ChangeOptionsApprovalDeadlineController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ChangeOptionsApprovalDeadlineControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final String DEADLINE_DAY_ATTR = "deadlineDateDay";
  private static final String DEADLINE_MONTH_ATTR = "deadlineDateMonth";
  private static final String DEADLINE_YEAR_ATTR = "deadlineDateYear";
  private static final String NOTE_ATTR = "note";

  private static final int APP_ID = 5;
  private static final int APP_DETAIL_ID = 50;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.OPTIONS_VARIATION;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private ApproveOptionsService approveOptionsService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ChangeOptionsApprovalDeadlineTaskService changeOptionsApprovalDeadlineTaskService;

  @MockBean
  private ChangeOptionsApprovalDeadlineFormValidator changeOptionsApprovalDeadlineFormValidator;

  private PwaApplicationDetail pwaApplicationDetail;
  private OptionsApprovalDeadlineView optionsApprovalDeadlineView;

  private AuthenticatedUserAccount user;
  private Person person;

  @Before
  public void setUp() {


    person = PersonTestUtil.createPersonFrom(new PersonId(1));
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, person),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION,
        APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    optionsApprovalDeadlineView = OptionsApprovalDeadlineViewTestUtil.createWithDeadline(LocalDate.of(2020,12,1));
    when(approveOptionsService.getOptionsApprovalDeadlineViewOrError(any())).thenReturn(optionsApprovalDeadlineView);

  when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(new ProcessingPermissionsDto(null, EnumSet.of(PwaAppProcessingPermission.CHANGE_OPTIONS_APPROVAL_DEADLINE)));

    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(true);

    endpointTester = new PwaApplicationEndpointTestBuilder(
        mockMvc,
        pwaApplicationDetailService,
        pwaAppProcessingPermissionService
    )
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CHANGE_OPTIONS_APPROVAL_DEADLINE);

  }

  @Test
  public void renderChangeDeadline_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
                .renderChangeDeadline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void renderChangeDeadline_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
                .renderChangeDeadline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }


  @Test
  public void approveOptions_appStatusSmokeTest() {
    mockValidationFail();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
                .changeOptionsApprovalDeadline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void approveOptions_processingPermissionSmokeTest() {
    mockValidationFail();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
                .changeOptionsApprovalDeadline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void approveOptions_whenTaskNotAccessible() throws Exception {
    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
        .changeOptionsApprovalDeadline(APP_ID, APP_TYPE, null, null, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderChangeDeadline_whenTaskNotAccessible() throws Exception {
    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
        .renderChangeDeadline(APP_ID, APP_TYPE, null, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void approveOptions_whenTaskAccessible_andfailsValidation() throws Exception {
    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(true);
    mockValidationFail();

    mockMvc.perform(post(ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
        .changeOptionsApprovalDeadline(APP_ID, APP_TYPE, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param(DEADLINE_DAY_ATTR, "1")
        .with(csrf())
    )
        .andExpect(status().isOk());

    verify(approveOptionsService, times(0)).approveOptions(any(), any(), any());
  }

  @Test
  public void approveOptions_whenTaskAccessible_andPassesValidation() throws Exception {
    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(true);

    var note = "some note";
    mockMvc.perform(post(ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
        .changeOptionsApprovalDeadline(APP_ID, APP_TYPE, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param(DEADLINE_DAY_ATTR, "1")
        .param(DEADLINE_MONTH_ATTR, "12")
        .param(DEADLINE_YEAR_ATTR, "2020")
        .param(NOTE_ATTR, note)
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection());

    var expectedDeadlineInstant = LocalDate.of(2020, 12, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    verify(approveOptionsService, times(1)).changeOptionsApprovalDeadline(
        pwaApplicationDetail,
        person,
        expectedDeadlineInstant,
        note
    );
  }

  @Test
  public void renderChangeDeadline_whenTaskAccessible_mapsCurrentDeadlineDateToForm() throws Exception {
    when(changeOptionsApprovalDeadlineTaskService.taskAccessible(any())).thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
        .renderChangeDeadline(APP_ID, APP_TYPE, null, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();


    var form = (ChangeOptionsApprovalDeadlineForm) modelAndView.getModel().get("form");
    assertThat(form.getDeadlineDateDay()).isEqualTo(optionsApprovalDeadlineView.getDeadlineLocalDate().getDayOfMonth());
    assertThat(form.getDeadlineDateMonth()).isEqualTo(optionsApprovalDeadlineView.getDeadlineLocalDate().getMonthValue());
    assertThat(form.getDeadlineDateYear()).isEqualTo(optionsApprovalDeadlineView.getDeadlineLocalDate().getYear());

  }

  private void mockValidationFail() {
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue(DEADLINE_DAY_ATTR, REQUIRED.errorCode(DEADLINE_DAY_ATTR), "error message");
      return errors;
    })
        .when(changeOptionsApprovalDeadlineFormValidator).validate(any(), any());
  }
}