package uk.co.ogauthority.pwa.model.form.masterpwas.contacts;

public class AddPwaContactForm {

  private String userIdentifier;

  private Integer pwaApplicationId;

  public AddPwaContactForm() {
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(Integer pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }
}
