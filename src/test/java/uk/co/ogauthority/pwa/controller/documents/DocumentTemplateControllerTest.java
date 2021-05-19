package uk.co.ogauthority.pwa.controller.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.generic.GenericBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentTemplateController.class)
public class DocumentTemplateControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  private DocumentTemplateService documentTemplateService;

  @SpyBean
  private GenericBreadcrumbService genericBreadcrumbService;

  private AuthenticatedUserAccount templateClauseManager, caseOfficer;

  private DocumentView documentView;

  @Before
  public void setUp() throws Exception {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_CASE_OFFICER));

    var docSource = new TemplateDocumentSource(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    documentView = new DocumentView(PwaDocumentType.TEMPLATE, docSource, docSource.getDocumentTemplateMnem());

    when(documentTemplateService.getDocumentView(any())).thenReturn(documentView);

  }

  @Test
  public void getTemplatesForSelect_hasPriv() {

    DocumentSpec.stream().forEach(documentSpec -> {

      try {

        mockMvc.perform(get("/document-templates/" + documentSpec.name())
            .with(authenticatedUserAndSession(templateClauseManager)))
            .andExpect(status().isOk());

      } catch (Exception e) {
        assertThat(false).isTrue();
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
        assertThat(false).isTrue();
      }

    });

  }


}