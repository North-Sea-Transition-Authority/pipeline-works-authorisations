package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentCreationServiceTest {

  @Mock
  private ApplicationContext springApplicationContext;

  @Mock
  private TemplateRenderingService templateRenderingService;

  @Mock
  private PdfRenderingService pdfRenderingService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private MailMergeService mailMergeService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Captor
  private ArgumentCaptor<Map<String, Object>> modelMapCaptor;

  private DocumentCreationService documentCreationService;

  private PwaApplicationDetail pwaApplicationDetail;

  private DocumentInstance documentInstance;
  private DocumentView documentView;

  private DocgenRun docgenRun;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        1,
        1);

    documentCreationService = new DocumentCreationService(
        springApplicationContext,
        templateRenderingService,
        pdfRenderingService,
        documentInstanceService,
        mailMergeService,
        pwaApplicationDetailService,
        pwaConsentService);

    documentInstance = new DocumentInstance();
    documentInstance.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    documentView = new DocumentView(PwaDocumentType.INSTANCE, pwaApplicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    when(documentInstanceService.getDocumentView(any(), any())).thenReturn(documentView);

    when(pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplicationDetail.getPwaApplication())).thenReturn(Optional.of(pwaApplicationDetail));

  }

  @Test
  public void generateConsentDocument_allDocSectionsProcessed_preview() {

    testAndAssertGeneration(DocGenType.PREVIEW, true, pwaApplicationDetail.getPwaApplicationRef());

  }

  @Test
  public void generateConsentDocument_full_consentRefPresent_noWatermark() {

    var consent = new PwaConsent();
    consent.setReference("consent/reference");

    when(pwaConsentService.getConsentByPwaApplication(pwaApplicationDetail.getPwaApplication())).thenReturn(Optional.of(consent));

    testAndAssertGeneration(DocGenType.FULL, false, consent.getReference());

  }

  private void testAndAssertGeneration(DocGenType docGenType, boolean watermarkShown, String expectedReference) {

    docgenRun = new DocgenRun(documentInstance, docGenType, DocgenRunStatus.PENDING);
    var person = PersonTestUtil.createDefaultPerson();
    docgenRun.setScheduledByPerson(person);

    var documentSectionGenerator = mock(DocumentSectionGenerator.class);
    when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType))
        .thenReturn(new DocumentSectionData("TEMPLATE", Map.of("test", "test")));

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> documentSectionGenerator);

    documentCreationService.createConsentDocument(docgenRun);

    var docSpec = pwaApplicationDetail.getPwaApplicationType().getConsentDocumentSpec();

    Map<SectionType, Long> sectionTypeToCountMap = docSpec.getDocumentSectionDisplayOrderMap().keySet().stream()
        .map(DocumentSection::getSectionType)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    int numberOfCustomSections = sectionTypeToCountMap.get(SectionType.CUSTOM).intValue();
    int numberOfOpeningParagraphSections = sectionTypeToCountMap.get(SectionType.OPENING_PARAGRAPH).intValue();
    int numberOfClauseSections = sectionTypeToCountMap.get(SectionType.CLAUSE_LIST).intValue();

    verify(documentSectionGenerator, times(numberOfCustomSections + numberOfOpeningParagraphSections))
        .getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType);
    verify(documentInstanceService, times(numberOfClauseSections)).getDocumentView(eq(documentInstance), any());
    verify(mailMergeService, times(numberOfClauseSections)).mailMerge(documentView, docGenType);

    verify(templateRenderingService, times(1)).render(eq("documents/consents/consentDocument.ftl"), modelMapCaptor.capture(), eq(false));

    assertThat(modelMapCaptor.getValue()).containsAllEntriesOf(Map.of(
        "showWatermark", watermarkShown,
        "consentRef", expectedReference,
        "issueDate", DateUtils.formatDate(LocalDate.now())
    ));

  }

}
