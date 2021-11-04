package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CrossingAgreementsValidationResult {

  private final Map<CrossingAgreementsSection, Boolean> sectionValidity;
  private final boolean isValid;

  public CrossingAgreementsValidationResult(Set<CrossingAgreementsSection> validSections) {
    this.sectionValidity = CrossingAgreementsSection.stream()
        .collect(Collectors.toMap(cas -> cas, validSections::contains));

    this.isValid = validSections.size() == sectionValidity.size();
  }

  public boolean isSectionValid(CrossingAgreementsSection crossingAgreementsSection) {
    return sectionValidity.get(crossingAgreementsSection);
  }

  public boolean isSectionValid(String crossingAgreementsSection) {
    return isSectionValid(CrossingAgreementsSection.valueOf(crossingAgreementsSection));
  }

  public boolean isCrossingAgreementsValid() {
    return isValid;
  }
}
