package uk.co.ogauthority.pwa.model.entity.masterpwas;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;
import uk.co.ogauthority.pwa.model.entity.converters.DevukFieldIdConverter;


@Entity
@Table(name = "pwa_detail_fields")
public class MasterPwaDetailField {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_detail_id")
  private MasterPwaDetail masterPwaDetail;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as DevukFieldId just wraps a basic class.
  @Convert(converter = DevukFieldIdConverter.class)
  private DevukFieldId devukFieldId;

  private String manualFieldName;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DevukFieldId getDevukFieldId() {
    return devukFieldId;
  }

  public void setDevukFieldId(DevukFieldId fieldId) {
    this.devukFieldId = fieldId;
  }

  public String getManualFieldName() {
    return manualFieldName;
  }

  public void setManualFieldName(String manualFieldName) {
    this.manualFieldName = manualFieldName;
  }

  public MasterPwaDetail getMasterPwaDetail() {
    return masterPwaDetail;
  }

  public void setMasterPwaDetail(MasterPwaDetail masterPwaDetail) {
    this.masterPwaDetail = masterPwaDetail;
  }
}
