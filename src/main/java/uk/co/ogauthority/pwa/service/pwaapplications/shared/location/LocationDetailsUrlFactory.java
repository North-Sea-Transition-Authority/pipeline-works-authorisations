package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.location.LocationDetailsDocumentsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class LocationDetailsUrlFactory {

  private final PwaApplicationType pwaApplicationType;
  private final Integer applicationId;

  public LocationDetailsUrlFactory(
      PwaApplicationType pwaApplicationType, Integer applicationId) {
    this.pwaApplicationType = pwaApplicationType;
    this.applicationId = applicationId;
  }

  public String getEditDocumentsUrl() {
    return ReverseRouter.route(on(LocationDetailsDocumentsController.class)
        .renderEditDocuments(pwaApplicationType, applicationId, null, null));
  }

  public String getFileDownloadUrl() {
    return ReverseRouter.route(on(LocationDetailsDocumentsController.class)
        // file id is full to allow templates to construct url as needed
        .handleDownload(pwaApplicationType, applicationId, null, null));
  }

}
