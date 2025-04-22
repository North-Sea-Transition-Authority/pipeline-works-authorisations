package uk.co.ogauthority.pwa.controller.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.documents.clauses.ClauseFormValidator;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.generic.GenericBreadcrumbService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(DocumentTemplateController.class)
@ContextConfiguration(classes = DocumentTemplateController.class)
@WithDefaultPageControllerAdvice
class DocumentTemplateControllerTest extends ResolverAbstractControllerTest {

  @MockBean
  private DocumentTemplateService documentTemplateService;

  @MockBean
  private MailMergeService mailMergeService;

  @SpyBean
  private GenericBreadcrumbService genericBreadcrumbService;

  @SpyBean
  private ClauseFormValidator clauseFormValidator;

  private AuthenticatedUserAccount templateClauseManager, caseOfficer;

  private DocumentView documentView;

  @BeforeEach
  void setUp() {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_ACCESS));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(2), List.of(PwaUserPrivilege.PWA_ACCESS));

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    documentView = new DocumentView(PwaDocumentType.TEMPLATE, docSource, docSource.getDocumentTemplateMnem());

    when(documentTemplateService.getDocumentView(any())).thenReturn(documentView);

    var clause = new DocumentTemplateSectionClauseVersion();
    when(documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(any())).thenReturn(clause);

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(templateClauseManager, Map.of(TeamType.REGULATOR, Set.of(Role.TEMPLATE_CLAUSE_MANAGER))))
        .thenReturn(true);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.REGULATOR), anySet());

  }

  @Test
  void getTemplatesForSelect_hasPriv() {

    DocumentSpec.stream().forEach(documentSpec -> {

      try {

        mockMvc.perform(get("/document-templates/" + documentSpec.name())
            .with(user(templateClauseManager)))
            .andExpect(status().isOk());

      } catch (Exception e) {
        throw new AssertionError(e);
      }

    });



  }

  @Test
  void getTemplatesForSelect_doesntHavePriv() throws Exception {

    DocumentSpec.stream().forEach(documentSpec -> {

      try {

        mockMvc.perform(get("/document-templates/" + documentSpec.name())
            .with(user(caseOfficer)))
            .andExpect(status().isForbidden());

      } catch (Exception e) {
        throw new AssertionError(e);
      }

    });

  }

  @Test
  void renderAddClauseAfter_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseAfter(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderAddClauseAfter_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseAfter(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postAddClauseAfter_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseAfter(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1,null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addClauseAfter(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void postAddClauseAfter_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseAfter(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1,null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addClauseAfter(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void renderAddClauseBefore_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseBefore(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderAddClauseBefore_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseBefore(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postAddClauseBefore_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseBefore(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addClauseBefore(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void postAddClauseBefore_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseBefore(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addClauseBefore(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void renderAddSubClauseFor_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddSubClauseFor(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderAddSubClauseFor_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddSubClauseFor(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postAddSubClauseFor_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddSubClauseFor(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addSubClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void postAddSubClauseFor_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddSubClauseFor(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addSubClause(any(), any(), any());

  }

  @Test
  void renderEditClause_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderEditClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderEditClause_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderEditClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postEditClause_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postEditClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).editClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void postEditClause_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postEditClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(user(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).editClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  void renderRemoveClause_correctPermission() throws Exception {

    var docView = mock(DocumentView.class);
    when(documentTemplateService.getDocumentView(any())).thenReturn(docView);

    var sectionView = new SectionClauseVersionView(1, 1, "a", "a", null, null, null);
    when(docView.getSectionClauseView(1)).thenReturn(sectionView);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderRemoveClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null)))
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderRemoveClause_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderRemoveClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null)))
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postRemoveClause_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postRemoveClause(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, 1, null, null)))
        .with(user(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).removeClause(any(), eq(templateClauseManager.getLinkedPerson()));

  }

}
