package uk.co.ogauthority.pwa.model.view.sidebarnav;

import java.util.Objects;

/**
 * Represents a sidebar link for template macro processing decisions.
 */
public final class SidebarSectionLink {
  /* When rendering the link, should we skip using the spring url processor as the link is to some page element. */
  private final boolean isAnchorLink;
  /* What is given to the html "a" tag href attribute. */
  private final String link;
  /* What is the content of the html "a" tag shown on screen. */
  private final String displayText;

  private SidebarSectionLink(boolean isAnchorLink, String link, String displayText) {
    this.isAnchorLink = isAnchorLink;
    this.link = link;
    this.displayText = displayText;
  }

  public boolean getIsAnchorLink() {
    return isAnchorLink;
  }

  public String getLink() {
    return link;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static SidebarSectionLink createAnchorLink(String displayText, String link) {
    return new SidebarSectionLink(true, link, displayText);
  }

  public static SidebarSectionLink createExternalLink(String displayText, String link) {
    return new SidebarSectionLink(false, link, displayText);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SidebarSectionLink that = (SidebarSectionLink) o;
    return isAnchorLink == that.isAnchorLink
        && Objects.equals(link, that.link)
        && Objects.equals(displayText, that.displayText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAnchorLink, link, displayText);
  }
}
