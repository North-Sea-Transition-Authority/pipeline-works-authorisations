package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "ped_blocks")
@Immutable
public class PearsBlock {

  @Id
  private String compositeKey;

  @ManyToOne
  @JoinColumn(name = "plm_id")
  private PearsLicence pearsLicence;

  @Column(name = "block_ref")
  private String blockReference;

  @Column(name = "block_no")
  private String blockNumber;

  @Column(name = "quadrant_no")
  private String quadrantNumber;

  private String suffix;

  @Enumerated(EnumType.STRING)
  @Column(name = "location")
  private BlockLocation blockLocation;

  public PearsBlock() {
  }

  @VisibleForTesting
  public PearsBlock(String compositeKey,
                    PearsLicence licence,
                    String blockRef,
                    String blockNo,
                    String quadrantNo,
                    String suffix,
                    BlockLocation blockLocation) {
    this.compositeKey = compositeKey;
    this.pearsLicence = licence;
    this.blockReference = blockRef;
    this.blockNumber = blockNo;
    this.quadrantNumber = quadrantNo;
    this.suffix = suffix;
    this.blockLocation = blockLocation;
  }

  public String getCompositeKey() {
    return compositeKey;
  }

  public PearsLicence getPearsLicence() {
    return pearsLicence;
  }

  public String getBlockReference() {
    return blockReference;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public String getQuadrantNumber() {
    return quadrantNumber;
  }

  public String getSuffix() {
    return suffix;
  }

  public BlockLocation getBlockLocation() {
    return blockLocation;
  }

  public boolean isLicensed() {
    return pearsLicence != null;
  }
}
