package uk.co.ogauthority.pwa.model.entity.migration;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Entity
@Table(name = "migrated_pipeline_auths")
public class ProcessedPwaConsentMigration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne
  @JoinColumn(name = "pad_id", referencedColumnName = "padId")
  private MigrationPwaConsent migrationPwaConsent;

  @OneToOne
  @JoinColumn(name = "pwa_pipeline_consent_id", referencedColumnName = "id")
  private PwaConsent pwaConsent;

  private Instant migratedTimestamp;

  public ProcessedPwaConsentMigration() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public MigrationPwaConsent getMigrationPwaConsent() {
    return migrationPwaConsent;
  }

  public void setMigrationPwaConsent(MigrationPwaConsent migrationPwaConsent) {
    this.migrationPwaConsent = migrationPwaConsent;
  }

  public PwaConsent getPwaConsent() {
    return pwaConsent;
  }

  public void setPwaConsent(PwaConsent pwaConsent) {
    this.pwaConsent = pwaConsent;
  }

  public Instant getMigratedTimestamp() {
    return migratedTimestamp;
  }

  public void setMigratedTimestamp(Instant migratedTimestamp) {
    this.migratedTimestamp = migratedTimestamp;
  }

  @Override
  public String toString() {
    return "ProcessedPwaConsentMigration{" +
        "id=" + id +
        ", migrationPwaConsent.padId=" + migrationPwaConsent.getPadId() +
        ", pwaConsent.id=" + pwaConsent.getId() +
        ", migratedTimestamp=" + migratedTimestamp +
        '}';
  }
}
