package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransaction;

@Entity(name = "pad_project_information_licence_applications")
public class PadProjectInformationLicenceApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_project_information_id")
  private PadProjectInformation padProjectInformation;

  @ManyToOne
  @JoinColumn(name = "pears_licence_application_number")
  private PearsLicenceTransaction pearsLicenceTransaction;

  @CreationTimestamp
  private Instant createdTimestamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadProjectInformationLicenceApplication() {
  }

  public PadProjectInformationLicenceApplication(PadProjectInformation padProjectInformation,
                                                 PearsLicenceTransaction pearsLicenceTransaction) {
    this.padProjectInformation = padProjectInformation;
    this.pearsLicenceTransaction = pearsLicenceTransaction;
  }

  public PadProjectInformation getPadProjectInformation() {
    return padProjectInformation;
  }

  public void setPadProjectInformation(
      PadProjectInformation padProjectInformation) {
    this.padProjectInformation = padProjectInformation;
  }

  public PearsLicenceTransaction getPearsLicenceApplication() {
    return pearsLicenceTransaction;
  }

  public void setPearsLicenceApplications(
      PearsLicenceTransaction pearsLicenceTransaction) {
    this.pearsLicenceTransaction = pearsLicenceTransaction;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
