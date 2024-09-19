package uk.co.ogauthority.pwa.features.termsandconditions.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.Arrays;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Entity(name = "terms_and_conditions")
public class PwaTermsAndConditions {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id")
  private MasterPwa masterPwa;

  private int variationTerm;

  private int huooTermOne;

  private int huooTermTwo;

  private int huooTermThree;

  private int depconParagraph;

  private int depconSchedule;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId createdBy;

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

  public int getHuooTermOne() {
    return huooTermOne;
  }

  public PwaTermsAndConditions setHuooTermOne(int huooTermOne) {
    this.huooTermOne = huooTermOne;
    return this;
  }

  public int getHuooTermTwo() {
    return huooTermTwo;
  }

  public PwaTermsAndConditions setHuooTermTwo(int huooTermTwo) {
    this.huooTermTwo = huooTermTwo;
    return this;
  }

  public int getHuooTermThree() {
    return huooTermThree;
  }

  public PwaTermsAndConditions setHuooTermThree(int huooTermThree) {
    this.huooTermThree = huooTermThree;
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

  public PersonId getCreatedBy() {
    return createdBy;
  }

  public PwaTermsAndConditions setCreatedBy(PersonId createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public PwaTermsAndConditions setCreatedTimestamp(Instant submittedTimestamp) {
    this.createdTimestamp = submittedTimestamp;
    return this;
  }

  public String getHuooString() {
    Integer[] huooTerms = {huooTermOne, huooTermTwo, huooTermThree};
    Arrays.sort(huooTerms);
    return String.format("%s, %s & %s", huooTerms[0], huooTerms[1], huooTerms[2]);
  }
}
