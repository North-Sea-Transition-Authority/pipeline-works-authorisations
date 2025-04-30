package uk.co.ogauthority.pwa.features.consents.viewconsent.controller;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.consents.viewconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}/consent/{pwaConsentId}/documents")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class ConsentFileController {

  private final PwaViewTabService pwaViewTabService;
  private final PwaConsentService pwaConsentService;
  private final ConsentFileViewerService consentFileViewerService;
  private final SearchPwaBreadcrumbService breadcrumbService;
  private final FileService fileService;
  private final AppFileManagementService appFileManagementService;


  @Autowired
  public ConsentFileController(PwaViewTabService pwaViewTabService,
                               PwaConsentService pwaConsentService,
                               ConsentFileViewerService consentFileViewerService,
                               SearchPwaBreadcrumbService breadcrumbService,
                               FileService fileService,
                               AppFileManagementService appFileManagementService) {
    this.pwaViewTabService = pwaViewTabService;
    this.pwaConsentService = pwaConsentService;
    this.consentFileViewerService = consentFileViewerService;
    this.breadcrumbService = breadcrumbService;
    this.fileService = fileService;
    this.appFileManagementService = appFileManagementService;
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

  @GetMapping("/download")
  public ResponseEntity<InputStreamResource> downloadConsentDocument(@PathVariable("pwaId") Integer pwaId,
                                                                     PwaContext pwaContext,
                                                                     @PathVariable Integer pwaConsentId) {

    var pwaApplication = pwaConsentService.getConsentById(pwaConsentId).getSourcePwaApplication();
    var files = appFileManagementService.getUploadedFiles(pwaApplication, FileDocumentType.CONSENT_DOCUMENT);

    if (files.isEmpty()) {
      throw new ResourceNotFoundException("No consent document found for application: " + pwaApplication.getId());
    }

    return fileService.download(files.getFirst());
  }

}
