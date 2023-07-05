package uk.co.ogauthority.pwa.features.termsandconditions.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.termsandconditions.model.PwaTermsAndConditions;

@Repository
public interface TermsAndConditionsRepository extends CrudRepository<PwaTermsAndConditions, Integer> {



}