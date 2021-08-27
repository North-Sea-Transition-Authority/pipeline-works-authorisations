package uk.co.ogauthority.pwa.model.view.consent;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;

public class ConsentFileView {

  private final PwaConsentApplicationDto pwaConsentApplicationDto;
  private final ConsultationRequestView consultationRequestView;

  public ConsentFileView(PwaConsentApplicationDto pwaConsentApplicationDto,
                         ConsultationRequestView consultationRequestView) {
    this.pwaConsentApplicationDto = pwaConsentApplicationDto;
    this.consultationRequestView = consultationRequestView;
  }

  public PwaConsentApplicationDto getPwaConsentApplicationDto() {
    return pwaConsentApplicationDto;
  }

  public ConsultationRequestView getConsultationRequestView() {
    return consultationRequestView;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentFileView that = (ConsentFileView) o;
    return Objects.equals(pwaConsentApplicationDto, that.pwaConsentApplicationDto)
        && Objects.equals(consultationRequestView, that.consultationRequestView);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaConsentApplicationDto, consultationRequestView);
  }

}
