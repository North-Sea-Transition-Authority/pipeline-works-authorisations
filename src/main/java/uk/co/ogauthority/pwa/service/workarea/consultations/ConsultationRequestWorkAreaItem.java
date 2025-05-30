package uk.co.ogauthority.pwa.service.workarea.consultations;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

public class ConsultationRequestWorkAreaItem extends ApplicationWorkAreaItem {

  private final Integer consultationRequestId;

  private final Integer consulteeGroupId;

  private final String consulteeGroupName;

  private final String consulteeGroupAbbr;

  private final String consultationRequestDeadlineDateTime;

  private final String consultationRequestStatus;

  private final String assignedResponderName;

  public ConsultationRequestWorkAreaItem(ConsultationRequestSearchItem requestSearchItem) {

    super(requestSearchItem.getApplicationDetailView());

    this.consultationRequestId = requestSearchItem.getConsultationRequestId();

    this.consulteeGroupId = requestSearchItem.getConsulteeGroupId();
    this.consulteeGroupName = requestSearchItem.getConsulteeGroupName();
    this.consulteeGroupAbbr = requestSearchItem.getConsulteeGroupAbbr();
    this.consultationRequestDeadlineDateTime = WorkAreaUtils.WORK_AREA_DATETIME_FORMAT
        .format(requestSearchItem.getDeadlineDate());
    this.consultationRequestStatus = requestSearchItem.getConsultationRequestStatus().getDisplayName();
    this.assignedResponderName = requestSearchItem.getAssignedResponderName();

  }

  public Integer getConsultationRequestId() {
    return consultationRequestId;
  }

  public Integer getConsulteeGroupId() {
    return consulteeGroupId;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public String getConsulteeGroupAbbr() {
    return consulteeGroupAbbr;
  }

  public String getConsultationRequestDeadlineDateTime() {
    return consultationRequestDeadlineDateTime;
  }

  public String getConsultationRequestStatus() {
    return consultationRequestStatus;
  }

  public String getAssignedResponderName() {
    return assignedResponderName;
  }

  @Override
  public List<WorkAreaColumnItemView> getApplicationStatusColumn() {
    var columnItemList = new ArrayList<WorkAreaColumnItemView>();
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(STATUS_LABEL, this.consultationRequestStatus)
    );

    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(
            "Due date", this.consultationRequestDeadlineDateTime)
    );

    var consulteeGroupAbbreviation = this.consulteeGroupAbbr != null
        ? String.format(" (%s)", this.consulteeGroupAbbr)
        : "";

    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem(
            "Consultee", this.consulteeGroupName + consulteeGroupAbbreviation)
    );

    if (this.assignedResponderName != null) {
      columnItemList.add(
          WorkAreaColumnItemView.createLabelledItem(
              "Responder", this.assignedResponderName)
      );
    }

    createFastTrackColumnItem().ifPresent(columnItemList::add);

    return columnItemList;
  }
}
