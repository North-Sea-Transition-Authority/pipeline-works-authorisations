package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.component.AddToListComponent;

@Immutable
@Entity
@Table(name = "ped_licence_applications")
public class PearsLicenceApplication implements AddToListComponent {

  @Id
  @Column(name = "licence_application_id", insertable = false, updatable = false)
  private Integer applicationId;

  @Column(name = "application_ref")
  private String applicationReference;

  public PearsLicenceApplication() {
  }

  @VisibleForTesting
  public PearsLicenceApplication(Integer applicationId, String applicationReference) {
    this.applicationId = applicationId;
    this.applicationReference = applicationReference;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public void setApplicationReference(String applicationReference) {
    this.applicationReference = applicationReference;
  }

  @Override
  public String getId() {
    return String.valueOf(applicationId);
  }

  @Override
  public String getName() {
    return applicationReference;
  }

  @Override
  public Boolean isValid() {
    return true;
  }
}
