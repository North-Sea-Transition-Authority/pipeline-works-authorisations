package uk.co.ogauthority.pwa.repository.appprocessing.options;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;

public interface OptionsApplicationApprovalRepository extends CrudRepository<OptionsApplicationApproval, Integer> {

  Optional<OptionsApplicationApproval> findByPwaApplication(PwaApplication pwaApplication);

}