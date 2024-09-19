package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;


import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

/**
 * Based on the user type, make sure that
 * 1. only 1 row per applications is returned
 * 2. only applications that are visible to specific searcher are returned
 * 3. only the latest version applicable per user type is returned.
 */
@Service
public class ApplicationReferencePredicateProvider implements ApplicationSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public ApplicationReferencePredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {
    return StringUtils.isNotBlank(applicationSearchParameters.getAppReference());
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<?> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {

    var cb = entityManager.getCriteriaBuilder();
    var lowerCaseReference = applicationSearchParameters.getAppReference().toLowerCase();
    return cb.like(
        cb.lower(searchCoreRoot.get(ApplicationDetailView_.PAD_REFERENCE)),
        "%" + lowerCaseReference + "%"
    );
  }
}
