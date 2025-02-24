package uk.co.ogauthority.pwa.controller.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.documents.clauses.ClauseFormValidator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = DocumentInstanceController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class DocumentInstanceControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private DocumentInstanceService documentInstanceService;

  @MockBean
  private MailMergeService mailMergeService;

  @SpyBean
  private ClauseFormValidator clauseFormValidator;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    var clause = new DocumentInstanceSectionClauseVersion();
    when(documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(any())).thenReturn(clause);

  }

  @Test
  void renderAddClauseAfter_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddClauseAfter_statusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void postAddClauseAfter_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddClauseAfter_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseAfter(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddClauseAfter_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addClauseAfter(any(), any(), eq(user.getLinkedPerson()));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postAddClauseAfter_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseAfter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addClauseAfter(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  void renderAddClauseBefore_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddClauseBefore_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postAddClauseBefore_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddClauseBefore_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddClauseBefore(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddClauseBefore_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addClauseBefore(any(), any(), eq(user.getLinkedPerson()));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postAddClauseBefore_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddClauseBefore(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addClauseBefore(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  void renderAddSubClauseFor_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddSubClauseFor_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postAddSubClauseFor_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddSubClauseFor_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postAddSubClauseFor(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddSubClauseFor_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).addSubClause(any(), any(), eq(user.getLinkedPerson()));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postAddSubClauseFor_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postAddSubClauseFor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).addSubClause(any(), any(), eq(user.getLinkedPerson()));

  }

  @Test
  void renderEditClause_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderEditClause_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postEditClause_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditClause_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postEditClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .addRequestParam("name", "name")
        .addRequestParam("text", "text");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postEditClause_success() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentInstanceService, times(1)).editClause(any(), any(), eq(user.getLinkedPerson()));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("name", "name")
            .param("text", "text"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postEditClause_validationFail() throws Exception {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentInstanceService, times(0)).editClause(any(), any(), eq(user.getLinkedPerson()));

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.HYDROGEN_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceController.class).postEditClause(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, DocumentTemplateMnem.CCUS_CONSENT_DOCUMENT, 1, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

  }

  @Test
  void renderRemoveClause_statusSmokeTest() {

    var docView = mock(DocumentView.class);
    when(documentInstanceService.getDocumentView(any())).thenReturn(docView);

    var sectionView = new SectionClauseVersionView(1, 1, "a", "a", null, null, null);
    when(docView.getSectionClauseView(1)).thenReturn(sectionView);

    var clauseVersion = new DocumentInstanceSectionClauseVersion();
    var clause = new DocumentInstanceSectionClause();
    clause.setDocumentInstance(new DocumentInstance());
    clauseVersion.setClause(clause);

    when(documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(1)).thenReturn(clauseVersion);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderRemoveClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1,
                    null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderRemoveClause_permissionSmokeTest() {

    var docView = mock(DocumentView.class);
    when(documentInstanceService.getDocumentView(any())).thenReturn(docView);

    var sectionView = new SectionClauseVersionView(1, 1, "a", "a", null, null, null);
    when(docView.getSectionClauseView(1)).thenReturn(sectionView);

    var clauseVersion = new DocumentInstanceSectionClauseVersion();
    var clause = new DocumentInstanceSectionClause();
    clause.setDocumentInstance(new DocumentInstance());
    clauseVersion.setClause(clause);

    when(documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(1)).thenReturn(clauseVersion);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .renderRemoveClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1,
                    null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postRemoveClause_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postRemoveClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postRemoveClause_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DocumentInstanceController.class)
                .postRemoveClause(applicationDetail.getMasterPwaApplicationId(), type, null, DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, 1, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

}
