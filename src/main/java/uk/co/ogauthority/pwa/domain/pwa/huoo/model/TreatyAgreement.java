package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import java.util.Arrays;
import java.util.stream.Stream;

public enum TreatyAgreement {

  ANY_TREATY_COUNTRY("Any treaty country",
      "Any body corporate which is party to an arrangement to export and/or import petroleum to and/or from the United Kingdom " +
          "(as the case may be).");

  private final String country;
  private final String agreementText;

  TreatyAgreement(String country, String agreementText) {
    this.country = country;
    this.agreementText = agreementText;
  }

  public String getCountry() {
    return country;
  }

  public String getAgreementText() {
    return agreementText;
  }

  public static Stream<TreatyAgreement> stream() {
    return Arrays.stream(TreatyAgreement.values());
  }
}
