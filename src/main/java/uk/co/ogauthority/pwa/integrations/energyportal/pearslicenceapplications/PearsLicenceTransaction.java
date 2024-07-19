package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.component.AddToListComponent;

@Immutable
@Entity
@Table(name = "ped_licence_transactions")
public class PearsLicenceTransaction implements AddToListComponent {

  @Id
  @Column(name = "transaction_id", insertable = false, updatable = false)
  private Integer transactionId;

  @Column(name = "transaction_reference")
  private String transactionReference;

  public PearsLicenceTransaction() {
  }

  @VisibleForTesting
  public PearsLicenceTransaction(Integer applicationId, String applicationReference) {
    this.transactionId = applicationId;
    this.transactionReference = applicationReference;
  }

  public Integer getTransactionId() {
    return transactionId;
  }

  public PearsLicenceTransaction setTransactionId(Integer transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public String getTransactionReference() {
    return transactionReference;
  }

  public PearsLicenceTransaction setTransactionReference(String transactionReference) {
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
