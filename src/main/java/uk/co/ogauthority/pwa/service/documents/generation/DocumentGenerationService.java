package uk.co.ogauthority.pwa.service.documents.generation;

import java.sql.Blob;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;

@Service
public class DocumentGenerationService {

  private final ApplicationContext springApplicationContext;
  private final TemplateRenderingService templateRenderingService;
  private final PdfRenderingService pdfRenderingService;

  @Autowired
  public DocumentGenerationService(ApplicationContext springApplicationContext,
                                   TemplateRenderingService templateRenderingService,
                                   PdfRenderingService pdfRenderingService) {
    this.springApplicationContext = springApplicationContext;
    this.templateRenderingService = templateRenderingService;
    this.pdfRenderingService = pdfRenderingService;
  }

  public Blob generateConsentDocument(PwaApplicationDetail pwaApplicationDetail,
                                      DocGenType docGenType) {

    var docSpec = pwaApplicationDetail.getPwaApplicationType().getConsentDocumentSpec();

    // for each section defined in the doc spec:
    // 1. get the template path and model map of relevant data for the section
    // 2. render the section and collect the html
    // 3. join onto previously rendered section
    var combinedSectionHtml = docSpec.getDocumentSectionDisplayOrderMap().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(entry -> springApplicationContext.getBean(entry.getKey().getSectionGenerator()).getDocumentSectionData(pwaApplicationDetail))
        .filter(Objects::nonNull) // TODO remove when all sections implemented
        .map(sectionData -> templateRenderingService.render(sectionData.getTemplatePath(), sectionData.getTemplateModel(), false))
        .collect(Collectors.joining());

    // render the main consent doc template using the joined-up section html as the 'data'
    Map<String, Object> docModelAndView = Map.of(
        "sectionHtml", combinedSectionHtml,
        "consentRef", pwaApplicationDetail.getPwaApplicationRef(), // TODO PWA-872 update with consent ref
        "showWatermark", docGenType == DocGenType.PREVIEW
    );

    var docHtml = templateRenderingService.render("documents/consents/consentDocument.ftl", docModelAndView, false);

    return pdfRenderingService.renderToBlob(docHtml);

  }

}
