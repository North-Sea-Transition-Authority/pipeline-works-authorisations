package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity
@Table(name = "fee_items")
public class FeeItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType pwaApplicationType;
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


  public String getDisplayDescription() {
    return displayDescription;
  }

  public void setDisplayDescription(String displayDescription) {
    this.displayDescription = displayDescription;
  }

}
