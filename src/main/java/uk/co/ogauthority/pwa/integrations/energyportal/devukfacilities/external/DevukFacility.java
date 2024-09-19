package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

@Entity(name = "devuk_facilities")
@Immutable
public class DevukFacility implements SearchSelectable {

  @Id
  private Integer id;

  private String facilityName;

  public DevukFacility() {}

  @VisibleForTesting
  public DevukFacility(Integer id, String facilityName) {
    this.id = id;
    this.facilityName = facilityName;
  }

  public Integer getId() {
    return id;
  }

  public String getFacilityName() {
    return facilityName;
  }

  @Override
  public String getSelectionId() {
    return String.valueOf(id);
  }

  @Override
  public String getSelectionText() {
    return getFacilityName();
  }
}
