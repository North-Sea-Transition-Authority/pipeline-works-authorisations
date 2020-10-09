package uk.co.ogauthority.pwa.service.workarea.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;

public class PwaApplicationWorkAreaItem extends ApplicationWorkAreaItem {

  public PwaApplicationWorkAreaItem(ApplicationDetailSearchItem applicationDetailSearchItem,
                                    Function<ApplicationDetailSearchItem, String> viewApplicationUrlProducer) {
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
