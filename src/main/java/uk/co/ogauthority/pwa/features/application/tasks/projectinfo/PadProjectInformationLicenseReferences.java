package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplications;

@Entity(name = "PAD_PROJECT_INFORMATION_LICENCE")
public class PadProjectInformationLicenseReferences {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "PAD_PROJECT_INFORMATION_ID")
  private PadProjectInformation padProjectInformation;

  @ManyToOne
  @JoinColumn(name = "PEARS_LICENSE_NUMBER")
  private PearsLicenceApplications pearsLicence;

  @CreationTimestamp
  private Instant createdTimestamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadProjectInformationLicenseReferences() {
  }

  public PadProjectInformationLicenseReferences(PadProjectInformation padProjectInformation,
                                                PearsLicenceApplications pearsLicence) {
    this.padProjectInformation = padProjectInformation;
    this.pearsLicence = pearsLicence;
  }

  public PadProjectInformation getPadProjectInformation() {
    return padProjectInformation;
  }

  public void setPadProjectInformation(
      PadProjectInformation padProjectInformation) {
    this.padProjectInformation = padProjectInformation;
  }

  public PearsLicenceApplications getPearsLicence() {
    return pearsLicence;
  }

  public void setPearsLicence(
      PearsLicenceApplications pearsLicence) {
    this.pearsLicence = pearsLicence;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
