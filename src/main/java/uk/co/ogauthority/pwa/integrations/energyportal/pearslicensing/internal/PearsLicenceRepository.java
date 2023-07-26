package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;

@Repository
public interface PearsLicenceRepository extends CrudRepository<PearsLicence, Integer> {

  List<PearsLicence> findAllByLicenceNameContainingIgnoreCase(String licenceName);

  Optional<PearsLicence> findByMasterId(Integer masterId);

  List<PearsLicence> findByMasterIdIn(List<Integer> masterId);

}
