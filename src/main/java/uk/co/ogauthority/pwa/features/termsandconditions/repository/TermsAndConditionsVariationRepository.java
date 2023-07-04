package uk.co.ogauthority.pwa.features.termsandconditions.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsVariation;

@Repository
public interface TermsAndConditionsVariationRepository extends CrudRepository<TermsAndConditionsVariation, Integer> {



}