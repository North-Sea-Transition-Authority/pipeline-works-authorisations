package uk.co.ogauthority.pwa.controller.search.consents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}/consent/{pwaConsentId}/documents")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class ConsentFileController {

  private final PwaViewTabService pwaViewTabService;
  private final DocgenService docgenService;
  private final PwaConsentService pwaConsentService;
  private final ConsentFileViewerService consentFileViewerService;
  private final SearchPwaBreadcrumbService breadcrumbService;

  @Autowired
  public ConsentFileController(PwaViewTabService pwaViewTabService,
                               DocgenService docgenService,
                               PwaConsentService pwaConsentService,
                               ConsentFileViewerService consentFileViewerService,
                               SearchPwaBreadcrumbService breadcrumbService) {
    this.pwaViewTabService = pwaViewTabService;
    this.docgenService = docgenService;
    this.pwaConsentService = pwaConsentService;
    this.consentFileViewerService = consentFileViewerService;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderViewConsentDocuments(@PathVariable("pwaId") Integer pwaId,
                                                 @PathVariable Integer pwaConsentId,
                                                 PwaContext pwaContext,
                                                 AuthenticatedUserAccount userAccount) {
    var consent = pwaConsentService.getConsentById(pwaConsentId);
    var consentDto = pwaViewTabService.getConsentHistoryTabContentForConsentId(consent.getId())
        .orElseThrow(() ->
            new IllegalStateException(
                String.format("Unable to resolve consent application dto from consent with id %s", consent.getId())));

    var consentFileView = consentFileViewerService.getConsentFileView(consent.getSourcePwaApplication(), consentDto,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    var modelAndView = new ModelAndView("search/consents/consentFiles")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("consentFileView", consentFileView)
        .addObject("urlFactory", new PwaViewUrlFactory(pwaId));

    breadcrumbService.fromPwaConsentTab(
        pwaId,
        pwaContext.getConsentSearchResultView().getPwaReference(),
        modelAndView,
        String.format("%s documents", consent.getReference()));

    return modelAndView;

  }

  @GetMapping("/download/{docgenRunId}")
  public ResponseEntity<Resource> downloadConsentDocument(@PathVariable("pwaId") Integer pwaId,
                                                          PwaContext pwaContext,
                                                          @PathVariable Integer pwaConsentId,
                                                          @PathVariable Long docgenRunId) {

    try {

      var docgenRun = docgenService.getDocgenRun(docgenRunId);

      var pwaConsent = pwaConsentService.getConsentById(pwaConsentId);

      pwaViewTabService.verifyConsentDocumentDownloadable(docgenRun, pwaConsent, pwaContext);

      var blob = docgenRun.getGeneratedDocument();

      var inputStream = blob.getBinaryStream();

      String filename = pwaConsent.getReference().replace("/", "-") + " consent document.pdf";
      return FileDownloadUtils.getResourceResponseEntity(blob, inputStream, filename);

    } catch (Exception e) {
      throw new RuntimeException(String.format("Error serving document with doc gen run ID %s for consent ID %s",
          docgenRunId, pwaConsentId), e);
    }

  }


}
