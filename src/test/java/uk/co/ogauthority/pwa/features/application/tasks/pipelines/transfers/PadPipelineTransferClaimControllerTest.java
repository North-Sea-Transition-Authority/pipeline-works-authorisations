package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PadPipelineTransferClaimController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PadPipelineTransferClaimControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;

  @MockBean
  ControllerHelperService controllerHelperService;

  @MockBean
  PadPipelineTransferService padPipelineTransferService;

  @SpyBean
  ApplicationBreadcrumbService applicationBreadcrumbService;

  AuthenticatedUserAccount user;

  PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
  }

  @Test
  public void renderClaimPipelineForm() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PadPipelineTransferClaimController.class)
        .renderClaimPipelineForm(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void submitClaimPipelineForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PadPipelineTransferClaimController.class)
            .submitClaimPipelineForm(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }
}
