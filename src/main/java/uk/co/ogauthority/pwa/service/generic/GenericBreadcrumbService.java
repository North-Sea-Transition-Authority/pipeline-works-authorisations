package uk.co.ogauthority.pwa.service.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateController;
import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateSelectController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class GenericBreadcrumbService {

  public void fromDocTemplateSelect(ModelAndView modelAndView,
                                                   DocumentSpec documentSpec) {

    Map<String, String> breadcrumbs = docTemplateSelect();

    addAttrs(modelAndView, breadcrumbs, documentSpec.getDisplayName());

  }

  public void fromDocTemplateOverview(DocumentSpec documentSpec, ModelAndView modelAndView, String thisPage) {

    var breadcrumbs = docTemplateSelect();

    breadcrumbs.put(
        ReverseRouter.route(on(DocumentTemplateController.class).renderConsentDocEditor(documentSpec, null)),
        documentSpec.getDisplayName());

    addAttrs(modelAndView, breadcrumbs, thisPage);

  }

  private Map<String, String> docTemplateSelect() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(DocumentTemplateSelectController.class).getTemplatesForSelect(null)),
        "Document templates");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
