package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PearsLicenceTransactionRepository extends CrudRepository<PearsLicenceApplication, Integer> {

  List<PearsLicenceApplication> findAllByTransactionIdIn(List<Integer> ids);

  List<PearsLicenceApplication> findAllByTransactionReferenceContainingIgnoreCase(String applicationReference);

}
