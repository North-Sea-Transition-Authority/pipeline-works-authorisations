package uk.co.ogauthority.pwa.features.webapp.devtools.paydashboard;

import jakarta.validation.constraints.NotNull;

public class TestPaymentForm {

  @NotNull
  private int amount;
  @NotNull
  private String reference;

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }
}
