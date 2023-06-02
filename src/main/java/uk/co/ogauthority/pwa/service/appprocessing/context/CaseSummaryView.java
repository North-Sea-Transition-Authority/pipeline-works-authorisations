package uk.co.ogauthority.pwa.service.appprocessing.context;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationDisplayUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.summary.controller.ApplicationSummaryController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseSummaryView {

  private final Integer pwaApplicationId;
  private final PwaApplicationType pwaApplicationType;
  private final String pwaApplicationTypeDisplay;
  private final String pwaApplicationRef;
  private final String holderNames;
  private final String fieldNames;
  private final String proposedStartDateDisplay;
  private final boolean fastTrackFlag;
  private final String caseOfficerName;
  private final Integer versionNo;
  private final String caseSummaryHeaderId;
  private final String masterPwaReference;

  public CaseSummaryView(Integer pwaApplicationId,
                         PwaApplicationType pwaApplicationType,
                         PwaResourceType resourceType,
                         String pwaApplicationRef,
                         String holderNames,
                         String fieldNames,
                         String proposedStartDateDisplay,
                         boolean fastTrackFlag,
                         String caseOfficerName,
                         Integer versionNo,
                         String caseSummaryHeaderId,
                         String masterPwaReference) {
    this.pwaApplicationId = pwaApplicationId;
    this.pwaApplicationType = pwaApplicationType;
    this.pwaApplicationTypeDisplay = PwaApplicationDisplayUtils.getApplicationTypeDisplay(pwaApplicationType, resourceType);
    this.pwaApplicationRef = pwaApplicationRef;
    this.holderNames = holderNames;
    this.fieldNames = fieldNames;
    this.proposedStartDateDisplay = proposedStartDateDisplay;
    this.fastTrackFlag = fastTrackFlag;
    this.caseOfficerName = caseOfficerName;
    this.versionNo = versionNo;
    this.caseSummaryHeaderId = caseSummaryHeaderId;
    this.masterPwaReference = masterPwaReference;
  }

  public static CaseSummaryView from(ApplicationDetailView detailViewItem, String caseSummaryHeaderId) {

    var appWorkAreaItem = new PwaApplicationWorkAreaItem(detailViewItem);

    String holders = appWorkAreaItem.getHolderColumn().stream()
        .map(WorkAreaColumnItemView::getValue)
        .collect(Collectors.joining(", "));

    String fields = String.join(", ", appWorkAreaItem.getOrderedFieldList());
    fields = fields.isBlank() ? null : fields;

    String proposedStartDateDisplay = Optional.ofNullable(detailViewItem.getPadProposedStart())
        .map(DateUtils::formatDate)
        .orElse(null);

    var appType = PwaApplicationType.resolveFromDisplayText(appWorkAreaItem.getApplicationTypeDisplay());

    return new CaseSummaryView(
        appWorkAreaItem.getPwaApplicationId(),
        appType,
        detailViewItem.getResourceType(),
        appWorkAreaItem.getApplicationReference(),
        holders,
        fields,
        proposedStartDateDisplay,
        appWorkAreaItem.wasSubmittedAsFastTrack(),
        appWorkAreaItem.getCaseOfficerName(),
        detailViewItem.getVersionNo(),
        caseSummaryHeaderId,
        appWorkAreaItem.getMasterPwaReference());
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public PwaApplicationType getPwaApplicationType() {
    return pwaApplicationType;
  }

  public String getPwaApplicationTypeDisplay() {
    return pwaApplicationTypeDisplay;
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

  public Integer getVersionNo() {
    return versionNo;
  }

  public String getCaseSummaryHeaderId() {
    return caseSummaryHeaderId;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  @SuppressWarnings("unused")
  // used in ftl template
  public String getAppSummaryUrl() {
    return ReverseRouter.route(on(ApplicationSummaryController.class).renderSummary(pwaApplicationId,
        pwaApplicationType, null, null, null, null));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CaseSummaryView that = (CaseSummaryView) o;
    return fastTrackFlag == that.fastTrackFlag && Objects.equals(pwaApplicationId,
        that.pwaApplicationId) && pwaApplicationType == that.pwaApplicationType && Objects.equals(
        pwaApplicationTypeDisplay, that.pwaApplicationTypeDisplay) && Objects.equals(pwaApplicationRef,
        that.pwaApplicationRef) && Objects.equals(holderNames, that.holderNames) && Objects.equals(
        fieldNames, that.fieldNames) && Objects.equals(proposedStartDateDisplay,
        that.proposedStartDateDisplay) && Objects.equals(caseOfficerName,
        that.caseOfficerName)
        && Objects.equals(versionNo, that.versionNo)
        && Objects.equals(caseSummaryHeaderId, that.caseSummaryHeaderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationId, pwaApplicationType, pwaApplicationTypeDisplay, pwaApplicationRef, holderNames,
        fieldNames, proposedStartDateDisplay, fastTrackFlag, caseOfficerName, versionNo, caseSummaryHeaderId);
  }
}
