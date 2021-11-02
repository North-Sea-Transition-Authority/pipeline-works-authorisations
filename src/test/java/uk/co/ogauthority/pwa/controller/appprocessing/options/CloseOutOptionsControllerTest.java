package uk.co.ogauthority.pwa.controller.appprocessing.options;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionView;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.CloseOutOptionsTaskService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CloseOutOptionsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class CloseOutOptionsControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final int APP_ID = 5;
  private static final int APP_DETAIL_ID = 50;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.OPTIONS_VARIATION;

  @MockBean
  private CloseOutOptionsTaskService closeOutOptionsTaskService;

  @MockBean
  private ApproveOptionsService approveOptionsService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private PadConfirmationOfOptionService padConfirmationOfOptionService;

  @Mock
  private PadConfirmationOfOptionView padConfirmationOfOptionView;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private Person person;


  @Before
  public void setUp() throws Exception {

    person = PersonTestUtil.createPersonFrom(new PersonId(1));
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, person),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.OPTIONS_VARIATION,
        APP_ID,
        APP_DETAIL_ID
    );

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(new ProcessingPermissionsDto(null, EnumSet.of(PwaAppProcessingPermission.CLOSE_OUT_OPTIONS)));

    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(closeOutOptionsTaskService.taskAccessible(any())).thenReturn(true);

    when(padConfirmationOfOptionService.getPadConfirmationOfOptionView(any())).thenReturn(padConfirmationOfOptionView);

    endpointTester = new PwaApplicationEndpointTestBuilder(
        mockMvc,
        pwaApplicationDetailService,
        pwaAppProcessingPermissionService
    )
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CLOSE_OUT_OPTIONS);

  }

  @Test
  public void renderCloseOutOptions_whenNotCloseable() throws Exception {
    when(closeOutOptionsTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CloseOutOptionsController.class)
        .renderCloseOutOptions(APP_ID, APP_TYPE, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderCloseOutOptions_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CloseOutOptionsController.class)
                .renderCloseOutOptions(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void renderCloseOutOptions_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CloseOutOptionsController.class)
                .renderCloseOutOptions(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void closeOutOptions_whenNotCloseable() throws Exception {
    when(closeOutOptionsTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(CloseOutOptionsController.class)
        .closeOutOptions(APP_ID, APP_TYPE, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isForbidden());

  }

  @Test
  public void closeOutOptions_whenCloseable() throws Exception {
    when(closeOutOptionsTaskService.taskAccessible(any())).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(CloseOutOptionsController.class)
        .closeOutOptions(APP_ID, APP_TYPE, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection());

    verify(approveOptionsService, times(1)).closeOutOptions(pwaApplicationDetail, user);

  }

  @Test
  public void closeOutOptions_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CloseOutOptionsController.class)
                .closeOutOptions(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void closeOutOptions_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CloseOutOptionsController.class)
                .closeOutOptions(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }
}