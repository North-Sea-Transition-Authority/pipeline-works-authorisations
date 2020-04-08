package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum TreatyAgreement {

  NORWAY("Norway", "Any body corporate which is party to an arrangement to export gas to the " +
      "United Kingdom from the Norwegian Continental Shelf (NCS)"),
  BELGIUM("Belgium", "Placeholder Belgium treaty agreement"),
  NETHERLANDS("Netherlands", "Placeholder Netherlands treaty agreement"),
  IRELAND("Ireland", "Placeholder Ireland treaty agreement");

  private String country;
  private String agreementText;

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
