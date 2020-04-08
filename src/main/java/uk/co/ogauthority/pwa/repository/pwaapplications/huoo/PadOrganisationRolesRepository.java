package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

@Repository
public interface PadOrganisationRolesRepository extends CrudRepository<PadOrganisationRole, Integer> {

  List<PadOrganisationRole> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
