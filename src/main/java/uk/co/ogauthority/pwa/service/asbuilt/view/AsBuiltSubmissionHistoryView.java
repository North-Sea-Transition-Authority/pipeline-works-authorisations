package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.util.List;

public class AsBuiltSubmissionHistoryView {

  private final AsBuiltNotificationView latestSubmissionView;
  private final List<AsBuiltNotificationView> historicalSubmissionViews;

  AsBuiltSubmissionHistoryView(AsBuiltNotificationView latestSubmissionView,
                               List<AsBuiltNotificationView> historicalSubmissionViews) {
    this.latestSubmissionView = latestSubmissionView;
    this.historicalSubmissionViews = historicalSubmissionViews;
  }

  public AsBuiltNotificationView getLatestSubmissionView() {
    return latestSubmissionView;
  }

  public List<AsBuiltNotificationView> getHistoricalSubmissionViews() {
    return historicalSubmissionViews;
  }

}