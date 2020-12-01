package uk.co.ogauthority.pwa.model.view.banner;

/**
 * Class representing a basic banner that is viewed at the top of pages.
 */
public class PageBannerView {

  private final String header;
  private final String headerCaption;

  private final String bodyHeader;

  private final BannerLink bannerLink;

  private PageBannerView(String header,
                         String headerCaption,
                         String bodyHeader,
                         BannerLink bannerLink) {
    this.header = header;
    this.headerCaption = headerCaption;
    this.bodyHeader = bodyHeader;
    this.bannerLink = bannerLink;
  }

  public String getHeader() {
    return header;
  }

  public String getHeaderCaption() {
    return headerCaption;
  }

  public String getBodyHeader() {
    return bodyHeader;
  }

  public BannerLink getBannerLink() {
    return bannerLink;
  }

  public static class PageBannerViewBuilder {
    private String header;
    private String headerCaption;
    private String bodyHeader;
    private BannerLink bannerLink;

    public PageBannerViewBuilder setHeader(String header) {
      this.header = header;
      return this;
    }

    public PageBannerViewBuilder setHeaderCaption(String headerCaption) {
      this.headerCaption = headerCaption;
      return this;
    }

    public PageBannerViewBuilder setBodyHeader(String bodyHeader) {
      this.bodyHeader = bodyHeader;
      return this;
    }

    public PageBannerViewBuilder setBannerLink(BannerLink bannerLink) {
      this.bannerLink = bannerLink;
      return this;
    }

    public PageBannerView build() {
      return new PageBannerView(header, headerCaption, bodyHeader, bannerLink);
    }
  }
}
