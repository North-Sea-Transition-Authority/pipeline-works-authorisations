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
  @Column(name = "transaction_id", insertable = false, updatable = false)
  private Integer transactionId;

  @Column(name = "transaction_reference")
  private String transactionReference;

  public PearsLicenceApplication() {
  }

  @VisibleForTesting
  public PearsLicenceApplication(Integer applicationId, String applicationReference) {
    this.transactionId = applicationId;
    this.transactionReference = applicationReference;
  }

  public Integer getTransactionId() {
    return transactionId;
  }

  public PearsLicenceApplication setTransactionId(Integer transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public String getTransactionReference() {
    return transactionReference;
  }

  public PearsLicenceApplication setTransactionReference(String transactionReference) {
    this.transactionReference = transactionReference;
    return this;
  }

  @Override
  public String getId() {
    return String.valueOf(transactionId);
  }

  @Override
  public String getName() {
    return transactionReference;
  }

  @Override
  public Boolean isValid() {
    return true;
  }
}
