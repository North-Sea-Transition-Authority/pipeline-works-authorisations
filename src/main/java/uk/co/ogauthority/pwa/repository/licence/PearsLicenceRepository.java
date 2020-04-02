package uk.co.ogauthority.pwa.repository.licence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;

@Repository
public interface PearsLicenceRepository extends CrudRepository<PearsLicence, Integer> {

  List<PearsLicence> findAllByLicenceNameContainingIgnoreCase(String licenceName);

  Optional<PearsLicence> findByMasterId(Integer masterId);

}
