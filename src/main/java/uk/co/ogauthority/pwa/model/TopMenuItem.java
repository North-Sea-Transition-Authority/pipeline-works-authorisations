package uk.co.ogauthority.pwa.model;

import org.apache.commons.lang3.StringUtils;

public class TopMenuItem {

  private final String displayName;
  private final String url;

  public TopMenuItem(String displayName, String url) {
    this.displayName = displayName;
    // Menu item urls should not have trailing '/' to allow correct matching against the current page url to determine if
    // the nav item is currently active
    this.url = StringUtils.stripEnd(url, "/");
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrl() {
    return url;
  }

}
