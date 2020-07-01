package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;


public interface PadDesignOpConditionsRepository extends CrudRepository<PadDesignOpConditions, Integer> {

  Optional<PadDesignOpConditions> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
