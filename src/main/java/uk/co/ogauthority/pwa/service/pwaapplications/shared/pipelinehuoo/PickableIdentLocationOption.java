package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;

/**
 * Defines points that can be selected as start of end points of a HUOO pipeline section.
 */
public class PickableIdentLocationOption implements Comparable<PickableIdentLocationOption> {

  private final String pickableString;
  private final String displayString;
  private final String locationName;

  public PickableIdentLocationOption(int identNumber, IdentPoint identPoint, String location) {
    this.pickableString = String.format("%s-%s", identNumber, identPoint);
    this.displayString = String.format("Ident %s %s %s", identNumber, identPoint.getMidStringDisplay(), location);
    this.locationName = location;
  }

  static Set<PickableIdentLocationOption> createIdentLocationOptionsFrom(IdentView identView) {
    return Set.of(
        new PickableIdentLocationOption(identView.getIdentNumber(), IdentPoint.FROM_LOCATION,
            identView.getFromLocation()),
        new PickableIdentLocationOption(identView.getIdentNumber(), IdentPoint.TO_LOCATION, identView.getToLocation())
    );

  }

  String getSortKey() {
    return StringUtils.leftPad(String.valueOf(getIdentNumber()), 3, "0") + "_" + getIdentPoint().getDisplayOrder();
  }

  public String getPickableString() {
    return pickableString;
  }

  public int getIdentNumber() {
    return Integer.valueOf(pickableString.substring(0, pickableString.indexOf("-")));
  }

  public String getDisplayString() {
    return displayString;
  }

  public String getLocationName() {
    return locationName;
  }

  public IdentPoint getIdentPoint() {
    return IdentPoint.valueOf(pickableString.substring(pickableString.indexOf("-") + 1));
  }

  @Override
  public int compareTo(PickableIdentLocationOption pickableIdentLocationOption) {
    return this.getSortKey().compareTo(pickableIdentLocationOption.getSortKey());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PickableIdentLocationOption that = (PickableIdentLocationOption) o;
    return Objects.equals(pickableString, that.pickableString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pickableString);
  }

  public enum IdentPoint {
    FROM_LOCATION(1, "from"),
    TO_LOCATION(2, "to");

    private final int displayOrder;
    private final String midStringDisplay;

    IdentPoint(int displayOrder, String midStringDisplay) {
      this.displayOrder = displayOrder;
      this.midStringDisplay = midStringDisplay;
    }

    public int getDisplayOrder() {
      return displayOrder;
    }

    public String getMidStringDisplay() {
      return midStringDisplay;
    }
  }
}
