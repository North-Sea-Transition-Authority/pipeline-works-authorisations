package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "ped_licence_applications")
public class PearsLicenceApplications {

  @Id
  @Column(name = "licence_application_id", insertable = false, updatable = false)
  private Integer applicationId;

  @Column(name = "application_ref")
  private String applicationReference;

  public PearsLicenceApplications() {
  }

  @VisibleForTesting
  public PearsLicenceApplications(Integer applicationId, String applicationReference) {
    this.applicationId = applicationId;
    this.applicationReference = applicationReference;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public String getId() {
    return String.valueOf(applicationId);
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getName() {
    return applicationReference;
  }

  public void setApplicationReference(String applicationReference) {
    this.applicationReference = applicationReference;
  }

  public Boolean isValid() {
    return true;
  }
}
