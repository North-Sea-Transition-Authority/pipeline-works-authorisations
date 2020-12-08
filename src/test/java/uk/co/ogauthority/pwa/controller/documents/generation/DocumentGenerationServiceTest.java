package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.exception.documents.DocumentInstanceException;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentGenerationService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
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

  @Mock
  private DocumentInstanceService documentInstanceService;

  private DocumentGenerationService documentGenerationService;

  private PwaApplicationDetail pwaApplicationDetail;

  private DocumentInstance documentInstance;
  private DocumentView documentView;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        1,
        1);

    documentGenerationService = new DocumentGenerationService(springApplicationContext, templateRenderingService, pdfRenderingService, documentInstanceService);

    documentInstance = new DocumentInstance();
    documentView = new DocumentView(PwaDocumentType.INSTANCE, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    when(documentInstanceService.getDocumentInstance(any(), any())).thenReturn(Optional.of(documentInstance));
    when(documentInstanceService.getDocumentView(any(), any())).thenReturn(documentView);

  }

  @Test
  public void generateConsentDocument_allDocSectionsProcessed() {

    var documentSectionGenerator = mock(DocumentSectionGenerator.class);
    when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail)).thenReturn(new DocumentSectionData("TEMPLATE", Map.of("test", "test")));

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> documentSectionGenerator);

    documentGenerationService.generateConsentDocument(pwaApplicationDetail, DocGenType.PREVIEW);

    var docSpec = pwaApplicationDetail.getPwaApplicationType().getConsentDocumentSpec();

    Map<SectionType, Long> sectionTypeToCountMap = docSpec.getDocumentSectionDisplayOrderMap().keySet().stream()
        .map(DocumentSection::getSectionType)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    int numberOfCustomSections = sectionTypeToCountMap.get(SectionType.CUSTOM).intValue();
    int numberOfClauseSections = sectionTypeToCountMap.get(SectionType.CLAUSE_LIST).intValue();

    verify(documentSectionGenerator, times(numberOfCustomSections)).getDocumentSectionData(pwaApplicationDetail);
    verify(documentInstanceService, times(numberOfClauseSections)).getDocumentView(eq(documentInstance), any());

  }

  @Test(expected = DocumentInstanceException.class)
  public void generateConsentDocument_noDocInstance() {

    when(documentInstanceService.getDocumentInstance(any(), any())).thenReturn(Optional.empty());
    documentGenerationService.generateConsentDocument(pwaApplicationDetail, DocGenType.PREVIEW);

  }

}
