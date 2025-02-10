package uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationDeleteService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DeleteApplicationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class DeleteApplicationControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PwaApplicationDeleteService pwaApplicationDeleteService;

  @SpyBean
  private ApplicationBreadcrumbService breadcrumbService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private Person person;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() throws Exception {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    person = new Person(100, "test", "person", "email", "telephone");
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, person),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.DRAFT);

    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);

  }

  @Test
  public void renderDeleteApplication_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DeleteApplicationController.class)
                .renderDeleteApplication(type, applicationDetail.getMasterPwaApplicationId(),null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderDeleteApplication_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DeleteApplicationController.class)
                .renderDeleteApplication(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }


  @Test
  public void postDeleteApplication_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DeleteApplicationController.class)
                .postDeleteApplication(type, applicationDetail.getMasterPwaApplicationId(), null, null, Optional.empty())));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postDeleteApplication_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DeleteApplicationController.class)
                .postDeleteApplication(type, applicationDetail.getMasterPwaApplicationId(), null, null, Optional.empty())));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void deleteApplication_success() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), eq(person))).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    mockMvc.perform(
            post(ReverseRouter.route(on(DeleteApplicationController.class)
                .postDeleteApplication(pwaApplicationDetail.getPwaApplicationType(),
                    pwaApplicationDetail.getMasterPwaApplicationId(), null, null, Optional.empty())
            ))
                .with(user(user))
                .with(csrf())

        ).andExpect(status().is3xxRedirection());

    verify(pwaApplicationDeleteService, times(1)).deleteApplication(user, pwaApplicationDetail);
    verify(analyticsService, times(1)).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.APPLICATION_DELETED));

  }

}
