package uk.co.ogauthority.pwa.model.entity.masterpwas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "pwas")
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
