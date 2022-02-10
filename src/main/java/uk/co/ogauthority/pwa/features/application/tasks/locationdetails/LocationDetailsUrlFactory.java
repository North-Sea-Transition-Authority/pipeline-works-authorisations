package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.controller.LocationDetailsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class LocationDetailsUrlFactory {

  private final Integer applicationId;
  private final PwaApplicationType applicationType;

  public LocationDetailsUrlFactory(PwaApplicationDetail detail) {
    this.applicationId = detail.getMasterPwaApplicationId();
    this.applicationType = detail.getPwaApplicationType();
  }

  public String getDocumentDownloadUrl() {
    return ReverseRouter.route(on(LocationDetailsController.class)
        .handleDownload(applicationType, applicationId, null, null));
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationDetailsUrlFactory that = (LocationDetailsUrlFactory) o;
    return Objects.equals(applicationId, that.applicationId)
        && applicationType == that.applicationType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, applicationType);
  }
}
