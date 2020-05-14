package uk.co.ogauthority.pwa.model.entity.masterpwas;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "pwas")
public class MasterPwa {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "pwa_master_id_generator")
  @SequenceGenerator(name = "pwa_master_id_generator", sequenceName = "pwas_id_seq", allocationSize = 1)
  private Integer id;

  private Instant createdTimestamp;

  public MasterPwa() {

  }

  public MasterPwa(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MasterPwa masterPwa = (MasterPwa) o;
    return id.equals(masterPwa.id)
        && createdTimestamp.equals(masterPwa.createdTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdTimestamp);
  }
}
