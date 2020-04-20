package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_cable_crossings")
public class PadCableCrossing implements Comparable<PadCableCrossing> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  private String cableName;
  private String location;
  private String cableOwner;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getCableName() {
    return cableName;
  }

  public void setCableName(String cableName) {
    this.cableName = cableName;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCableOwner() {
    return cableOwner;
  }

  public void setCableOwner(String cableOwner) {
    this.cableOwner = cableOwner;
  }

  @Override
  public int compareTo(PadCableCrossing padCableCrossing) {
    var compareName = this.cableName.compareTo(padCableCrossing.cableName);
    if (compareName == 0) {
      return this.id.compareTo(padCableCrossing.id);
    }
    return compareName;
  }
}
