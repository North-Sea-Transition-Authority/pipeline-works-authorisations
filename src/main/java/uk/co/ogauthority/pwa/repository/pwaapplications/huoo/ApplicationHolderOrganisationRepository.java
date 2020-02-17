package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwa.huoo.ApplicationHolderOrganisation;

@Repository
public interface ApplicationHolderOrganisationRepository extends CrudRepository<ApplicationHolderOrganisation, Integer> {

  List<ApplicationHolderOrganisation> findByPwaApplicationDetail(PwaApplicationDetail detail);

}
