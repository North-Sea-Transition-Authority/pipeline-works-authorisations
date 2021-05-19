package uk.co.ogauthority.pwa.controller.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateClauseActionsUrlProvider;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;
import uk.co.ogauthority.pwa.service.generic.GenericBreadcrumbService;

@Controller
@RequestMapping("/document-templates/{documentSpec}")
public class DocumentTemplateController {

  private final GenericBreadcrumbService breadcrumbService;
  private final DocumentTemplateService documentTemplateService;

  @Autowired
  public DocumentTemplateController(GenericBreadcrumbService breadcrumbService,
                                    DocumentTemplateService documentTemplateService) {
    this.breadcrumbService = breadcrumbService;
    this.documentTemplateService = documentTemplateService;
  }

  @GetMapping
  public ModelAndView renderConsentDocEditor(@PathVariable("documentSpec") DocumentSpec documentSpec,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    var docView = documentTemplateService.getDocumentView(documentSpec);

    var modelAndView = new ModelAndView("documents/templates/documentTemplateEditor")
        .addObject("docView", docView)
        .addObject("clauseActionsUrlProvider", new DocumentTemplateClauseActionsUrlProvider())
        .addObject("documentSpec", documentSpec);

    breadcrumbService.fromDocTemplateSelect(modelAndView, documentSpec);

    return modelAndView;

  }

}
