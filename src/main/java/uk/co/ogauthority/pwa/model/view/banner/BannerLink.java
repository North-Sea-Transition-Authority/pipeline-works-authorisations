package uk.co.ogauthority.pwa.model.view.banner;

public class BannerLink {
  private String url;
  private String text;

  public BannerLink(String url, String text) {
    this.url = url;
    this.text = text;
  }

  public String getUrl() {
    return url;
  }

  public String getText() {
    return text;
  }
}
