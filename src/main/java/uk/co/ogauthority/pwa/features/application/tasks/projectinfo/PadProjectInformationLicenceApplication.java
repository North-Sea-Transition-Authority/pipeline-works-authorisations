package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransactions;

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
  private PearsLicenceTransactions pearsLicenceTransactions;

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
                                                 PearsLicenceTransactions pearsLicenceTransactions) {
    this.padProjectInformation = padProjectInformation;
    this.pearsLicenceTransactions = pearsLicenceTransactions;
  }

  public PadProjectInformation getPadProjectInformation() {
    return padProjectInformation;
  }

  public void setPadProjectInformation(
      PadProjectInformation padProjectInformation) {
    this.padProjectInformation = padProjectInformation;
  }

  public PearsLicenceTransactions getPearsLicenceApplication() {
    return pearsLicenceTransactions;
  }

  public void setPearsLicenceApplications(
      PearsLicenceTransactions pearsLicenceTransactions) {
    this.pearsLicenceTransactions = pearsLicenceTransactions;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
