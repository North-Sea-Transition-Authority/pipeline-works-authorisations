package uk.co.ogauthority.pwa.controller.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
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

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentTemplateController.class)
@Import(PwaMvcTestConfiguration.class)
public class DocumentTemplateControllerTest extends AbstractControllerTest {

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

  @Before
  public void setUp() throws Exception {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_CASE_OFFICER));

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    documentView = new DocumentView(PwaDocumentType.TEMPLATE, docSource, docSource.getDocumentTemplateMnem());

    when(documentTemplateService.getDocumentView(any())).thenReturn(documentView);

    var clause = new DocumentTemplateSectionClauseVersion();
    when(documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(any())).thenReturn(clause);

  }

  @Test
  public void getTemplatesForSelect_hasPriv() {

    DocumentSpec.stream().forEach(documentSpec -> {

      try {

        mockMvc.perform(get("/document-templates/" + documentSpec.name())
            .with(authenticatedUserAndSession(templateClauseManager)))
            .andExpect(status().isOk());

      } catch (Exception e) {
        throw new AssertionError(e);
      }

    });



  }

  @Test
  public void getTemplatesForSelect_doesntHavePriv() throws Exception {

    DocumentSpec.stream().forEach(documentSpec -> {

      try {

        mockMvc.perform(get("/document-templates/" + documentSpec.name())
            .with(authenticatedUserAndSession(caseOfficer)))
            .andExpect(status().isForbidden());

      } catch (Exception e) {
        throw new AssertionError(e);
      }

    });

  }

  @Test
  public void renderAddClauseAfter_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseAfter(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderAddClauseAfter_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseAfter(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postAddClauseAfter_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseAfter(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1,null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addClauseAfter(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void postAddClauseAfter_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseAfter(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1,null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addClauseAfter(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void renderAddClauseBefore_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseBefore(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderAddClauseBefore_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseBefore(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postAddClauseBefore_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseBefore(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addClauseBefore(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void postAddClauseBefore_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddClauseBefore(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addClauseBefore(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void renderAddSubClauseFor_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddSubClauseFor(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderAddSubClauseFor_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddSubClauseFor(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postAddSubClauseFor_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddSubClauseFor(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).addSubClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void postAddSubClauseFor_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postAddSubClauseFor(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).addSubClause(any(), any(), any());

  }

  @Test
  public void renderEditClause_correctPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderEditClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderEditClause_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderEditClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postEditClause_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postEditClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).editClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void postEditClause_validationFail() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postEditClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(documentTemplateService, times(0)).editClause(any(), any(), eq(templateClauseManager.getLinkedPerson()));

  }

  @Test
  public void renderRemoveClause_correctPermission() throws Exception {

    var docView = mock(DocumentView.class);
    when(documentTemplateService.getDocumentView(any())).thenReturn(docView);

    var sectionView = new SectionClauseVersionView(1, 1, "a", "a", null, null, null);
    when(docView.getSectionClauseView(1)).thenReturn(sectionView);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderRemoveClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null)))
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderRemoveClause_wrongPermission() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
        .renderRemoveClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null)))
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postRemoveClause_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateController.class)
        .postRemoveClause(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, 1, null, null)))
        .with(authenticatedUserAndSession(templateClauseManager))
        .with(csrf())
        .param("name", "name")
        .param("text", "text"))
        .andExpect(status().is3xxRedirection());

    verify(documentTemplateService, times(1)).removeClause(any(), eq(templateClauseManager.getLinkedPerson()));

  }

}