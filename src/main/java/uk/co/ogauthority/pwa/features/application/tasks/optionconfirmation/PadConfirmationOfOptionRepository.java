package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadConfirmationOfOptionRepository extends CrudRepository<PadConfirmationOfOption, Integer> {

  Optional<PadConfirmationOfOption> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  boolean existsByPwaApplicationDetailAndConfirmedOptionType(PwaApplicationDetail pwaApplicationDetail,
                                                             ConfirmedOptionType confirmedOptionType);

}