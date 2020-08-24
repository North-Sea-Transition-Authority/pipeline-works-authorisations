package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum TreatyAgreement {

  NORWAY("Norway", "Norwegian Continental Shelf", "NCS"),
  BELGIUM("Belgium", "Belgium Continental Shelf", "BCS"),
  NETHERLANDS("Netherlands", "Netherlands Continental Shelf", "NCS"),
  IRELAND("Ireland", "Ireland Continental Shelf", "ICS");

  private static final String STANDARD_AGREEMENT_TEXT_FORMAT = "Any body corporate which is party to an arrangement to export gas to the " +
      "United Kingdom from the %s (%s)";

  private final String country;
  private final String agreementText;

  TreatyAgreement(String country, String continentalShelf, String continentalShelfAbbreviation) {
    this.country = country;
    this.agreementText = String.format(STANDARD_AGREEMENT_TEXT_FORMAT, continentalShelf, continentalShelfAbbreviation);
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
