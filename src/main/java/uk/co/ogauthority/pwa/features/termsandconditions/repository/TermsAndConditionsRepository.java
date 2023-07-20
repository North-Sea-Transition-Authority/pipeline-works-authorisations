package uk.co.ogauthority.pwa.features.termsandconditions.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.termsandconditions.model.PwaTermsAndConditions;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Repository
public interface TermsAndConditionsRepository extends CrudRepository<PwaTermsAndConditions, Integer> {
  Optional<PwaTermsAndConditions> findPwaTermsAndConditionsByMasterPwa(MasterPwa masterPwa);

  Page<PwaTermsAndConditions> findAllByPwaReferenceContainingIgnoreCase(Pageable pageable, String filter);

}