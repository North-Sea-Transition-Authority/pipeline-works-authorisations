package uk.co.ogauthority.pwa.model.entity.masterpwas;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.internal.DevukFieldIdConverter;


@Entity
@Table(name = "pwa_detail_fields")
public class MasterPwaDetailArea {

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

  public MasterPwaDetailArea() {
  }

  public MasterPwaDetailArea(MasterPwaDetail masterPwaDetail,
                             DevukFieldId devukFieldId,
                             String manualFieldName) {
    this.masterPwaDetail = masterPwaDetail;
    this.devukFieldId = devukFieldId;
    this.manualFieldName = manualFieldName;
  }

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
