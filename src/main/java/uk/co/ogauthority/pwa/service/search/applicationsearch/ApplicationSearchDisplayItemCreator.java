package uk.co.ogauthority.pwa.service.search.applicationsearch;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;

@Service
public class ApplicationSearchDisplayItemCreator {

  public ApplicationSearchDisplayItem createDisplayItem(ApplicationDetailItemView applicationDetailItemView) {
    return new ApplicationSearchDisplayItem(applicationDetailItemView);
  }

}
