package uk.co.ogauthority.pwa.model.form.documents;

import javax.validation.constraints.NotNull;

public class ClauseForm {

  @NotNull(message = "Enter a clause name")
  private String name;

  @NotNull(message = "Enter some clause text")
  private String text;

  public ClauseForm() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
