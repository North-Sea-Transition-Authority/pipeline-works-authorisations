package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PearsLicenceTransactionRepository extends CrudRepository<PearsLicenceTransaction, Integer> {

  List<PearsLicenceTransaction> findAllByTransactionIdIn(List<Integer> ids);

  List<PearsLicenceTransaction> findAllByTransactionReferenceContainingIgnoreCase(String applicationReference);

}
