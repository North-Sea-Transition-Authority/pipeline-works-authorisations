package uk.co.ogauthority.pwa.service.documents.generation;

import java.sql.Blob;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;

@Service
public class DocumentCreationService {

  private final ApplicationContext springApplicationContext;
  private final TemplateRenderingService templateRenderingService;
  private final PdfRenderingService pdfRenderingService;
  private final DocumentInstanceService documentInstanceService;
  private final MailMergeService mailMergeService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaConsentService pwaConsentService;

  @Autowired
  public DocumentCreationService(ApplicationContext springApplicationContext,
                                 TemplateRenderingService templateRenderingService,
                                 PdfRenderingService pdfRenderingService,
                                 DocumentInstanceService documentInstanceService,
                                 MailMergeService mailMergeService,
                                 PwaApplicationDetailService pwaApplicationDetailService,
                                 PwaConsentService pwaConsentService) {
    this.springApplicationContext = springApplicationContext;
    this.templateRenderingService = templateRenderingService;
    this.pdfRenderingService = pdfRenderingService;
    this.documentInstanceService = documentInstanceService;
    this.mailMergeService = mailMergeService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaConsentService = pwaConsentService;
  }

  public Blob createConsentDocument(DocumentInstance documentInstance,
                                    DocGenType docGenType) {

    var app = documentInstance.getPwaApplication();

    var latestSubmittedDetail = pwaApplicationDetailService
        .getLatestSubmittedDetail(app)
        .orElseThrow(() -> new RuntimeException(
            String.format("No submitted detail found when trying to generate document for app with id: %s", app)));

    var docSpec = documentInstance.getPwaApplication().getDocumentSpec();

    // for each section defined in the doc spec:
    // 1. get the template path and model map of relevant data for the section
    // 2. render the section and collect the html
    // 3. join onto previously rendered section
    var combinedSectionHtml = docSpec.getDocumentSectionDisplayOrderMap().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(entry -> getDocumentSectionData(latestSubmittedDetail, documentInstance, entry.getKey(), docGenType))
        .filter(Objects::nonNull)
        .map(sectionData -> templateRenderingService.render(sectionData.getTemplatePath(), sectionData.getTemplateModel(), false))
        .collect(Collectors.joining());

    // render the main consent doc template using the joined-up section html as the 'data'

    // use consent ref if available, fallback to app ref
    String consentRef = pwaConsentService.getConsentByPwaApplication(app)
        .map(PwaConsent::getReference)
        .orElse(latestSubmittedDetail.getPwaApplicationRef());

    Map<String, Object> docModelAndView = Map.of(
        "sectionHtml", combinedSectionHtml,
        "consentRef", consentRef,
        "showWatermark", docGenType == DocGenType.PREVIEW
    );

    var docHtml = templateRenderingService.render("documents/consents/consentDocument.ftl", docModelAndView, false);

    return pdfRenderingService.renderToBlob(docHtml);

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
