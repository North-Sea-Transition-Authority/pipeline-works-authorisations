package uk.co.ogauthority.pwa.service.appprocessing.context;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.util.LinkedMultiValueMap;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationDisplayUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.summary.controller.ApplicationSummaryController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseSummaryView {

  private final Integer pwaApplicationId;
  private final PwaApplicationType pwaApplicationType;
  private final PwaResourceType pwaResourceType;
  private final String pwaApplicationTypeDisplay;
  private final String pwaApplicationRef;
  private final String holderNames;
  private final String areaNames;
  private final String proposedStartDateDisplay;
  private final boolean fastTrackFlag;
  private final String caseOfficerName;
  private final Integer versionNo;
  private final String caseSummaryHeaderId;
  private final String masterPwaReference;
  private final Integer masterPwaId;

  public CaseSummaryView(Integer pwaApplicationId,
                         PwaApplicationType pwaApplicationType,
                         PwaResourceType resourceType,
                         String pwaApplicationRef,
                         String holderNames,
                         String areaNames,
                         String proposedStartDateDisplay,
                         boolean fastTrackFlag,
                         String caseOfficerName,
                         Integer versionNo,
                         String caseSummaryHeaderId,
                         String masterPwaReference,
                         Integer masterPwaId) {
    this.pwaApplicationId = pwaApplicationId;
    this.pwaApplicationType = pwaApplicationType;
    this.pwaResourceType = resourceType;
    this.pwaApplicationTypeDisplay = PwaApplicationDisplayUtils.getApplicationTypeDisplay(pwaApplicationType, resourceType);
    this.pwaApplicationRef = pwaApplicationRef;
    this.holderNames = holderNames;
    this.areaNames = areaNames;
    this.proposedStartDateDisplay = proposedStartDateDisplay;
    this.fastTrackFlag = fastTrackFlag;
    this.caseOfficerName = caseOfficerName;
    this.versionNo = versionNo;
    this.caseSummaryHeaderId = caseSummaryHeaderId;
    this.masterPwaReference = masterPwaReference;
    this.masterPwaId = masterPwaId;
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
        appWorkAreaItem.getMasterPwaReference(),
        detailViewItem.getPwaId());
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

  public String getAreaNames() {
    return areaNames;
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

  public Integer getMasterPwaId() {
    return masterPwaId;
  }

  public PwaResourceType getPwaResourceType() {
    return pwaResourceType;
  }

  @SuppressWarnings("unused")
  // used in ftl template
  public String getAppSummaryUrl() {
    return ReverseRouter.route(on(ApplicationSummaryController.class).renderSummary(pwaApplicationId,
        pwaApplicationType, null, null, null, null));
  }

  @SuppressWarnings("unused")
  // used in ftl template
  public String getViewMasterPwaUrlIfVariation() {
    if (pwaApplicationType.equals(PwaApplicationType.INITIAL)) {
      return null;
    }

    return ReverseRouter.routeWithQueryParamMap(on(PwaViewController.class)
        .renderViewPwa(getMasterPwaId(), PwaViewTab.PIPELINES, null, null, null),
        new LinkedMultiValueMap<>(Map.of("showBreadcrumbs", List.of("false"))));
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
    return fastTrackFlag == that.fastTrackFlag
        && Objects.equals(pwaApplicationId, that.pwaApplicationId)
        && Objects.equals(pwaApplicationType, that.pwaApplicationType)
        && Objects.equals(pwaResourceType, that.pwaResourceType)
        && Objects.equals(pwaApplicationTypeDisplay, that.pwaApplicationTypeDisplay)
        && Objects.equals(pwaApplicationRef, that.pwaApplicationRef)
        && Objects.equals(holderNames, that.holderNames)
        && Objects.equals(areaNames, that.areaNames)
        && Objects.equals(proposedStartDateDisplay, that.proposedStartDateDisplay)
        && Objects.equals(caseOfficerName, that.caseOfficerName)
        && Objects.equals(versionNo, that.versionNo)
        && Objects.equals(caseSummaryHeaderId, that.caseSummaryHeaderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationId, pwaApplicationType, pwaResourceType, pwaApplicationTypeDisplay, pwaApplicationRef, holderNames,
        areaNames, proposedStartDateDisplay, fastTrackFlag, caseOfficerName, versionNo, caseSummaryHeaderId);
  }
}
