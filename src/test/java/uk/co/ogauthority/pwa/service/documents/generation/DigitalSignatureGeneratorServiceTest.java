package uk.co.ogauthority.pwa.service.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;

@RunWith(MockitoJUnitRunner.class)
public class DigitalSignatureGeneratorServiceTest {

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private MailMergeService mailMergeService;

  @InjectMocks
  private DigitalSignatureGeneratorService underTest;

  @Test
  public void getDocumentSectionData_callsMailMergeAndReturnsExpectedData() {
    // Arrange
    PwaApplicationDetail pwaApplicationDetail = new PwaApplicationDetail();
    DocumentInstance documentInstance = new DocumentInstance();
    DocGenType docGenType = DocGenType.FULL;
    DocumentView docView = mock(DocumentView.class);
    DocumentSection documentSection = DocumentSection.DIGITAL_SIGNATURE;

    when(documentInstanceService.getDocumentView(documentInstance, documentSection))
        .thenReturn(docView);

    // Act
    DocumentSectionData result = underTest.getDocumentSectionData(
        pwaApplicationDetail, documentInstance, docGenType
    );

    // Assert
    verify(documentInstanceService).getDocumentView(documentInstance, documentSection);
    verify(mailMergeService).mailMerge(docView, docGenType);

    assertThat(result.getTemplatePath())
        .isEqualTo("documents/consents/sections/digitalSignature.ftl");
    assertThat(result.getTemplateModel())
        .containsEntry("docView", docView)
        .hasSize(1); // Only one key in the map
  }
}