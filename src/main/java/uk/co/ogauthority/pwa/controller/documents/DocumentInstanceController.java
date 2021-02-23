package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent.AppConsentDocController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.documents.clauses.ClauseFormValidator;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.documents.DocumentInstanceRedirectUtils;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/documents/{documentTemplateMnem}")
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class DocumentInstanceController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final DocumentInstanceService documentInstanceService;
  private final ClauseFormValidator clauseFormValidator;

  @Autowired
  public DocumentInstanceController(AppProcessingBreadcrumbService breadcrumbService,
                                    ControllerHelperService controllerHelperService,
                                    DocumentInstanceService documentInstanceService,
                                    ClauseFormValidator clauseFormValidator) {
    this.breadcrumbService = breadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.documentInstanceService = documentInstanceService;
    this.clauseFormValidator = clauseFormValidator;
  }

  @GetMapping("/add-clause-after/{clauseIdToAddAfter}")
  public ModelAndView renderAddClauseAfter(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                           @PathVariable("clauseIdToAddAfter") Integer clauseIdToAddAfter,
                                           @ModelAttribute("form") ClauseForm form,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddAfter);

    return getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD);

  }

  private ModelAndView getAddEditClauseModelAndView(PwaAppProcessingContext processingContext, ScreenActionType screenActionType) {

    var cancelUrl = ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(processingContext.getPwaApplication().getId(), processingContext.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("documents/clauses/addEditClause")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("actionType", screenActionType);

    String thisPage = screenActionType.getActionText() + " clause";
    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, thisPage);

    return modelAndView;

  }

  private ModelAndView getAddEditClauseModelAndView(PwaAppProcessingContext processingContext,
                                                    ScreenActionType screenActionType,
                                                    ClauseForm form) {
    return getAddEditClauseModelAndView(processingContext, screenActionType)
        .addObject("form", form);
  }

  @PostMapping("/add-clause-after/{clauseIdToAddAfter}")
  public ModelAndView postAddClauseAfter(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                         @PathVariable("clauseIdToAddAfter") Integer clauseIdToAddAfter,
                                         @ModelAttribute("form") ClauseForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    clauseFormValidator.validate(form, bindingResult);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD), () -> {

          var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddAfter);

          documentInstanceService.addClauseAfter(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);

        });

  }

  @GetMapping("/add-clause-before/{clauseIdToAddBefore}")
  public ModelAndView renderAddClauseBefore(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                           @PathVariable("clauseIdToAddBefore") Integer clauseIdToAddBefore,
                                           @ModelAttribute("form") ClauseForm form,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddBefore);

    return getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD);

  }

  @PostMapping("/add-clause-before/{clauseIdToAddBefore}")
  public ModelAndView postAddClauseBefore(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                          @PathVariable("clauseIdToAddBefore") Integer clauseIdToAddBefore,
                                          @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    clauseFormValidator.validate(form, bindingResult);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD), () -> {

          var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddBefore);

          documentInstanceService.addClauseBefore(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);

        });

  }

  @GetMapping("/add-sub-clause/{clauseId}")
  public ModelAndView renderAddSubClauseFor(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext,
                                            @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                            @PathVariable("clauseId") Integer clauseId,
                                            @ModelAttribute("form") ClauseForm form,
                                            AuthenticatedUserAccount authenticatedUserAccount) {

    documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);

    return getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD);

  }

  @PostMapping("/add-sub-clause/{clauseId}")
  public ModelAndView postAddSubClauseFor(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                          @PathVariable("clauseId") Integer clauseId,
                                          @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    clauseFormValidator.validate(form, bindingResult);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(processingContext, ScreenActionType.ADD), () -> {

          var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);

          documentInstanceService.addSubClause(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);

        });

  }

  @GetMapping("/edit-clause/{clauseId}")
  public ModelAndView renderEditClause(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       PwaAppProcessingContext processingContext,
                                       @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                       @PathVariable("clauseId") Integer clauseId,
                                       @ModelAttribute("form") ClauseForm form,
                                       AuthenticatedUserAccount authenticatedUserAccount) {

    var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);
    form.setName(clauseVersion.getName());
    form.setText(clauseVersion.getText());

    return getAddEditClauseModelAndView(processingContext, ScreenActionType.EDIT, form);

  }

  @PostMapping("/edit-clause/{clauseId}")
  public ModelAndView postEditClause(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     PwaAppProcessingContext processingContext,
                                     @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                     @PathVariable("clauseId") Integer clauseId,
                                     @ModelAttribute("form") ClauseForm form,
                                     BindingResult bindingResult,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     RedirectAttributes redirectAttributes) {

    clauseFormValidator.validate(form, bindingResult);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(processingContext, ScreenActionType.EDIT, form), () -> {

          var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);

          documentInstanceService.editClause(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause updated");

          return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);

        });

  }

  @GetMapping("/remove-clause/{clauseId}")
  public ModelAndView renderRemoveClause(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                         @PathVariable("clauseId") Integer clauseId,
                                         @ModelAttribute("form") ClauseForm form,
                                         AuthenticatedUserAccount authenticatedUserAccount) {

    return getRemoveClauseModelAndView(processingContext, clauseId);

  }

  @PostMapping("/remove-clause/{clauseId}")
  public ModelAndView postRemoveClause(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     PwaAppProcessingContext processingContext,
                                       @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                     @PathVariable("clauseId") Integer clauseId,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     RedirectAttributes redirectAttributes) {

    documentInstanceService.removeClause(clauseId, authenticatedUserAccount.getLinkedPerson());
    FlashUtils.success(redirectAttributes, "Clause removed");
    return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);
  }

  private ModelAndView getRemoveClauseModelAndView(PwaAppProcessingContext processingContext, Integer clauseId) {

    var cancelUrl = ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(processingContext.getPwaApplication().getId(), processingContext.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("documents/clauses/removeClause")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("sectionClauseView", documentInstanceService.getSectionClauseView(clauseId));

    String thisPage = "Remove clause";
    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, thisPage);

    return modelAndView;
  }


}
