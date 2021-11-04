package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_block_crossing_owners")
public class PadCrossedBlockOwner implements ChildEntity<Integer, PadCrossedBlock> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_block_crossing_id")
  private PadCrossedBlock padCrossedBlock;

  private Integer ownerOuId;

  private String ownerName;

  public PadCrossedBlockOwner() {
  }

  public PadCrossedBlockOwner(PadCrossedBlock padCrossedBlock, Integer ownerOuId, String ownerName) {
    this.padCrossedBlock = padCrossedBlock;
    this.ownerOuId = ownerOuId;
    this.ownerName = ownerName;
  }

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadCrossedBlock parentEntity) {
    this.padCrossedBlock = parentEntity;
  }

  @Override
  public PadCrossedBlock getParent() {
    return this.padCrossedBlock;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadCrossedBlock getPadCrossedBlock() {
    return padCrossedBlock;
  }

  public void setPadCrossedBlock(PadCrossedBlock padCrossedBlock) {
    this.padCrossedBlock = padCrossedBlock;
  }

  public Integer getOwnerOuId() {
    return ownerOuId;
  }

  public void setOwnerOuId(Integer ownerOuId) {
    this.ownerOuId = ownerOuId;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadCrossedBlockOwner that = (PadCrossedBlockOwner) o;
    return id.equals(that.id)
        && padCrossedBlock.equals(that.padCrossedBlock)
        && Objects.equals(ownerOuId, that.ownerOuId)
        && Objects.equals(ownerName, that.ownerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, padCrossedBlock, ownerOuId, ownerName);
  }
}
