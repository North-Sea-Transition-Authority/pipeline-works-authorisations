package uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates;


public class ApplicationUpdateSummaryView {


  private final String requestReason;

  private final String deadlineTimestamp;

  private final String requestedTimestamp;

  private final String responseByPersonName;

  private final String responseTimestamp;

  private final String responseType;
  private final String responseOtherChanges;


  private ApplicationUpdateSummaryView(String requestReason,
                                       String deadlineTimestamp,
                                       String requestedTimestamp,
                                       String responseByPersonName,
                                       String responseTimestamp,
                                       String responseOtherChanges) {
    this.requestReason = requestReason;
    this.deadlineTimestamp = deadlineTimestamp;
    this.requestedTimestamp = requestedTimestamp;
    this.responseByPersonName = responseByPersonName;
    this.responseTimestamp = responseTimestamp;
    this.responseType = responseOtherChanges == null ? "Requested changes only" : "Other changes";
    this.responseOtherChanges = responseOtherChanges;
  }

  public static ApplicationUpdateSummaryView from(ApplicationUpdateRequestView view,
                                                  String responseByPersonName) {
    return new ApplicationUpdateSummaryView(
        view.getRequestReason(),
        view.getDeadlineTimestampDisplay(),
        view.getRequestedTimestampDisplay(),
        responseByPersonName,
        view.getResponseTimestampDisplay(),
        view.getResponseOtherChanges()
    );

  }

  public String getRequestReason() {
    return requestReason;
  }

  public String getDeadlineTimestamp() {
    return deadlineTimestamp;
  }

  public String getRequestedTimestamp() {
    return requestedTimestamp;
  }

  public String getResponseByPersonName() {
    return responseByPersonName;
  }

  public String getResponseTimestamp() {
    return responseTimestamp;
  }

  public String getResponseType() {
    return responseType;
  }

  public String getResponseOtherChanges() {
    return responseOtherChanges;
  }
}
