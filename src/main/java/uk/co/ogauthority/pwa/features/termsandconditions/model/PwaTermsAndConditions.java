package uk.co.ogauthority.pwa.features.termsandconditions.model;

import java.time.Instant;
import java.util.Arrays;
import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity(name = "terms_and_conditions")
public class PwaTermsAndConditions {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String pwaReference;

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

  public String getPwaReference() {
    return pwaReference;
  }

  public PwaTermsAndConditions setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
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
