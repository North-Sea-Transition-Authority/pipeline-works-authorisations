package uk.co.ogauthority.pwa.service.documents.templates;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class DocumentTemplateSelectUrlProvider {

  public String getEditUrl(DocumentSpec documentSpec) {

    return ReverseRouter.route(on(DocumentTemplateController.class)
        .renderConsentDocEditor(documentSpec, null));

  }

}
