package uk.co.ogauthority.pwa.service.search.consents;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
