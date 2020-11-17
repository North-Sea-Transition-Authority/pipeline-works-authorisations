package uk.co.ogauthority.pwa.model.form.consultation;

import java.util.List;

public class ConsulteeAdviceView {

  private final String consulteeGroupName;

  private final ConsultationRequestView activeRequestView;

  private final List<ConsultationRequestView> historicRequestViews;

  public ConsulteeAdviceView(String consulteeGroupName,
                             ConsultationRequestView activeRequestView,
                             List<ConsultationRequestView> historicRequestViews) {
    this.consulteeGroupName = consulteeGroupName;
    this.activeRequestView = activeRequestView;
    this.historicRequestViews = historicRequestViews;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public ConsultationRequestView getActiveRequestView() {
    return activeRequestView;
  }

  public List<ConsultationRequestView> getHistoricRequestViews() {
    return historicRequestViews;
  }
}
