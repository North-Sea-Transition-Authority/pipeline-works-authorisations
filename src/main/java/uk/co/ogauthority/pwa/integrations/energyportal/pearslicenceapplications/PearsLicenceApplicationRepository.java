package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PearsLicenceApplicationRepository extends CrudRepository<PearsLicenceApplications, Integer> {

  List<PearsLicenceApplications> findAllByApplicationIdIn(List<Integer> ids);

  List<PearsLicenceApplications> findAllByApplicationReferenceContainingIgnoreCase(String applicationReference);

}
