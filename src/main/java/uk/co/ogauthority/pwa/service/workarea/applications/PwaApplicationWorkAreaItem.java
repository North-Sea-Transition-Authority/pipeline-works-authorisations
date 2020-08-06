package uk.co.ogauthority.pwa.service.workarea.applications;

import java.util.function.Function;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;

public class PwaApplicationWorkAreaItem extends ApplicationWorkAreaItem {

  public PwaApplicationWorkAreaItem(ApplicationDetailSearchItem applicationDetailSearchItem,
                                    Function<ApplicationDetailSearchItem, String> viewApplicationUrlProducer) {
    super(applicationDetailSearchItem, viewApplicationUrlProducer.apply(applicationDetailSearchItem));
  }

}
