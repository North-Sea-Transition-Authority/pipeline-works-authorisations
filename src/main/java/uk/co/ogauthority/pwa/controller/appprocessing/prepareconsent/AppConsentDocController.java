package uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.io.InputStream;
import java.sql.Blob;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentDocumentService;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentDocumentUrlFactory;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.FailedSendForApprovalCheck;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.PreSendForApprovalChecksView;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.PrepareConsentTaskService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlFactory;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentGenerationService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/consent-document")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class AppConsentDocController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final DocumentService documentService;
  private final DocumentGenerationService documentGenerationService;
  private final PrepareConsentTaskService prepareConsentTaskService;
  private final ControllerHelperService controllerHelperService;
  private final TemplateTextService templateTextService;
  private final ConsentDocumentService consentDocumentService;
  private final ConsentReviewService consentReviewService;
  private final MailMergeService mailMergeService;

  @Autowired
  public AppConsentDocController(AppProcessingBreadcrumbService breadcrumbService,
                                 DocumentService documentService,
                                 DocumentGenerationService documentGenerationService,
                                 PrepareConsentTaskService prepareConsentTaskService,
                                 ControllerHelperService controllerHelperService,
                                 TemplateTextService templateTextService,
                                 ConsentDocumentService consentDocumentService,
                                 ConsentReviewService consentReviewService,
                                 MailMergeService mailMergeService) {
    this.breadcrumbService = breadcrumbService;
    this.documentService = documentService;
    this.documentGenerationService = documentGenerationService;
    this.prepareConsentTaskService = prepareConsentTaskService;
    this.controllerHelperService = controllerHelperService;
    this.templateTextService = templateTextService;
    this.consentDocumentService = consentDocumentService;
    this.consentReviewService = consentReviewService;
    this.mailMergeService = mailMergeService;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView renderConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             PwaAppProcessingContext processingContext,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    return whenPrepareConsentAvailable(
        processingContext,
        () -> {

          var docInstanceOpt = documentService
              .getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

          var docView = docInstanceOpt
              .map(documentService::getDocumentViewForInstance)
              .map(documentView -> mailMergeService.mailMerge(documentView, DocGenType.PREVIEW))
              .orElse(null);

          var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/consentDocumentEditor")
              .addObject("caseSummaryView", processingContext.getCaseSummaryView())
              .addObject("docInstanceExists", docInstanceOpt.isPresent())
              .addObject("consentDocumentUrlFactory",
                  new ConsentDocumentUrlFactory(processingContext.getPwaApplication()))
              .addObject("clauseActionsUrlFactory",
                  new ClauseActionsUrlFactory(processingContext.getPwaApplication(), docView))
              .addObject("docView", docView)
              .addObject("userProcessingPermissions", processingContext.getAppProcessingPermissions());

          breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Prepare consent");

          return modelAndView;

        });

  }

  @GetMapping("/download")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {
      PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW, PwaApplicationStatus.COMPLETE})
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_CONSENT_DOCUMENT)
  public ResponseEntity<Resource> downloadPdf(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount) {

    return resourceWhenPrepareConsentAvailable(
        processingContext,
        () -> {

          try {

            var blob = documentGenerationService.generateConsentDocument(processingContext.getApplicationDetail(),
                DocGenType.PREVIEW);
            var inputStream = blob.getBinaryStream();

            return getResourceResponseEntity(blob, inputStream);

          } catch (Exception e) {
            throw new RuntimeException("Error serving document", e);
          }

        });

  }

  private ResponseEntity<Resource> getResourceResponseEntity(Blob blob, InputStream inputStream) {

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(blob.length())
          .header(HttpHeaders.CONTENT_DISPOSITION,
              String.format("attachment; filename=\"%s\"", "test-filename.pdf"))
          .body(new InputStreamResource(inputStream));

    } catch (Exception e) {
      throw new RuntimeException(String.format("Error serving file '%s'", "test-filename.pdf"), e);
    }

  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView postConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           RedirectAttributes redirectAttributes) {

    return whenPrepareConsentAvailable(
        processingContext,
        () -> {

          documentService.createDocumentInstance(
              processingContext.getPwaApplication(),
              DocumentTemplateMnem.PWA_CONSENT_DOCUMENT,
              authenticatedUserAccount.getLinkedPerson());

          FlashUtils.info(redirectAttributes, "Document loaded");

          return ReverseRouter.redirect(on(AppConsentDocController.class)
              .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

        });

  }

  @GetMapping("/reload")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView renderReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           RedirectAttributes redirectAttributes) {

    return whenDocumentReloadable(
        processingContext,
        redirectAttributes,
        () -> new ModelAndView("pwaApplication/appProcessing/prepareConsent/reloadDocumentConfirm")
            .addObject("appRef", processingContext.getPwaApplication().getAppReference())
            .addObject("consentDocumentUrlFactory",
                new ConsentDocumentUrlFactory(processingContext.getPwaApplication())));

  }

  @PostMapping("/reload")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView postReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    return whenDocumentReloadable(
        processingContext,
        redirectAttributes,
        () -> {

          documentService.reloadDocumentInstance(
              processingContext.getPwaApplication(),
              DocumentTemplateMnem.PWA_CONSENT_DOCUMENT,
              authenticatedUserAccount.getLinkedPerson());

          FlashUtils.info(redirectAttributes, "Document reloaded");

          return ReverseRouter.redirect(on(AppConsentDocController.class)
              .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

        });

  }

  private ModelAndView whenDocumentReloadable(PwaAppProcessingContext processingContext,
                                              RedirectAttributes redirectAttributes,
                                              Supplier<ModelAndView> successSupplier) {

    if (documentService.getDocumentInstance(processingContext.getPwaApplication(),
        DocumentTemplateMnem.PWA_CONSENT_DOCUMENT).isEmpty()) {

      FlashUtils.error(redirectAttributes, String.format("%s does not have a consent document to reload",
          processingContext.getPwaApplication().getAppReference()));

      return ReverseRouter.redirect(on(AppConsentDocController.class)
          .renderConsentDocEditor(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null));

    }

    return successSupplier.get();

  }

  @GetMapping("/send-for-approval")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL)
  public ModelAndView renderSendForApproval(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @ModelAttribute("form") SendConsentForApprovalForm form,
                                           RedirectAttributes redirectAttributes) {

    return whenSendForApprovalAvailable(
        processingContext,
        redirectAttributes,
        (preSendForApprovalChecksView) -> {

          String coverLetterText = templateTextService
              .getLatestVersionTextByType(processingContext.getApplicationType().getConsentEmailTemplateTextType());
          form.setCoverLetterText(coverLetterText);

          return getSendForApprovalModelAndView(processingContext, preSendForApprovalChecksView);

        });

  }

  private ModelAndView getSendForApprovalModelAndView(PwaAppProcessingContext processingContext,
                                                      PreSendForApprovalChecksView preSendForApprovalChecksView) {

    var urlFactory = new SendForApprovalUrlFactory(preSendForApprovalChecksView.getParallelConsentViews());

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/sendForApproval")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("cancelUrl", ReverseRouter.route(on(AppConsentDocController.class)
            .renderConsentDocEditor(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null)))
        .addObject("parallelConsentViews", preSendForApprovalChecksView.getParallelConsentViews())
        .addObject("urlFactory", urlFactory);


    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, "Send consent for approval");

    return modelAndView;

  }

  @PostMapping("/send-for-approval")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL)
  public ModelAndView sendForApproval(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      @ModelAttribute("form") SendConsentForApprovalForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount authUser,
                                      RedirectAttributes redirectAttributes) {
    return whenSendForApprovalAvailable(
        processingContext,
        redirectAttributes,
        (preSendForApprovalChecksView) -> {

          consentDocumentService.validateSendConsentFormUsingPreApprovalChecks(
              form,
              bindingResult,
              preSendForApprovalChecksView);
          return controllerHelperService.checkErrorsAndRedirect(
              bindingResult,
              getSendForApprovalModelAndView(processingContext, preSendForApprovalChecksView),
              () -> {

                consentDocumentService.sendForApproval(
                    processingContext.getApplicationDetail(),
                    form.getCoverLetterText(),
                    authUser,
                    preSendForApprovalChecksView.getParallelConsentViews());

                FlashUtils.info(redirectAttributes,processingContext.getPwaApplication().getAppReference() + " consent sent for approval");

                return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));

              });
        });
  }

  public ModelAndView whenPrepareConsentAvailable(PwaAppProcessingContext processingContext,
                                                  Supplier<ModelAndView> modelAndViewSupplier) {

    if (!prepareConsentTaskService.taskAccessible(processingContext)) {
      throwAccessDeniedException(processingContext);
    }

    return modelAndViewSupplier.get();

  }

  private ModelAndView whenSendForApprovalAvailable(PwaAppProcessingContext processingContext,
                                                    RedirectAttributes redirectAttributes,
                                                    Function<PreSendForApprovalChecksView, ModelAndView> modelAndViewFunction) {
    var application = processingContext.getPwaApplication();
    if (consentReviewService.areThereAnyOpenReviews(processingContext.getApplicationDetail())) {
      FlashUtils.info(redirectAttributes, "Already sent for approval",
          String.format("There is already a consent review open for the application with reference %s", application.getAppReference()));
      return ReverseRouter.redirect(on(WorkAreaController.class)
          .renderWorkArea(null, null, null));
    }


    var preSendForApprovalChecksView = consentDocumentService
        .getPreSendForApprovalChecksView(processingContext.getApplicationDetail());

    var reasonsToPreventSendForApproval = preSendForApprovalChecksView.getFailedSendForApprovalChecks();

    if (!reasonsToPreventSendForApproval.isEmpty()) {
      FlashUtils.errorWithBulletPoints(redirectAttributes,
          "Tasks outstanding",
          "All outstanding tasks must be completed before sending the consent for approval.",
          reasonsToPreventSendForApproval.stream()
              .map(FailedSendForApprovalCheck::getReason)
              .collect(Collectors.toList())
      );

      return ReverseRouter.redirect(on(AppConsentDocController.class)
          .renderConsentDocEditor(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null));
    }

    return modelAndViewFunction.apply(preSendForApprovalChecksView);

  }

  public ResponseEntity<Resource> resourceWhenPrepareConsentAvailable(PwaAppProcessingContext processingContext,
                                                                      Supplier<ResponseEntity<Resource>> resourceSupplier) {

    if (!prepareConsentTaskService.taskAccessible(processingContext)) {
      throwAccessDeniedException(processingContext);
    }

    return resourceSupplier.get();

  }

  private void throwAccessDeniedException(PwaAppProcessingContext processingContext) {
    throw new AccessDeniedException(String.format(
        "Can't access %s controller routes as application with id [%s] has invalid task state",
        PwaAppProcessingTask.PREPARE_CONSENT.name(),
        processingContext.getMasterPwaApplicationId()));

  }

}
