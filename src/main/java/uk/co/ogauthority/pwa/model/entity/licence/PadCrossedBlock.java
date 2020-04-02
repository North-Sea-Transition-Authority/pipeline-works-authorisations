package uk.co.ogauthority.pwa.model.entity.licence;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLicenceStatus;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_blocks")
public class PadCrossedBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @OneToOne
  @JoinColumn(name = "plm_id")
  private PearsLicence licence;

  @Column(name = "block_ref")
  private String blockReference;

  @Column(name = "quadrant_no")
  private String quadrantNumber;

  @Column(name = "blockNumber")
  private String blockNumber;

  private String suffix;
  private BlockLocation location;
  private BlockLicenceStatus licenceStatus;
  private Instant startTimestamp;

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

  public PearsLicence getLicence() {
    return licence;
  }

  public void setLicence(PearsLicence licence) {
    this.licence = licence;
  }

  public String getBlockReference() {
    return blockReference;
  }

  public void setBlockReference(String blockReference) {
    this.blockReference = blockReference;
  }

  public String getQuadrantNumber() {
    return quadrantNumber;
  }

  public void setQuadrantNumber(String quadrantNumber) {
    this.quadrantNumber = quadrantNumber;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(String blockNumber) {
    this.blockNumber = blockNumber;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public BlockLocation getLocation() {
    return location;
  }

  public void setLocation(BlockLocation location) {
    this.location = location;
  }

  public BlockLicenceStatus getLicenceStatus() {
    return licenceStatus;
  }

  public void setLicenceStatus(BlockLicenceStatus licenceStatus) {
    this.licenceStatus = licenceStatus;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }
}
