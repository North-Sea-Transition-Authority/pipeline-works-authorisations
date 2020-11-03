package uk.co.ogauthority.pwa.controller.appprocessing.decision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory;
import uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlFactory;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.documents.pdf.PdfRenderingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/consent-document")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class AppConsentDocController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final DocumentService documentService;
  private final TemplateRenderingService templateRenderingService;
  private final PdfRenderingService pdfRenderingService;

  @Autowired
  public AppConsentDocController(AppProcessingBreadcrumbService breadcrumbService,
                                 DocumentService documentService,
                                 TemplateRenderingService templateRenderingService,
                                 PdfRenderingService pdfRenderingService) {
    this.breadcrumbService = breadcrumbService;
    this.documentService = documentService;
    this.templateRenderingService = templateRenderingService;
    this.pdfRenderingService = pdfRenderingService;
  }

  @GetMapping
  public ModelAndView renderConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             PwaAppProcessingContext processingContext,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    var docInstanceOpt = documentService
        .getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    var docView = docInstanceOpt
        .map(documentService::getDocumentViewForInstance)
        .orElse(null);

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/decision/consentDocumentEditor")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("docInstanceExists", docInstanceOpt.isPresent())
        .addObject("consentDocumentUrlFactory", new ConsentDocumentUrlFactory(processingContext.getPwaApplication()))
        .addObject("clauseActionsUrlFactory", new ClauseActionsUrlFactory(processingContext.getPwaApplication(), docView))
        .addObject("docView", docView)
        .addObject("downloadUrl", ReverseRouter.route(on(AppConsentDocController.class)
            .downloadPdf(applicationId, pwaApplicationType, processingContext, authenticatedUserAccount)));

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Consent document");

    return modelAndView;

  }

  @GetMapping("/download")
  @ResponseBody
  // todo remove when doc framework implemented PWA-120
  public ResponseEntity<Resource> downloadPdf(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount) {

    try {
      var html = templateRenderingService.render("documents/testDocument.ftl", Map.of("showWatermark", true), false);
      var blob = pdfRenderingService.renderToBlob(html);
      var inputStream = blob.getBinaryStream();

      try {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(blob.length())
            .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", "test-filename.pdf"))
            .body(new InputStreamResource(inputStream));
      } catch (Exception e) {
        throw new RuntimeException(String.format("Error serving file '%s'", "test-filename.pdf"), e);
      }

    } catch (Exception e) {
      throw new RuntimeException("Error serving document", e);
    }

  }

  @PostMapping
  public ModelAndView postConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   PwaAppProcessingContext processingContext,
                                   AuthenticatedUserAccount authenticatedUserAccount,
                                   RedirectAttributes redirectAttributes) {

    documentService.createDocumentInstance(
        processingContext.getPwaApplication(),
        DocumentTemplateMnem.PWA_CONSENT_DOCUMENT,
        authenticatedUserAccount.getLinkedPerson());

    FlashUtils.info(redirectAttributes, "Document loaded");

    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

  }

  @GetMapping("/reload")
  public ModelAndView renderReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           RedirectAttributes redirectAttributes) {

    if (documentService.getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT).isEmpty()) {
      return flashErrorAndReturn(processingContext, redirectAttributes);
    }

    return new ModelAndView("pwaApplication/appProcessing/decision/reloadDocumentConfirm")
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("consentDocumentUrlFactory", new ConsentDocumentUrlFactory(processingContext.getPwaApplication()));

  }

  @PostMapping("/reload")
  public ModelAndView postReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    if (documentService.getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT).isEmpty()) {
      return flashErrorAndReturn(processingContext, redirectAttributes);
    }

    documentService.reloadDocumentInstance(
        processingContext.getPwaApplication(),
        DocumentTemplateMnem.PWA_CONSENT_DOCUMENT,
        authenticatedUserAccount.getLinkedPerson());

    FlashUtils.info(redirectAttributes, "Document reloaded");

    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

  }

  private ModelAndView flashErrorAndReturn(PwaAppProcessingContext processingContext,
                                           RedirectAttributes redirectAttributes) {

    int applicationId = processingContext.getPwaApplication().getId();
    var pwaApplicationType = processingContext.getApplicationType();

    FlashUtils.error(redirectAttributes, String.format("%s does not have a consent document to reload",
        processingContext.getPwaApplication().getAppReference()));

    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));
  }

}
