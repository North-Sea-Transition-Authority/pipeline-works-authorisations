package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

@Entity
@Table(name = "portal_organisation_units")
@Immutable
public class PortalOrganisationUnit implements SearchSelectable {

  @Id
  private int ouId;

  private String name;

  @ManyToOne
  @JoinColumn(name = "org_grp_id")
  private PortalOrganisationGroup portalOrganisationGroup;

  // the companies house start date
  private LocalDate startDate;

  // the companies house date the company was closed/dissolved
  private LocalDate endDate;

  private boolean isDuplicate;

  private boolean isActive;

  public PortalOrganisationUnit() {
  }

  @VisibleForTesting
  PortalOrganisationUnit(int ouId,
                         String name,
                         PortalOrganisationGroup portalOrganisationGroup,
                         LocalDate startDate,
                         LocalDate endDate,
                         boolean isDuplicate,
                         boolean isActive) {
    this.ouId = ouId;
    this.name = name;
    this.portalOrganisationGroup = portalOrganisationGroup;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isDuplicate = isDuplicate;
    this.isActive = isActive;
  }


  public int getOuId() {
    return ouId;
  }

  public String getName() {
    return name;
  }

  public Optional<PortalOrganisationGroup> getPortalOrganisationGroup() {
    return Optional.ofNullable(portalOrganisationGroup);
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isDuplicate() {
    return isDuplicate;
  }

  public boolean isActive() {
    return isActive;
  }

  // Interface methods
  @Override
  public String getSelectionId() {
    return String.valueOf(ouId);
  }

  @Override
  public String getSelectionText() {
    return getName();
  }


  // IDE generated methods
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalOrganisationUnit)) {
      return false;
    }
    PortalOrganisationUnit that = (PortalOrganisationUnit) o;
    return ouId == that.ouId
        && name.equals(that.name)
        && Objects.equals(portalOrganisationGroup, that.portalOrganisationGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ouId, name, portalOrganisationGroup);
  }
}
