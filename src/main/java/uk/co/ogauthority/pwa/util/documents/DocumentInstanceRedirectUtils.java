package uk.co.ogauthority.pwa.util.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.controller.AppConsentDocController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class DocumentInstanceRedirectUtils {

  private DocumentInstanceRedirectUtils() {
    throw new AssertionError();
  }

  public static ModelAndView getRedirect(PwaApplication application, DocumentTemplateMnem mnem) {
    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(application.getId(), application.getApplicationType(), null, null));
  }

}
