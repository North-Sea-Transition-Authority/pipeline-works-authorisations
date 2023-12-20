package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_linked_areas")
public class PadLinkedArea implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "area_name_manual_entry")
  private String areaName;

  @ManyToOne
  @JoinColumn(name = "field_id")
  private DevukField devukField;

  @Enumerated(EnumType.STRING)
  private LinkedAreaType areaType;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  public PadLinkedArea() {
  }

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    setPwaApplicationDetail(parentEntity);
  }

  @Override
  public PwaApplicationDetail getParent() {
    return getPwaApplicationDetail();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAreaName() {
    return areaName;
  }

  public void setAreaName(String areaName) {
    this.areaName = areaName;
  }

  public DevukField getDevukField() {
    return devukField;
  }

  public void setDevukField(DevukField devukField) {
    this.devukField = devukField;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public boolean isLinkedToDevuk() {
    return (devukField != null && LinkedAreaType.FIELD.equals(areaType));
  }

  public LinkedAreaType getAreaType() {
    return areaType;
  }

  public PadLinkedArea setAreaType(LinkedAreaType areaType) {
    this.areaType = areaType;
    return this;
  }
}
