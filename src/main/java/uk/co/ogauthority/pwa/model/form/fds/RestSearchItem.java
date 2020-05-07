package uk.co.ogauthority.pwa.model.form.fds;

/**
 * A RestSearchItem is used to produce each item within a search selector.
 * id and text are required fields for the JSON response.
 */
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
