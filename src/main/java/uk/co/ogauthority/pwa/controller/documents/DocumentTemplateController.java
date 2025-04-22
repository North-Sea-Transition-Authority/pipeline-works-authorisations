package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.documents.clauses.ClauseFormValidator;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateClauseActionsUrlProvider;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.generic.GenericBreadcrumbService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.FlashUtils;

@Controller
@RequestMapping("/document-templates/{documentSpec}")
@HasAnyRole(teamType = TeamType.REGULATOR, roles = {Role.TEMPLATE_CLAUSE_MANAGER})
public class DocumentTemplateController {

  private final GenericBreadcrumbService breadcrumbService;
  private final DocumentTemplateService documentTemplateService;
  private final ClauseFormValidator clauseFormValidator;
  private final ControllerHelperService controllerHelperService;
  private final MailMergeService mailMergeService;

  @Autowired
  public DocumentTemplateController(GenericBreadcrumbService breadcrumbService,
                                    DocumentTemplateService documentTemplateService,
                                    ClauseFormValidator clauseFormValidator,
                                    ControllerHelperService controllerHelperService,
                                    MailMergeService mailMergeService) {
    this.breadcrumbService = breadcrumbService;
    this.documentTemplateService = documentTemplateService;
    this.clauseFormValidator = clauseFormValidator;
    this.controllerHelperService = controllerHelperService;
    this.mailMergeService = mailMergeService;
  }

  @GetMapping
  public ModelAndView renderConsentDocEditor(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    var docView = documentTemplateService.getDocumentView(documentSpec);

    mailMergeService.mailMerge(docView, DocGenType.PREVIEW);

    var modelAndView = new ModelAndView("documents/templates/documentTemplateEditor")
        .addObject("docView", docView)
        .addObject("clauseActionsUrlProvider", new DocumentTemplateClauseActionsUrlProvider(documentSpec))
        .addObject("documentSpec", documentSpec)
        .addObject("docTemplateEditorHeaderId", DocumentTemplateService.DOC_TEMPLATE_EDITOR_HEADER_ID);

    breadcrumbService.fromDocTemplateSelect(modelAndView, documentSpec);

    return modelAndView;

  }

  @GetMapping("/add-clause-after/{clauseIdToAddAfter}")
  public ModelAndView renderAddClauseAfter(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                           @PathVariable("clauseIdToAddAfter") Integer clauseIdToAddAfter,
                                           @ModelAttribute("form") ClauseForm form,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseIdToAddAfter);

