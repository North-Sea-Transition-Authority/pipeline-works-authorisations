package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;

/**
 * Represents a single item to be displayed in the application search results.
 */
public class ApplicationSearchDisplayItem extends ApplicationWorkAreaItem {

  ApplicationSearchDisplayItem(ApplicationDetailItemView applicationDetailSearchItem,
                               Function<ApplicationDetailItemView, String> viewApplicationUrlProducer) {
    super(applicationDetailSearchItem, viewApplicationUrlProducer.apply(applicationDetailSearchItem));
  }

  @Override
  public List<WorkAreaColumnItemView> getApplicationStatusColumn() {
    var columnItemList = new ArrayList<WorkAreaColumnItemView>();
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(STATUS_LABEL, this.getApplicationStatusDisplay())
    );

    if (!StringUtils.isEmpty(this.getCaseOfficerName())) {
      columnItemList.add(
          WorkAreaColumnItemView.createLabelledItem(CASE_OFFICER_DISPLAY_LABEL, this.getCaseOfficerName())
      );
    }

    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(DEFAULT_APP_STATUS_SET_LABEL, this.getFormattedStatusSetDatetime())
    );

    createFastTrackColumnItem().ifPresent(columnItemList::add);

    return columnItemList;
  }
}
