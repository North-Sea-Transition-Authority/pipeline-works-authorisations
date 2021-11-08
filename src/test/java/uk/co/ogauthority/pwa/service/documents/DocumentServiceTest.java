package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  private DocumentService documentService;

  @Before
  public void setUp() {

    documentService = new DocumentService(documentTemplateService, documentInstanceService);

  }

  @Test
  public void createDocumentInstance_noExistingDocInstance() {

    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.INITIAL);
    var person = new Person();

    var docDto = new DocumentTemplateDto();

    when(documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, PwaApplicationType.INITIAL.getConsentDocumentSpec())).thenReturn(docDto);

    documentService.createDocumentInstance(app, person);

    verify(documentTemplateService, times(1)).populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, PwaApplicationType.INITIAL.getConsentDocumentSpec());

    verify(documentInstanceService, times(1)).createFromDocumentDto(app, docDto, person);

  }

  @Test
  public void reloadDocumentInstance_docInstanceExists() {

    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.INITIAL);

    documentService.reloadDocumentInstance(app, new Person());

    verify(documentInstanceService, times(1)).clearClauses(app, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

  }

  @Test
  public void documentInstanceExists_true() {

    when(documentInstanceService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    assertThat(documentService.getDocumentInstance(new PwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)).isPresent();

  }

  @Test
  public void documentInstanceExists_false() {

    when(documentInstanceService.getDocumentInstance(any(), any())).thenReturn(Optional.empty());

    assertThat(documentService.getDocumentInstance(new PwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)).isEmpty();

  }

}
