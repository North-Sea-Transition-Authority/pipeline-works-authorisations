package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

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
    return sectionValidity.get(CrossingAgreementsSection.valueOf(crossingAgreementsSection));
  }

  public boolean isCrossingAgreementsValid() {
    return isValid;
  }
}
