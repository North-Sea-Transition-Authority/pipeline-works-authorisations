package uk.co.ogauthority.pwa.controller.pwaapplications.options;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.OptionsTemplateForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.OptionsTemplateService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OptionsTemplateController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class OptionsTemplateControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private OptionsTemplateService optionsTemplateService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

  }

  @Test
  public void renderOptionsTemplate_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .renderOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderOptionsTemplate_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .renderOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderOptionsTemplate_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .renderOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postOptionsTemplate_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(optionsTemplateService, new OptionsTemplateForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .postOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void postOptionsTemplate_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(optionsTemplateService, new OptionsTemplateForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .postOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null,null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postOptionsTemplate_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(optionsTemplateService, new OptionsTemplateForm(), ValidationType.FULL);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsTemplateController.class)
                .postOptionsTemplate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postOptionsTemplate_passValidation() throws Exception {

    ControllerTestUtils.passValidationWhenPost(optionsTemplateService, new OptionsTemplateForm(), ValidationType.FULL);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    pwaApplicationDetail.getPwaApplication().setId(1);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(pwaApplicationDetail);

    when(pwaApplicationPermissionService.getPermissions(pwaApplicationDetail, user.getLinkedPerson())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(OptionsTemplateController.class)
        .postOptionsTemplate(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            null,
            null,
            null,
            null,
            ValidationType.FULL)))
        .with(authenticatedUserAndSession(user))
        .param(ValidationType.FULL.getButtonText(), "")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padFileService, times(1)).updateFiles(
        any(),
        eq(pwaApplicationDetail),
        eq(ApplicationDetailFilePurpose.OPTIONS_TEMPLATE),
        eq(FileUpdateMode.DELETE_UNLINKED_FILES),
        any());

  }

  @Test
  public void postOptionsTemplate_failValidation() throws Exception {

    ControllerTestUtils.failValidationWhenPost(optionsTemplateService, new OptionsTemplateForm(), ValidationType.FULL);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    pwaApplicationDetail.getPwaApplication().setId(1);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(pwaApplicationDetail);

    when(pwaApplicationPermissionService.getPermissions(pwaApplicationDetail, user.getLinkedPerson())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(OptionsTemplateController.class)
        .postOptionsTemplate(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            null,
            null,
            null,
            null,
            ValidationType.FULL)))
        .with(authenticatedUserAndSession(user))
        .param(ValidationType.FULL.getButtonText(), "")
        .with(csrf()))
        .andExpect(status().isOk());

    verify(padFileService, times(0)).updateFiles(
        any(),
        any(),
        any(),
        any(),
        any());

  }

}
