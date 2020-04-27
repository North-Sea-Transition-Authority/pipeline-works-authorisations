package uk.co.ogauthority.pwa.model.form.fds;

public class RestSearchItem {

  private final String id;
  private final String text;

  public RestSearchItem(String id, String text) {
    this.id = id;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }
}
