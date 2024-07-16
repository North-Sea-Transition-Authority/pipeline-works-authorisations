package uk.co.ogauthority.pwa.service.search.consents;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

public interface ConsentSearchPredicateProvider {

  default boolean shouldApplyToSearch(ConsentSearchParams searchParams,
                                      ConsentSearchContext searchContext) {
    return true;
  }

  Predicate getPredicate(ConsentSearchParams searchParams,
                         ConsentSearchContext searchContext,
                         CriteriaQuery<ConsentSearchItem> criteriaQuery,
                         Root<ConsentSearchItem> queryRoot);

}
