package uk.co.ogauthority.pwa.features.termsandconditions.model;

public class TermsAndConditionsForm {

  private Integer pwaId;

  private Integer variationTerm;

  private Integer huooTermOne;

  private Integer huooTermTwo;

  private Integer huooTermThree;

  private Integer depconParagraph;

  private Integer depconSchedule;

  public Integer getPwaId() {
    return pwaId;
  }

  public TermsAndConditionsForm setPwaId(Integer pwaId) {
    this.pwaId = pwaId;
    return this;
  }

  public Integer getVariationTerm() {
    return variationTerm;
  }

  public TermsAndConditionsForm setVariationTerm(Integer variationTerm) {
    this.variationTerm = variationTerm;
    return this;
  }

  public Integer getHuooTermOne() {
    return huooTermOne;
  }

  public TermsAndConditionsForm setHuooTermOne(Integer huooTermOne) {
    this.huooTermOne = huooTermOne;
    return this;
  }

  public Integer getHuooTermTwo() {
    return huooTermTwo;
  }

  public TermsAndConditionsForm setHuooTermTwo(Integer huooTermTwo) {
    this.huooTermTwo = huooTermTwo;
    return this;
  }

  public Integer getHuooTermThree() {
    return huooTermThree;
  }

  public TermsAndConditionsForm setHuooTermThree(Integer huooTermThree) {
    this.huooTermThree = huooTermThree;
    return this;
  }

  public Integer getDepconParagraph() {
    return depconParagraph;
  }

  public TermsAndConditionsForm setDepconParagraph(Integer depconParagraph) {
    this.depconParagraph = depconParagraph;
    return this;
  }

  public Integer getDepconSchedule() {
    return depconSchedule;
  }

  public TermsAndConditionsForm setDepconSchedule(Integer depconSchedule) {
    this.depconSchedule = depconSchedule;
    return this;
  }
}
