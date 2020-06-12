package uk.co.ogauthority.pwa.energyportal.model.dto.teams;

import java.util.Objects;

public class PortalRoleDto {

  private final int resId;
  private final String name;
  private final String title;
  private final String description;
  private final int displaySequence;

  public PortalRoleDto(int resId, String name, String title, String description, int displaySequence) {
    this.resId = resId;
    this.name = name;
    this.title = title;
    this.description = description;
    this.displaySequence = displaySequence;
  }

  public int getResId() {
    return resId;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getDisplaySequence() {
    return displaySequence;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalRoleDto)) {
      return false;
    }
    PortalRoleDto that = (PortalRoleDto) o;
    return resId == that.resId
        && displaySequence == that.displaySequence
        && Objects.equals(name, that.name)
        && Objects.equals(title, that.title)
        && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resId, name, title, description, displaySequence);
  }
}
