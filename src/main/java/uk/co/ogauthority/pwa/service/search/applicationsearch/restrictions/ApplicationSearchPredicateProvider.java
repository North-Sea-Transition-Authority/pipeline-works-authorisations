package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

/**
 * Produces metadata about a PwaApplication including the version of the application a given user is permitted to access.
 */
public interface ApplicationSearchPredicateProvider {


  boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                             ApplicationSearchParameters applicationSearchParameters);


  Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                            ApplicationSearchParameters applicationSearchParameters,
                            CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                            Root<ApplicationDetailView> searchCoreRoot);

}
