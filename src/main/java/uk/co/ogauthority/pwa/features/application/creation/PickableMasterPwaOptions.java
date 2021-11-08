package uk.co.ogauthority.pwa.features.application.creation;

import java.util.Map;

public class PickableMasterPwaOptions {

  private final Map<String, String> consentedPickablePwas;

  private final Map<String, String> nonconsentedPickablePwas;

  public PickableMasterPwaOptions(Map<String, String> consentedPickablePwas,
                                  Map<String, String> nonconsentedPickablePwas) {

    this.nonconsentedPickablePwas = nonconsentedPickablePwas;

    this.consentedPickablePwas = consentedPickablePwas;
  }

  public Map<String, String> getConsentedPickablePwas() {
    return consentedPickablePwas;
  }

  public Map<String, String> getNonconsentedPickablePwas() {
    return nonconsentedPickablePwas;
  }
}

