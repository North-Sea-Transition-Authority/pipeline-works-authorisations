package uk.co.ogauthority.pwa.service.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateSelectController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class GenericBreadcrumbService {

  public Map<String, String> fromDocTemplateSelect(ModelAndView modelAndView,
                                                   DocumentSpec documentSpec) {

    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(DocumentTemplateSelectController.class).getTemplatesForSelect(null)),
        "Document templates");

    addAttrs(modelAndView, breadcrumbs, documentSpec.getDisplayName());

    return breadcrumbs;

  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
