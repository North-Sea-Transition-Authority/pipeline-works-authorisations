package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Entity
@Table(name = "as_built_notification_groups")
public class AsBuiltNotificationGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_consent_id")
  private PwaConsent pwaConsent;

  private String reference;

  private Instant createdTimestamp;

  public AsBuiltNotificationGroup() {
    //hibernate
  }

  public AsBuiltNotificationGroup(PwaConsent pwaConsent,
                                  String reference,
                                  Instant createdTimestamp) {
    this.pwaConsent = pwaConsent;
    this.reference = reference;
    this.createdTimestamp = createdTimestamp;
  }

  public int getMasterPwaIdFromGroupConsent() {
    return this.pwaConsent.getMasterPwa().getId();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaConsent getPwaConsent() {
    return pwaConsent;
  }

  public void setPwaConsent(PwaConsent pwaConsent) {
    this.pwaConsent = pwaConsent;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
