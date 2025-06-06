package uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnitDetail;

@Repository
public interface PortalOrganisationUnitDetailRepository extends CrudRepository<PortalOrganisationUnitDetail, Integer> {

  @EntityGraph(attributePaths = {"organisationUnit", "organisationUnit.portalOrganisationGroup"})
  List<PortalOrganisationUnitDetail> findByOrganisationUnitIn(List<PortalOrganisationUnit> portalOrganisationUnits);

  @EntityGraph(attributePaths = {"organisationUnit", "organisationUnit.portalOrganisationGroup"})
  List<PortalOrganisationUnitDetail> findByOrganisationUnit_ouIdIn(Collection<Integer> portalOrganisationUnitOuIds);

}
