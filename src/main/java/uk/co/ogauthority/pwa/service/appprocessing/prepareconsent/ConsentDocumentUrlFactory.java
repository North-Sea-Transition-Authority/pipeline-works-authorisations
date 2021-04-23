package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent.AppConsentDocController;
import uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent.ConsentReviewController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class ConsentDocumentUrlFactory {

  private final PwaApplication application;

  public ConsentDocumentUrlFactory(PwaApplication application) {
    this.application = application;
  }

  public String getRenderEditorUrl() {
    return ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(application.getId(), application.getApplicationType(), null, null));
  }

  public String getLoadDocumentUrl() {
    return ReverseRouter.route(on(AppConsentDocController.class)
        .postConsentDocEditor(application.getId(), application.getApplicationType(), null, null, null));
  }

  public String getReloadDocumentUrl() {
    return ReverseRouter.route(on(AppConsentDocController.class)
        .renderReloadDocument(application.getId(), application.getApplicationType(), null, null, null));
  }

  public String getSendForApprovalUrl() {
    return ReverseRouter.route(on(AppConsentDocController.class)
        .renderSendForApproval(application.getId(), application.getApplicationType(), null, null, null));
  }

  public String getDownloadUrl() {
    return ReverseRouter.route(on(AppConsentDocController.class)
        .downloadPdf(application.getId(), application.getApplicationType(), null, null));
  }

  public String getReturnToCaseOfficerUrl() {
    return ReverseRouter.route(on(ConsentReviewController.class)
        .renderReturnToCaseOfficer(application.getId(), application.getApplicationType(), null, null, null));
  }

  public String getIssueConsentUrl() {
    return ReverseRouter.route(on(ConsentReviewController.class)
        .renderIssueConsent(application.getId(), application.getApplicationType(), null, null));
  }

}
