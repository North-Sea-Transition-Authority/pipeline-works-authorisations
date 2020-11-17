package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentGenerationService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationServiceTest {

  @Mock
  private ApplicationContext springApplicationContext;

  @Mock
  private TemplateRenderingService templateRenderingService;

  @Mock
  private PdfRenderingService pdfRenderingService;

  private DocumentGenerationService documentGenerationService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        1,
        1);

    documentGenerationService = new DocumentGenerationService(springApplicationContext, templateRenderingService, pdfRenderingService);

  }

  @Test
  public void generateConsentDocument_allDocSectionsProcessed() {

    var documentSectionGenerator = mock(DocumentSectionGenerator.class);
    when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail)).thenReturn(new DocumentSectionData("TEMPLATE", Map.of("test", "test")));

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> documentSectionGenerator);

    documentGenerationService.generateConsentDocument(pwaApplicationDetail, DocGenType.PREVIEW);

    var docSpec = pwaApplicationDetail.getPwaApplicationType().getConsentDocumentSpec();
    var numberOfSections = docSpec.getDocumentSectionDisplayOrderMap().size();

    verify(documentSectionGenerator, times(numberOfSections)).getDocumentSectionData(pwaApplicationDetail);

  }

}