    return getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD);

  }

  private ModelAndView getAddEditClauseModelAndView(DocumentSpec documentSpec, ScreenActionType screenActionType) {

    var cancelUrl = ReverseRouter.route(on(DocumentTemplateController.class)
        .renderConsentDocEditor(documentSpec, null));

    var modelAndView = new ModelAndView("documents/clauses/addEditClause")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("actionType", screenActionType);

    String thisPage = screenActionType.getActionText() + " clause";
    breadcrumbService.fromDocTemplateOverview(documentSpec, modelAndView, thisPage);

    var docSource = new TemplateDocumentSource(documentSpec);

    var mergeFieldNames = mailMergeService
        .getMailMergeFieldsForDocumentSource(docSource).stream()
        .map(MailMergeField::getDisplayString)
        .collect(Collectors.toList());

    modelAndView.addObject("mergeFieldNames", mergeFieldNames);

    return modelAndView;

  }

  private ModelAndView getAddEditClauseModelAndView(DocumentSpec documentSpec,
                                                    ScreenActionType screenActionType,
                                                    ClauseForm form) {
    return getAddEditClauseModelAndView(documentSpec, screenActionType)
        .addObject("form", form);
  }

  @PostMapping("/add-clause-after/{clauseIdToAddAfter}")
  public ModelAndView postAddClauseAfter(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                         @PathVariable("clauseIdToAddAfter") Integer clauseIdToAddAfter,
                                         @ModelAttribute("form") ClauseForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    var docSource = new TemplateDocumentSource(documentSpec);

    clauseFormValidator.validate(form, bindingResult, docSource);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD), () -> {

          var clauseVersion = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseIdToAddAfter);

          documentTemplateService.addClauseAfter(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return getOverviewRedirect(documentSpec);

        });

  }

  @GetMapping("/add-clause-before/{clauseIdToAddBefore}")
  public ModelAndView renderAddClauseBefore(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                            @PathVariable("clauseIdToAddBefore") Integer clauseIdToAddBefore,
                                            @ModelAttribute("form") ClauseForm form,
                                            AuthenticatedUserAccount authenticatedUserAccount) {

    documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseIdToAddBefore);

    return getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD);

  }

  @PostMapping("/add-clause-before/{clauseIdToAddBefore}")
  public ModelAndView postAddClauseBefore(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                          @PathVariable("clauseIdToAddBefore") Integer clauseIdToAddBefore,
                                          @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    var docSource = new TemplateDocumentSource(documentSpec);

    clauseFormValidator.validate(form, bindingResult, docSource);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD), () -> {

          var clauseVersion = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseIdToAddBefore);

          documentTemplateService.addClauseBefore(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return getOverviewRedirect(documentSpec);

        });

  }

  @GetMapping("/add-sub-clause/{clauseId}")
  public ModelAndView renderAddSubClauseFor(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                            @PathVariable("clauseId") Integer clauseId,
                                            @ModelAttribute("form") ClauseForm form,
                                            AuthenticatedUserAccount authenticatedUserAccount) {

    documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseId);

    return getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD);

  }

  @PostMapping("/add-sub-clause/{clauseId}")
  public ModelAndView postAddSubClauseFor(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                          @PathVariable("clauseId") Integer clauseId,
                                          @ModelAttribute("form") ClauseForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          RedirectAttributes redirectAttributes) {

    var docSource = new TemplateDocumentSource(documentSpec);

    clauseFormValidator.validate(form, bindingResult, docSource);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(documentSpec, ScreenActionType.ADD), () -> {

          var clauseVersion = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseId);

          documentTemplateService.addSubClause(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause added");

          return getOverviewRedirect(documentSpec);

        });

  }

  @GetMapping("/edit-clause/{clauseId}")
  public ModelAndView renderEditClause(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                       @PathVariable("clauseId") Integer clauseId,
                                       @ModelAttribute("form") ClauseForm form,
                                       AuthenticatedUserAccount authenticatedUserAccount) {

    var clauseVersion = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseId);
    form.setName(clauseVersion.getName());
    form.setText(clauseVersion.getText());

    return getAddEditClauseModelAndView(documentSpec, ScreenActionType.EDIT, form);

  }

  @PostMapping("/edit-clause/{clauseId}")
  public ModelAndView postEditClause(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                     @PathVariable("clauseId") Integer clauseId,
                                     @ModelAttribute("form") ClauseForm form,
                                     BindingResult bindingResult,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     RedirectAttributes redirectAttributes) {

    var docSource = new TemplateDocumentSource(documentSpec);

    clauseFormValidator.validate(form, bindingResult, docSource);

    return controllerHelperService
        .checkErrorsAndRedirect(bindingResult, getAddEditClauseModelAndView(documentSpec, ScreenActionType.EDIT, form), () -> {

          var clauseVersion = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(clauseId);

          documentTemplateService.editClause(clauseVersion, form, authenticatedUserAccount.getLinkedPerson());

          FlashUtils.success(redirectAttributes, "Clause updated");

          return getOverviewRedirect(documentSpec);

        });

  }

  @GetMapping("/remove-clause/{clauseId}")
  public ModelAndView renderRemoveClause(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                         @PathVariable("clauseId") Integer clauseId,
                                         AuthenticatedUserAccount authenticatedUserAccount) {
    return getRemoveClauseModelAndView(documentSpec, clauseId);
  }

  @PostMapping("/remove-clause/{clauseId}")
  public ModelAndView postRemoveClause(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                       @PathVariable("clauseId") Integer clauseId,
                                       AuthenticatedUserAccount authenticatedUserAccount,
                                       RedirectAttributes redirectAttributes) {

    documentTemplateService.removeClause(clauseId, authenticatedUserAccount.getLinkedPerson());
    FlashUtils.success(redirectAttributes, "Clause removed");
    return getOverviewRedirect(documentSpec);

  }

  public ModelAndView getOverviewRedirect(DocumentSpec documentSpec) {

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .renderConsentDocEditor(documentSpec, null));

  }

  private ModelAndView getRemoveClauseModelAndView(DocumentSpec documentSpec, Integer clauseId) {

    var cancelUrl = ReverseRouter.route(on(DocumentTemplateController.class)
        .renderConsentDocEditor(documentSpec, null));

    var docView = documentTemplateService.getDocumentView(documentSpec);

    var modelAndView = new ModelAndView("documents/clauses/removeClause")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("sectionClauseView", docView.getSectionClauseView(clauseId));

    String thisPage = "Remove clause";
    breadcrumbService.fromDocTemplateOverview(documentSpec, modelAndView, thisPage);

    return modelAndView;

  }

}
