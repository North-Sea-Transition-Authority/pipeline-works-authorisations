package uk.co.ogauthority.pwa.model.form.consultation;

import java.util.ArrayList;
import java.util.List;

public class ConsulteeGroupRequestsView {

  private ConsultationRequestView currentRequest;
  private List<ConsultationRequestView> historicalRequests = new ArrayList<>();


  public ConsultationRequestView getCurrentRequest() {
    return currentRequest;
  }

  public void setCurrentRequest(ConsultationRequestView currentRequest) {
    this.currentRequest = currentRequest;
  }

  public List<ConsultationRequestView> getHistoricalRequests() {
    return historicalRequests;
  }

  public void setHistoricalRequests(
      List<ConsultationRequestView> historicalRequests) {
    this.historicalRequests = historicalRequests;
  }

  public void addHistoricalRequest(
      ConsultationRequestView historicalRequestViews) {
    this.historicalRequests.add(historicalRequestViews);
  }
}
