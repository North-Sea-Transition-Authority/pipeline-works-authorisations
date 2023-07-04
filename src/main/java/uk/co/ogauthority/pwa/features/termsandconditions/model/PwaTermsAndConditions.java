package uk.co.ogauthority.pwa.features.termsandconditions.model;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Entity(name = "terms_and_conditions_variations")
public class PwaTermsAndConditions {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id")
  private MasterPwa masterPwa;

  private int variationTerm;

  private String huooTerms;

  private int depconParagraph;

  private int depconSchedule;

  private int createdBy;

  private Instant createdTimestamp;

  public Integer getId() {
    return id;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public PwaTermsAndConditions setMasterPwa(
      MasterPwa masterPwa) {
    this.masterPwa = masterPwa;
    return this;
  }

  public int getVariationTerm() {
    return variationTerm;
  }

  public PwaTermsAndConditions setVariationTerm(int variationTerm) {
    this.variationTerm = variationTerm;
    return this;
  }

  public String getHuooTerms() {
    return huooTerms;
  }

  public PwaTermsAndConditions setHuooTerms(String huooTerm) {
    this.huooTerms = huooTerm;
    return this;
  }

  public int getDepconParagraph() {
    return depconParagraph;
  }

  public PwaTermsAndConditions setDepconParagraph(int depconParagraph) {
    this.depconParagraph = depconParagraph;
    return this;
  }

  public int getDepconSchedule() {
    return depconSchedule;
  }

  public PwaTermsAndConditions setDepconSchedule(int depconSchedule) {
    this.depconSchedule = depconSchedule;
    return this;
  }

  public int getCreatedBy() {
    return createdBy;
  }

  public PwaTermsAndConditions setCreatedBy(int submittedBy) {
    this.createdBy = submittedBy;
    return this;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public PwaTermsAndConditions setCreatedTimestamp(Instant submittedTimestamp) {
    this.createdTimestamp = submittedTimestamp;
    return this;
  }
}
