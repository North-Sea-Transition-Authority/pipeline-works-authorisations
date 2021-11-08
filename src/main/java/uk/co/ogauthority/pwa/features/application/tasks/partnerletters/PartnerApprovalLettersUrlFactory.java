package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.controller.PartnerLettersController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class PartnerApprovalLettersUrlFactory {

  private final Integer applicationId;
  private final PwaApplicationType applicationType;

  public PartnerApprovalLettersUrlFactory(PwaApplicationDetail detail) {
    this.applicationId = detail.getMasterPwaApplicationId();
    this.applicationType = detail.getPwaApplicationType();
  }

  public String getDocumentDownloadUrl() {
    return ReverseRouter.route(on(PartnerLettersController.class)
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
    PartnerApprovalLettersUrlFactory that = (PartnerApprovalLettersUrlFactory) o;
    return Objects.equals(applicationId, that.applicationId)
        && applicationType == that.applicationType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, applicationType);
  }
}
