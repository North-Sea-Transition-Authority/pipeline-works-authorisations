package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.controller.appprocessing.decision.AppConsentDocController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.documents.DocumentInstanceRedirectUtils;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/documents/{documentTemplateMnem}")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class DocumentInstanceController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  public DocumentInstanceController(AppProcessingBreadcrumbService breadcrumbService,
                                    ControllerHelperService controllerHelperService,
                                    DocumentInstanceService documentInstanceService) {
    this.breadcrumbService = breadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.documentInstanceService = documentInstanceService;
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

    var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddAfter);

    return getAddClauseModelAndView(processingContext);

  }

  private ModelAndView getAddClauseModelAndView(PwaAppProcessingContext processingContext) {

    var cancelUrl = ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(processingContext.getPwaApplication().getId(), processingContext.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("documents/clauses/addEditClause")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl);

    breadcrumbService.fromConsentDocument(processingContext.getPwaApplication(), modelAndView, "Add clause");

    return modelAndView;

  }

  @PostMapping("/add-clause-after/{clauseIdToAddAfter}")
  public ModelAndView postAddClauseAfter(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                         @PathVariable("clauseIdToAddAfter") Integer clauseIdToAddAfter,
                                         @Valid @ModelAttribute("form") ClauseForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddClauseModelAndView(processingContext), () -> {

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

    var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseIdToAddBefore);

    return getAddClauseModelAndView(processingContext);

  }

  @PostMapping("/add-clause-before/{clauseIdToAddBefore}")
  public ModelAndView postAddClauseBefore(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                          @PathVariable("clauseIdToAddBefore") Integer clauseIdToAddBefore,
                                          @Valid @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddClauseModelAndView(processingContext), () -> {

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

    var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);

    return getAddClauseModelAndView(processingContext);

  }

  @PostMapping("/add-sub-clause/{clauseId}")
  public ModelAndView postAddSubClauseFor(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @PathVariable("documentTemplateMnem") DocumentTemplateMnem documentTemplateMnem,
                                          @PathVariable("clauseId") Integer clauseId,
                                          @Valid @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddClauseModelAndView(processingContext), () -> {

      var clauseVersion = documentInstanceService.getInstanceClauseVersionByClauseIdOrThrow(clauseId);

      documentInstanceService.addSubClause(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

      FlashUtils.success(redirectAttributes, "Clause added");

      return DocumentInstanceRedirectUtils.getRedirect(processingContext.getPwaApplication(), documentTemplateMnem);

    });

  }

}
