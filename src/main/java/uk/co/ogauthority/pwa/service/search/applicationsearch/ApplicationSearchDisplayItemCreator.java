package uk.co.ogauthority.pwa.service.search.applicationsearch;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@Service
public class ApplicationSearchDisplayItemCreator {

  public ApplicationSearchDisplayItem createDisplayItem(ApplicationDetailItemView applicationDetailItemView) {
    return new ApplicationSearchDisplayItem(applicationDetailItemView, this::createAccessUrl);

  }


  private String createAccessUrl(ApplicationDetailItemView applicationDetailItemView) {
    return CaseManagementUtils.routeCaseManagement(
        applicationDetailItemView.getPwaApplicationId(),
        applicationDetailItemView.getApplicationType()
    );
  }


}
