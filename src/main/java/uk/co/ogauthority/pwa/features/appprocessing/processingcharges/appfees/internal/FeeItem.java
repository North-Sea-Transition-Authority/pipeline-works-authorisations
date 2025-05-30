package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

@Entity
@Table(name = "fee_items")
public class FeeItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType pwaApplicationType;

  @Enumerated(EnumType.STRING)
  private PwaApplicationFeeType pwaApplicationFeeType;

  private String displayDescription;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public PwaApplicationType getPwaApplicationType() {
    return pwaApplicationType;
  }

  public void setPwaApplicationType(PwaApplicationType pwaApplicationType) {
    this.pwaApplicationType = pwaApplicationType;
  }

  public PwaApplicationFeeType getPwaApplicationFeeType() {
    return pwaApplicationFeeType;
  }

  public void setPwaApplicationFeeType(
      PwaApplicationFeeType pwaApplicationFeeType) {
    this.pwaApplicationFeeType = pwaApplicationFeeType;
  }

  public String getDisplayDescription() {
    return displayDescription;
  }

  public void setDisplayDescription(String displayDescription) {
    this.displayDescription = displayDescription;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeeItem feeItem = (FeeItem) o;
    return pwaApplicationType == feeItem.pwaApplicationType && pwaApplicationFeeType == feeItem.pwaApplicationFeeType && Objects.equals(
        displayDescription, feeItem.displayDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationType, pwaApplicationFeeType, displayDescription);
  }
}
