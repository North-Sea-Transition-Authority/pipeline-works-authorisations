package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

/**
 * A HuooRole is the type of association an organisation or other entity has with a PWA.
 */
public enum HuooRole implements Displayable {

  HOLDER(10, "Holder"),
  USER(20, "User"),
  OPERATOR(30, "Operator"),
  OWNER(40, "Owner");

  private final int displayOrder;
  private final String displayText;

  HuooRole(int displayOrder, String displayText) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
  }

  @Override
  public String getDisplayName() {
    return displayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static Stream<HuooRole> stream() {
    return Arrays.stream(HuooRole.values());
  }
}
