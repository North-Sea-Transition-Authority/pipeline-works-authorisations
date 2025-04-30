package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.documents.DocumentInstanceException;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.PrepareConsentTaskService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.ConsentDocumentService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.SendForApprovalUrlFactory;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.FailedSendForApprovalCheck;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.PreSendForApprovalChecksView;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.features.consents.viewconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatusResult;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceClauseActionsUrlProvider;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/consent-document")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class AppConsentDocController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final DocumentService documentService;
  private final PrepareConsentTaskService prepareConsentTaskService;
  private final ControllerHelperService controllerHelperService;
  private final TemplateTextService templateTextService;
  private final ConsentDocumentService consentDocumentService;
  private final ConsentReviewService consentReviewService;
  private final MailMergeService mailMergeService;
  private final DocgenService docgenService;
  private final MarkdownService markdownService;
  private final ConsentFileViewerService consentFileViewerService;
  private final AnalyticsService analyticsService;
  private final FileService fileService;
  private final AppFileManagementService appFileManagementService;

  @Autowired
  public AppConsentDocController(AppProcessingBreadcrumbService breadcrumbService,
                                 DocumentService documentService,
                                 PrepareConsentTaskService prepareConsentTaskService,
                                 ControllerHelperService controllerHelperService,
                                 TemplateTextService templateTextService,
                                 ConsentDocumentService consentDocumentService,
                                 ConsentReviewService consentReviewService,
                                 MailMergeService mailMergeService,
                                 DocgenService docgenService,
                                 MarkdownService markdownService,
                                 ConsentFileViewerService consentFileViewerService,
                                 AnalyticsService analyticsService,
                                 FileService fileService,
                                 AppFileManagementService appFileManagementService
  ) {
    this.breadcrumbService = breadcrumbService;
    this.documentService = documentService;
    this.prepareConsentTaskService = prepareConsentTaskService;
    this.controllerHelperService = controllerHelperService;
    this.templateTextService = templateTextService;
    this.consentDocumentService = consentDocumentService;
    this.consentReviewService = consentReviewService;
    this.mailMergeService = mailMergeService;
    this.docgenService = docgenService;
    this.markdownService = markdownService;
    this.consentFileViewerService = consentFileViewerService;
    this.analyticsService = analyticsService;
    this.fileService = fileService;
    this.appFileManagementService = appFileManagementService;
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

          var docMnem = DocumentTemplateMnem.getMnemFromResourceType(processingContext.getApplicationDetail().getResourceType());
          var docInstanceOpt = documentService
              .getDocumentInstance(processingContext.getPwaApplication(), docMnem);

          var docView = docInstanceOpt
              .map(documentService::getDocumentViewForInstance)
              .orElse(null);

          if (docView != null) {
            mailMergeService.mailMerge(docView, DocGenType.PREVIEW);
          }

          var sosdConsultationRequestView = consentFileViewerService.getLatestConsultationRequestViewForDocumentType(
              processingContext.getPwaApplication(), ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION).orElse(null);

          var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/consentDocumentEditor")
              .addObject("caseSummaryView", processingContext.getCaseSummaryView())
              .addObject("docInstanceExists", docInstanceOpt.isPresent())
              .addObject("consentDocumentUrlProvider",
                  new ConsentDocumentUrlProvider(processingContext.getPwaApplication()))
              .addObject("clauseActionsUrlProvider",
                  new DocumentInstanceClauseActionsUrlProvider(processingContext.getPwaApplication(), docView))
              .addObject("docView", docView)
              .addObject("userProcessingPermissions", processingContext.getAppProcessingPermissions())
              .addObject("automaticMailMergePreviewClasses", mailMergeService.getMailMergePreviewClasses(MailMergeFieldType.AUTOMATIC))
              .addObject("manualMailMergePreviewClasses", mailMergeService.getMailMergePreviewClasses(MailMergeFieldType.MANUAL))
              .addObject("sosdConsultationRequestView", sosdConsultationRequestView)
              .addObject("openConsentReview", processingContext.hasOpenConsentReview());

          breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Prepare consent");

          return modelAndView;

        });

  }

  @GetMapping("/{docgenRunId}/status")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public DocgenRunStatusResult getDocgenRunStatus(@PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable Long docgenRunId,
                                                  PwaAppProcessingContext processingContext) {

    if (!prepareConsentTaskService.taskAccessible(processingContext)) {
      throwAccessDeniedException(processingContext);
    }

    var docgenRun = docgenService.getDocgenRun(docgenRunId);

    checkDocgenRunAccessible(docgenRun, processingContext);

    return docgenService.getDocgenRunStatus(docgenRunId,
        ReverseRouter.route(on(AppConsentDocController.class).renderConsentDocEditor(applicationId, pwaApplicationType, null, null)));

  }

  @PostMapping("/preview")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView schedulePreview(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      AuthenticatedUserAccount authenticatedUserAccount,
                                      @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false)
                                      Optional<String> analyticsClientId) {

    return whenPrepareConsentAvailable(processingContext, () -> {

      var docMnem = DocumentTemplateMnem.getMnemFromResourceType(processingContext.getApplicationDetail().getResourceType());
      var docInstance = documentService
          .getDocumentInstance(processingContext.getPwaApplication(), docMnem)
          .orElseThrow(() -> new DocumentInstanceException(String.format("Couldn't find doc instance for app with id: %s",
              applicationId)));

      var run = docgenService.createDocgenRun(docInstance, DocGenType.PREVIEW, authenticatedUserAccount.getLinkedPerson());
      docgenService.scheduleDocumentGeneration(run);

      analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.DOCUMENT_PREVIEW);

      return ReverseRouter.redirect(on(AppConsentDocController.class)
          .renderDocumentGenerating(applicationId, pwaApplicationType, run.getId(), null, null));
    });

  }

  @GetMapping("/{docgenRunId}/generating")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ModelAndView renderDocumentGenerating(@PathVariable("applicationId") Integer applicationId,
                                               @PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable Long docgenRunId,
                                               PwaAppProcessingContext processingContext,
                                               AuthenticatedUserAccount authenticatedUserAccount) {

    return whenPrepareConsentAvailable(processingContext, () -> {

      var docgenRun = docgenService.getDocgenRun(docgenRunId);

      checkDocgenRunAccessible(docgenRun, processingContext);

      var statusUrl = ReverseRouter.route(on(AppConsentDocController.class)
          .getDocgenRunStatus(applicationId, pwaApplicationType, docgenRunId, null));

      ModelAndView modelAndView = new ModelAndView("docgen/documentGenerating");
      modelAndView.addObject("statusUrl", statusUrl);
      modelAndView.addObject("returnUrl",
          ReverseRouter.route(on(AppConsentDocController.class)
              .renderConsentDocEditor(applicationId, pwaApplicationType, null, null)));

      return modelAndView;

    });

  }

  private void checkDocgenRunAccessible(DocgenRun docgenRun,
                                        PwaAppProcessingContext processingContext) {

    var applicationId = processingContext.getMasterPwaApplicationId();

    if (!docgenRun.getDocumentInstance().getPwaApplication().getId().equals(applicationId)) {
      throw new AccessDeniedException(
          String.format("User tried to access docgen run for a different application (ID: %s) than the " +
              "one they are viewing (ID: %s)", docgenRun.getDocumentInstance().getPwaApplication().getId(), applicationId));
    }

    if (docgenRun.getDocGenType() != DocGenType.PREVIEW) {
      throw new AccessDeniedException(String.format(
          "User tried to access a non-PREVIEW docgen run using the preview endpoint for app with ID: %s", applicationId));
    }

  }

  @GetMapping("/download/{docgenRunId}")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = { PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable("applicationId") Integer applicationId,
                                                         @PathVariable("applicationType")
                                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                         @PathVariable Long docgenRunId,
                                                         PwaAppProcessingContext processingContext,
                                                         AuthenticatedUserAccount authenticatedUserAccount) {
    var docgenRun = docgenService.getDocgenRun(docgenRunId);

    checkDocgenRunAccessible(docgenRun, processingContext);

    var files = appFileManagementService.getUploadedFiles(processingContext.getPwaApplication(), FileDocumentType.CONSENT_PREVIEW).stream()
        .sorted(Comparator.comparing(UploadedFile::getUploadedAt).reversed())
        .toList();

    if (files.isEmpty()) {
      throw new ResourceNotFoundException("No consent preview found for application: " + applicationId);
    }

    return fileService.download(files.getFirst());
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
            .addObject("consentDocumentUrlProvider",
                new ConsentDocumentUrlProvider(processingContext.getPwaApplication())));

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
              authenticatedUserAccount.getLinkedPerson());

          FlashUtils.info(redirectAttributes, "Document reloaded");

          return ReverseRouter.redirect(on(AppConsentDocController.class)
              .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

        });

  }

  private ModelAndView whenDocumentReloadable(PwaAppProcessingContext processingContext,
                                              RedirectAttributes redirectAttributes,
                                              Supplier<ModelAndView> successSupplier) {

    var docMnem = DocumentTemplateMnem.getMnemFromResourceType(processingContext.getApplicationDetail().getResourceType());
    if (documentService.getDocumentInstance(processingContext.getPwaApplication(), docMnem).isEmpty()) {
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
        .addObject("nonBlockingTasksWarning", preSendForApprovalChecksView.getNonBlockingTasksWarning())
        .addObject("urlFactory", urlFactory);


    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, "Send consent for approval");

    return modelAndView;

  }

  @PostMapping(value = "/send-for-approval", params = "preview-text-button")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW})
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL)
  public ModelAndView previewCoverLetter(@PathVariable("applicationId") Integer applicationId,
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

            var mergeContainer = mailMergeService.resolveMergeFields(processingContext.getPwaApplication(), DocGenType.PREVIEW);
            var markdownPreviewHtml = markdownService.convertMarkdownToHtml(form.getCoverLetterText(), mergeContainer);

            return getSendForApprovalModelAndView(processingContext, preSendForApprovalChecksView)
                .addObject("markdownPreviewHtml", markdownPreviewHtml);

        });

  }

  @PostMapping(value = "/send-for-approval")
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
              processingContext.getPwaApplication(),
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
