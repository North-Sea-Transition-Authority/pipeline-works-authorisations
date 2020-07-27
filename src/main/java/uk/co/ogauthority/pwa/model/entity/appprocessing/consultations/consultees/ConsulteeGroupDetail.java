package uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "consultee_group_details")
public class ConsulteeGroupDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "cg_id")
  private ConsulteeGroup consulteeGroup;

  private String name;

  private String abbreviation;

  private Boolean tipFlag;

  private Integer versionNo;

  private Instant startTimestamp;

  private Instant endTimestamp;

  public ConsulteeGroupDetail() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ConsulteeGroup getConsulteeGroup() {
    return consulteeGroup;
  }

  public Integer getConsulteeGroupId() {
    return consulteeGroup.getId();
  }

  public void setConsulteeGroup(ConsulteeGroup consulteeGroup) {
    this.consulteeGroup = consulteeGroup;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsulteeGroupDetail that = (ConsulteeGroupDetail) o;
    return Objects.equals(id, that.id)
        && Objects.equals(consulteeGroup, that.consulteeGroup)
        && Objects.equals(name, that.name)
        && Objects.equals(abbreviation, that.abbreviation)
        && Objects.equals(tipFlag, that.tipFlag)
        && Objects.equals(versionNo, that.versionNo)
        && Objects.equals(startTimestamp, that.startTimestamp)
        && Objects.equals(endTimestamp, that.endTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, consulteeGroup, name, abbreviation, tipFlag, versionNo, startTimestamp, endTimestamp);
  }
}
