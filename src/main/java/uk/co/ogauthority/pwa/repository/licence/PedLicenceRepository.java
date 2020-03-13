package uk.co.ogauthority.pwa.repository.licence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.licence.PedLicence;

@Repository
public interface PedLicenceRepository extends CrudRepository<PedLicence, Integer> {

  List<PedLicence> findAllByLicenceNameLikeIgnoreCase(String licenceName);

}
