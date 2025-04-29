package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.documents.generation.DocgenRunSectionData;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunSectionDataRepository;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.documents.signing.DocumentSigningService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class DocumentCreationServiceTest {

  public static final String DIV_CLASS_PAGE_BREAK = "<div class='page-break'/>";
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

  @Mock
  private DocgenRunSectionDataRepository docgenRunSectionDataRepository;

  @Mock
  private DocumentSigningService documentSigningService;

  @Captor
  private ArgumentCaptor<Map<String, Object>> modelMapCaptor;

  @Captor
  private ArgumentCaptor<List<DocgenRunSectionData>> docgenRunSectionDataCaptor;

  private DocumentCreationService documentCreationService;

  private PwaApplicationDetail pwaApplicationDetail;

  private DocumentInstance documentInstance;
  private DocumentView documentView;

  private DocgenRun docgenRun;

  @BeforeEach
  void setUp() {

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
        pwaConsentService,
        docgenRunSectionDataRepository,
        documentSigningService
    );

    documentInstance = new DocumentInstance();
    documentInstance.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    documentView = new DocumentView(PwaDocumentType.INSTANCE, pwaApplicationDetail.getPwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);

    when(documentInstanceService.getDocumentView(any(), any())).thenReturn(documentView);

    when(pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplicationDetail.getPwaApplication())).thenReturn(Optional.of(pwaApplicationDetail));

  }

  @Test
  void generateConsentDocument_allDocSectionsProcessed_preview() {

    testAndAssertGeneration(DocGenType.PREVIEW, true, pwaApplicationDetail.getPwaApplicationRef());

  }

  @Test
  void generateConsentDocument_errorWhileLogging_stillCompletes() {

    when(docgenRunSectionDataRepository.saveAll(any())).thenThrow(RuntimeException.class);

    testAndAssertGeneration(DocGenType.PREVIEW, true, pwaApplicationDetail.getPwaApplicationRef());

  }

  @Test
  void generateConsentDocument_full_consentRefPresent_noWatermark() {

    var consent = new PwaConsent();
    consent.setReference("consent/reference");

    when(pwaConsentService.getConsentByPwaApplication(pwaApplicationDetail.getPwaApplication())).thenReturn(Optional.of(consent));

    testAndAssertGeneration(DocGenType.FULL, false, consent.getReference());

  }

  private DocumentSectionGenerator defaultDocumentSectionGenerator(DocGenType docGenType) {
    var documentSectionGenerator = mock(DocumentSectionGenerator.class);
    when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType))
        .thenReturn(new DocumentSectionData("TEMPLATE", Map.of("test", "test")));
    return documentSectionGenerator;
  }

  private void testAndAssertGeneration(DocGenType docGenType, boolean watermarkShown, String expectedReference) {
    testAndAssertGeneration(docGenType, watermarkShown, expectedReference, this::defaultDocumentSectionGenerator);
  }

  private void testAndAssertGeneration(DocGenType docGenType, boolean watermarkShown, String expectedReference, Function<DocGenType, DocumentSectionGenerator> documentSectionGeneratorFunction) {

    docgenRun = new DocgenRun(documentInstance, docGenType, DocgenRunStatus.PENDING);
    var person = PersonTestUtil.createDefaultPerson();
    docgenRun.setScheduledByPerson(person);

    var documentSectionGenerator = documentSectionGeneratorFunction.apply(docGenType);

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> documentSectionGenerator);
    when(templateRenderingService.getRenderedTemplate("documents/consents/fragments/pageBreak.ftl", Map.of()))
        .thenReturn(DIV_CLASS_PAGE_BREAK);

    documentCreationService.createConsentDocument(docgenRun);

    var docSpec = DocumentSpec.getSpecForApplication(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getResourceType());

    Map<SectionType, Long> sectionTypeToCountMap = docSpec.getDocumentSectionDisplayOrderMap().keySet().stream()
        .map(DocumentSection::getSectionType)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    int numberOfCustomSections = sectionTypeToCountMap.get(SectionType.CUSTOM).intValue();
    int numberOfOpeningParagraphSections = sectionTypeToCountMap.get(SectionType.OPENING_PARAGRAPH).intValue();
    int numberOfClauseSections = sectionTypeToCountMap.get(SectionType.CLAUSE_LIST).intValue();
    int numberOfDigitalSignatureSections = sectionTypeToCountMap.get(SectionType.DIGITAL_SIGNATURE).intValue();

    verify(documentSectionGenerator, times(numberOfCustomSections + numberOfOpeningParagraphSections + numberOfDigitalSignatureSections))
        .getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType);
    verify(documentInstanceService, times(numberOfClauseSections)).getDocumentView(eq(documentInstance), any());
    verify(mailMergeService, times(numberOfClauseSections)).mailMerge(documentView, docGenType);

    verify(templateRenderingService).render(eq("documents/consents/consentDocument.ftl"), modelMapCaptor.capture(), eq(false));

    assertThat(modelMapCaptor.getValue()).containsAllEntriesOf(Map.of(
        "showWatermark", watermarkShown,
        "consentRef", expectedReference,
        "issueDate", DateUtils.formatDate(LocalDate.now())
    ));

    verify(docgenRunSectionDataRepository).saveAll(docgenRunSectionDataCaptor.capture());

    assertThat(docgenRunSectionDataCaptor.getValue()).hasSize(numberOfClauseSections + numberOfOpeningParagraphSections + numberOfCustomSections + numberOfDigitalSignatureSections);

  }

  @Test
  void nbspSuccessfulGeneration() {
    Map<String, Object> dataMap = Map.of("testing", "test\u00A0ing");
    when(templateRenderingService.render(anyString(), eq(dataMap), anyBoolean())).thenReturn("test\u00A0ing");

    testAndAssertGeneration(DocGenType.PREVIEW, true, pwaApplicationDetail.getPwaApplicationRef(), docGenType -> {
      var documentSectionGenerator = mock(DocumentSectionGenerator.class);
      when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType))
          .thenReturn(new DocumentSectionData("TEMPLATE", Map.of("testing", "test\u00A0ing")));
      return documentSectionGenerator;
    });

    var captor = ArgumentCaptor.forClass(Map.class);
    verify(templateRenderingService).render(eq("documents/consents/consentDocument.ftl"), captor.capture(), anyBoolean());

    var sectionHtml = (String) captor.getValue().get("sectionHtml");
    assertThat(sectionHtml).doesNotContain("\u00A0");
  }

  @Test
  public void pageBreakSuccessfulGeneration() {
    Map<String, Object> dataMap = Map.of("testing", "testing");
    when(templateRenderingService.render(anyString(), eq(dataMap), anyBoolean())).thenReturn(MailMergeFieldMnem.PAGE_BREAK.asMailMergeTag()+"testing");

    testAndAssertGeneration(
        DocGenType.PREVIEW,
        true,
        pwaApplicationDetail.getPwaApplicationRef(),
        docGenType -> {
          var documentSectionGenerator = mock(DocumentSectionGenerator.class);
          when(documentSectionGenerator.getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType))
              .thenReturn(new DocumentSectionData("TEMPLATE", dataMap));
          return documentSectionGenerator;
        }
    );

    var captor = ArgumentCaptor.forClass(Map.class);
    verify(templateRenderingService).render(eq("documents/consents/consentDocument.ftl"), captor.capture(), anyBoolean());

    var sectionHtml = (String) captor.getValue().get("sectionHtml");
    assertThat(sectionHtml)
        .doesNotContain(MailMergeFieldMnem.PAGE_BREAK.asMailMergeTag())
        .contains("testing" + DIV_CLASS_PAGE_BREAK + "testing");
  }

}
