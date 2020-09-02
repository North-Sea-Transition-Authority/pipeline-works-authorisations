package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Optional;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseSummaryView {

  private final String pwaApplicationType;
  private final String pwaApplicationRef;
  private final String holderNames;
  private final String fieldNames;
  private final String proposedStartDateDisplay;
  private final boolean fastTrackFlag;
  private final String caseOfficerName;

  public CaseSummaryView(String pwaApplicationType,
                         String pwaApplicationRef,
                         String holderNames,
                         String fieldNames,
                         String proposedStartDateDisplay,
                         boolean fastTrackFlag,
                         String caseOfficerName) {
    this.pwaApplicationType = pwaApplicationType;
    this.pwaApplicationRef = pwaApplicationRef;
    this.holderNames = holderNames;
    this.fieldNames = fieldNames;
    this.proposedStartDateDisplay = proposedStartDateDisplay;
    this.fastTrackFlag = fastTrackFlag;
    this.caseOfficerName = caseOfficerName;
  }

  public static CaseSummaryView from(ApplicationDetailSearchItem detailSearchItem) {

    // TODO PWA-835 link up app summary screen to case management
    var appWorkAreaItem = new PwaApplicationWorkAreaItem(detailSearchItem, (detailSearchItem1) -> "#");

    String holders = appWorkAreaItem.getHolderColumn().stream()
        .map(WorkAreaColumnItemView::getValue)
        .collect(Collectors.joining(", "));

    String fields = String.join(", ", appWorkAreaItem.getOrderedFieldList());
    fields = fields.isBlank() ? null : fields;

    String proposedStartDateDisplay = Optional.ofNullable(detailSearchItem.getPadProposedStart())
        .map(DateUtils::formatDate)
        .orElse(null);

    return new CaseSummaryView(
        appWorkAreaItem.getApplicationTypeDisplay(),
        appWorkAreaItem.getApplicationReference(),
        holders,
        fields,
        proposedStartDateDisplay,
        appWorkAreaItem.wasSubmittedAsFastTrack(),
        appWorkAreaItem.getCaseOfficerName()
    );

  }

  public String getPwaApplicationType() {
    return pwaApplicationType;
  }

  public String getPwaApplicationRef() {
    return pwaApplicationRef;
  }

  public String getHolderNames() {
    return holderNames;
  }

  public String getFieldNames() {
    return fieldNames;
  }

  public String getProposedStartDateDisplay() {
    return proposedStartDateDisplay;
  }

  public boolean isFastTrackFlag() {
    return fastTrackFlag;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }
}
