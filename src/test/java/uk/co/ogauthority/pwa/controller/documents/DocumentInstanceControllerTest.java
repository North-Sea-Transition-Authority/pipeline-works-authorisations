package uk.co.ogauthority.pwa.controller.documents;

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
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.documents.clauses.ClauseFormValidator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DocumentInstanceController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class DocumentInstanceControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private DocumentInstanceService documentInstanceService;

  @SpyBean
  private ClauseFormValidator clauseFormValidator;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    var clause = new DocumentInstanceSectionClauseVersion();
    when(documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(any())).thenReturn(clause);

  }

  @Test
  public void renderAddClauseAfter_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddClauseAfter_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddClauseAfter_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddClauseAfter_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddClauseAfter_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addClauseAfter(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void postAddClauseAfter_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name"))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addClauseAfter(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void renderAddClauseBefore_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddClauseBefore_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddClauseBefore_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddClauseBefore_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddClauseBefore_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addClauseBefore(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void postAddClauseBefore_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name"))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addClauseBefore(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void renderAddSubClauseFor_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddSubClauseFor_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddSubClauseFor_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddSubClauseFor_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddSubClauseFor_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addSubClause(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void postAddSubClauseFor_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name"))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addSubClause(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void renderEditClause_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderEditClause_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void postEditClause_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditClause_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postEditClause_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).editClause(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  public void postEditClause_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("name", "name"))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).editClause(any(), any(), eq(user.getLinkedPerson()));

  }

}
