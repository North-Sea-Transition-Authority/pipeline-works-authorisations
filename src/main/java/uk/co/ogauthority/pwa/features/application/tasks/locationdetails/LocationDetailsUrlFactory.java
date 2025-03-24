package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class LocationDetailsUrlFactory {

  private final Integer applicationId;

  public LocationDetailsUrlFactory(PwaApplicationDetail detail) {
    this.applicationId = detail.getMasterPwaApplicationId();
  }

  public String getDocumentDownloadUrl() {
    return ReverseRouter.route(on(PadFileManagementRestController.class).download(applicationId, null));
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
    return Objects.equals(applicationId, that.applicationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId);
  }
}
