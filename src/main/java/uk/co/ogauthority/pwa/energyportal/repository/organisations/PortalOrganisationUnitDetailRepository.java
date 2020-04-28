package uk.co.ogauthority.pwa.energyportal.repository.organisations;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;

@Repository
public interface PortalOrganisationUnitDetailRepository extends CrudRepository<PortalOrganisationUnitDetail, Integer> {

  @EntityGraph(attributePaths = "organisationUnit")
  List<PortalOrganisationUnitDetail> getAllByOrganisationUnitIn(List<PortalOrganisationUnit> portalOrganisationUnits);

}
