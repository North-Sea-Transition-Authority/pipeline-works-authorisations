package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "fee_periods")
public class FeePeriod {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String description;

  @CreatedDate
  private Instant created;

  @LastModifiedDate
  @Column(name = "last_modified")
  private Instant modified;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeePeriod feePeriod = (FeePeriod) o;
    return description.equals(feePeriod.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description);
  }

  @PrePersist
  @PreUpdate
  private void updateTimeStamps() {
    if (created == null) {
      created = Instant.now();
    }
    modified = Instant.now();
  }
}
