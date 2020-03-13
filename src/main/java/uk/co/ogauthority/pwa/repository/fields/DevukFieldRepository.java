package uk.co.ogauthority.pwa.repository.fields;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.fields.DevukField;

@Repository
public interface DevukFieldRepository extends CrudRepository<DevukField, Integer> {

  List<DevukField> findAllByOrganisationUnitAndStatusIn(PortalOrganisationUnit organisationUnit, List<Integer> statusCodes);

}
