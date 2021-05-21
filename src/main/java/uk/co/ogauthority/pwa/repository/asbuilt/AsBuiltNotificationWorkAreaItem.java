package uk.co.ogauthority.pwa.repository.asbuilt;

import static uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem.STATUS_LABEL;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

public class AsBuiltNotificationWorkAreaItem {

  private final Integer asBuiltNotificationGroupId;
  private final String asBuiltNotificationGroupReference;
  private final LocalDate asBuiltNotificationDeadlineDate;
  private final Instant projectCompletionDateTimestamp;
  private final AsBuiltNotificationGroupStatus asBuiltNotificationGroupStatus;
  private final String projectName;
  private final Integer pwaId;
  private final String masterPwaReference;
  private final Integer consentId;
  private final AsBuiltNotificationGroupStatus status;
  private final List<String> pwaHolderNameList;

  private final String accessUrl;


  public AsBuiltNotificationWorkAreaItem(AsBuiltNotificationWorkareaView asBuiltNotificationWorkareaView) {
    this(asBuiltNotificationWorkareaView, "tempUrl");
  }

  public AsBuiltNotificationWorkAreaItem(AsBuiltNotificationWorkareaView asBuiltNotificationWorkareaView, String accessUrl) {
    this.asBuiltNotificationGroupId = asBuiltNotificationWorkareaView.getNgId();
    this.asBuiltNotificationGroupReference = asBuiltNotificationWorkareaView.getNgReference();
    this.asBuiltNotificationDeadlineDate = asBuiltNotificationWorkareaView.getDeadlineDate();
    this.projectCompletionDateTimestamp = asBuiltNotificationWorkareaView.getProjectCompletionDateTimestamp();
    this.asBuiltNotificationGroupStatus = asBuiltNotificationWorkareaView.getStatus();
    this.projectName = asBuiltNotificationWorkareaView.getProjectName();
    this.pwaId = asBuiltNotificationWorkareaView.getPwaId();
    this.masterPwaReference = asBuiltNotificationWorkareaView.getMasterPwaReference();
    this.consentId = asBuiltNotificationWorkareaView.getConsentId();
    this.status = asBuiltNotificationWorkareaView.getStatus();
    this.pwaHolderNameList = asBuiltNotificationWorkareaView.getPwaHolderNameList();
    this.accessUrl = accessUrl;
  }

  public Integer getAsBuiltNotificationGroupId() {
    return asBuiltNotificationGroupId;
  }

  public String getAsBuiltNotificationGroupReference() {
    return asBuiltNotificationGroupReference;
  }

  public String getAsBuiltNotificationDeadlineDateDisplay() {
    return Optional.ofNullable(this.asBuiltNotificationDeadlineDate)
        .map(WorkAreaUtils.WORK_AREA_DATE_FORMAT::format)
        .orElse(null);
  }

  public String getProjectCompletionDateDisplay() {
    return Optional.ofNullable(this.projectCompletionDateTimestamp)
        .map(WorkAreaUtils.WORK_AREA_DATE_FORMAT::format)
        .orElse(null);
  }

  public AsBuiltNotificationGroupStatus getAsBuiltNotificationGroupStatus() {
    return asBuiltNotificationGroupStatus;
  }

  public String getProjectName() {
    return projectName;
  }

  public Integer getPwaId() {
    return pwaId;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  public Integer getConsentId() {
    return consentId;
  }

  public AsBuiltNotificationGroupStatus getStatus() {
    return status;
  }

  public List<String> getPwaHolderNameList() {
    return pwaHolderNameList;
  }

  public String getAccessUrl() {
    return accessUrl;
  }

  public List<WorkAreaColumnItemView> getApplicationColumn() {
    var columnItemList = new ArrayList<WorkAreaColumnItemView>();

    columnItemList.add(
        WorkAreaColumnItemView.createLinkItem(
            this.asBuiltNotificationGroupReference,
            this.accessUrl
        ));

    columnItemList.add(
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, this.masterPwaReference)
    );

    return columnItemList;
  }

  public List<WorkAreaColumnItemView> getHolderColumn() {
    var columnItemList = new ArrayList<WorkAreaColumnItemView>();
    this.pwaHolderNameList.stream().forEachOrdered(holderName -> columnItemList.add(
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, holderName)
    ));

    return columnItemList;

  }

  public List<WorkAreaColumnItemView> getSummaryColumn() {

    var columnItemList = new ArrayList<WorkAreaColumnItemView>();

    columnItemList.add(WorkAreaColumnItemView.createLabelledItem("Project name", this.projectName));
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem("As-built deadline", this.getAsBuiltNotificationDeadlineDateDisplay()));
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem("Project completion date", this.getProjectCompletionDateDisplay()));

    return columnItemList;

  }

  public List<WorkAreaColumnItemView> getStatusColumn() {
    var columnItemList = new ArrayList<WorkAreaColumnItemView>();
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(STATUS_LABEL, this.getStatus().getDisplayName())
    );

    return columnItemList;
  }

}
