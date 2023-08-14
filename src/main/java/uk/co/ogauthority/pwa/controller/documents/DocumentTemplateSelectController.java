package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.termsandconditions.controller.TermsAndConditionsManagementController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateSelectUrlProvider;

@Controller
@RequestMapping("/document-templates/select")
public class DocumentTemplateSelectController {

  @GetMapping
  public ModelAndView getTemplatesForSelect(AuthenticatedUserAccount authenticatedUserAccount) {

    var templateOptions = DocumentSpec.stream()
        .collect(Collectors.toList());

    return new ModelAndView("documents/templates/documentTemplateSelect")
        .addObject("documentTemplates", templateOptions)
        .addObject("urlProvider", new DocumentTemplateSelectUrlProvider())
        .addObject("tcUrl", ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null,
                0,
                null)));

  }

}
