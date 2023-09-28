package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PearsLicenceApplicationRepository extends CrudRepository<PearsLicenceApplication, Integer> {

  List<PearsLicenceApplication> findAllByApplicationIdIn(List<Integer> ids);

  List<PearsLicenceApplication> findAllByApplicationReferenceContainingIgnoreCase(String applicationReference);

}
