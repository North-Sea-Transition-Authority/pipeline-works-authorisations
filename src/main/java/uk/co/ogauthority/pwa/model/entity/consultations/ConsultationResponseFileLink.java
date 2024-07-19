package uk.co.ogauthority.pwa.model.entity.consultations;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;

@Entity
@Table(name = "consultation_resp_file_links")
public class ConsultationResponseFileLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "consultation_resp_id")
  private ConsultationResponse consultationResponse;

  @OneToOne
  @JoinColumn(name = "af_id")
  private AppFile appFile;

  public ConsultationResponseFileLink() {

  }

  public ConsultationResponseFileLink(ConsultationResponse consultationResponse,
                                      AppFile appFile) {
    this.consultationResponse = consultationResponse;
    this.appFile = appFile;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ConsultationResponse getConsultationResponse() {
    return consultationResponse;
  }

  public void setConsultationResponse(ConsultationResponse consultationResponse) {
    this.consultationResponse = consultationResponse;
  }

  public AppFile getAppFile() {
    return appFile;
  }

  public void setAppFile(AppFile appFile) {
    this.appFile = appFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponseFileLink that = (ConsultationResponseFileLink) o;
    return Objects.equals(id, that.id)
        && Objects.equals(consultationResponse, that.consultationResponse)
        && Objects.equals(appFile, that.appFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, consultationResponse, appFile);
  }

}
