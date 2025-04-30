package uk.co.ogauthority.pwa.service.documents.generation;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.documents.generation.DocgenRunSectionData;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunSectionDataRepository;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.documents.signing.DocumentSigningService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class DocumentCreationService {

  private final ApplicationContext springApplicationContext;
  private final TemplateRenderingService templateRenderingService;
  private final PdfRenderingService pdfRenderingService;
  private final DocumentInstanceService documentInstanceService;
  private final MailMergeService mailMergeService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaConsentService pwaConsentService;
  private final DocgenRunSectionDataRepository docgenRunSectionDataRepository;
  private final DocumentSigningService documentSigningService;

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCreationService.class);

  private static final String NON_BREAKING_SPACE_CHAR = "\u00A0";

  @Autowired
  public DocumentCreationService(ApplicationContext springApplicationContext,
                                 TemplateRenderingService templateRenderingService,
                                 PdfRenderingService pdfRenderingService,
                                 DocumentInstanceService documentInstanceService,
                                 MailMergeService mailMergeService,
                                 PwaApplicationDetailService pwaApplicationDetailService,
                                 PwaConsentService pwaConsentService,
                                 DocgenRunSectionDataRepository docgenRunSectionDataRepository,
                                 DocumentSigningService documentSigningService) {
    this.springApplicationContext = springApplicationContext;
    this.templateRenderingService = templateRenderingService;
    this.pdfRenderingService = pdfRenderingService;
    this.documentInstanceService = documentInstanceService;
    this.mailMergeService = mailMergeService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaConsentService = pwaConsentService;
    this.docgenRunSectionDataRepository = docgenRunSectionDataRepository;
    this.documentSigningService = documentSigningService;
  }

  public ByteArrayResource createConsentDocument(DocgenRun docgenRun) {

    var documentInstance = docgenRun.getDocumentInstance();
    var docGenType = docgenRun.getDocGenType();
    var app = documentInstance.getPwaApplication();

    var isPreview = docGenType == DocGenType.PREVIEW;

    var latestSubmittedDetail = pwaApplicationDetailService
        .getLatestSubmittedDetail(app)
        .orElseThrow(() -> new RuntimeException(
            String.format("No submitted detail found when trying to generate document for app with id: %s", app)));

    var docSpec = documentInstance.getPwaApplication().getDocumentSpec();

    var logItems = new ArrayList<DocgenRunSectionData>();
    var combinedHtmlStringBuilder = new StringBuilder();

    // for each section defined in the doc spec:
    // 1. get the template path and model map of relevant data for the section
    // 2. render the section and collect the html
    // 3. join onto previously rendered section
    docSpec.getDocumentSectionDisplayOrderMap().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(entry -> {

          var sectionData = getDocumentSectionData(latestSubmittedDetail, documentInstance, entry.getKey(), docGenType);
          LOGGER.info("Document section data is null?: [{}] for: [{}] on detail [{}] with document instance [{}] and docGenType [{}]",
                  Objects.isNull(sectionData),
                  entry.getKey().name(),
                  latestSubmittedDetail.getId(),
                  documentInstance.getId(),
                  docGenType.name()
          );
          return sectionData;
        })
        .filter(Objects::nonNull)
        .forEach(documentSectionData -> {

          var htmlContentString = templateRenderingService
              .render(documentSectionData.getTemplatePath(), documentSectionData.getTemplateModel(), false);

          var logItem = new DocgenRunSectionData();
          logItem.setDocgenRun(docgenRun);
          logItem.setTemplateName(documentSectionData.getTemplatePath());
          logItem.setHtmlContent(htmlContentString);

          logItems.add(logItem);

          combinedHtmlStringBuilder.append(htmlContentString);

        });

    try {
      docgenRunSectionDataRepository.saveAll(logItems);
    } catch (Exception e) {
      LOGGER.error("Error saving docgen run section logs for docgen run id: {}", docgenRun.getId(), e);
    }

    // use consent ref if available, fallback to app ref
    String consentRef = pwaConsentService.getConsentByPwaApplication(app)
        .map(PwaConsent::getReference)
        .orElse(latestSubmittedDetail.getPwaApplicationRef());

    var pageBreakHtml = templateRenderingService.getRenderedTemplate("documents/consents/fragments/pageBreak.ftl", Map.of());

    var htmlString = combinedHtmlStringBuilder.toString()
        .replace(NON_BREAKING_SPACE_CHAR, " ")
        .replace(MailMergeFieldMnem.PAGE_BREAK.asMailMergeTag(), pageBreakHtml);

    // render the main consent doc template using the joined-up section html as the 'data'
    Map<String, Object> docModelAndView = Map.of(
        "sectionHtml", htmlString,
        "consentRef", consentRef,
        "showWatermark", isPreview,
        "issueDate", DateUtils.formatDate(LocalDate.now())
    );

    var docHtml = templateRenderingService.render("documents/consents/consentDocument.ftl", docModelAndView, false);

    var unsignedPdf = pdfRenderingService.render(docHtml);

    return signPdf(isPreview, unsignedPdf, docgenRun.getScheduledByPerson());

  }

  @VisibleForTesting
  ByteArrayResource signPdf(boolean isPreview, ByteArrayResource unsignedPdf, Person user) {
    if (isPreview) {
      return documentSigningService.previewPdfSignature(unsignedPdf);
    } else {
      return documentSigningService.signPdf(unsignedPdf, user);
    }
  }

  private DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                     DocumentInstance documentInstance,
                                                     DocumentSection documentSection,
                                                     DocGenType docGenType) {

    if (documentSection.getSectionType() == SectionType.CLAUSE_LIST) {

      var docView = documentInstanceService.getDocumentView(documentInstance, documentSection);
      mailMergeService.mailMerge(docView, docGenType);

      return new DocumentSectionData(
          "documents/consents/clauseSection",
          Map.of(
              "docView", docView,
              "sectionType", documentSection));

    }

    return springApplicationContext.getBean(documentSection.getSectionGenerator())
        .getDocumentSectionData(pwaApplicationDetail, documentInstance, docGenType);

  }

}
