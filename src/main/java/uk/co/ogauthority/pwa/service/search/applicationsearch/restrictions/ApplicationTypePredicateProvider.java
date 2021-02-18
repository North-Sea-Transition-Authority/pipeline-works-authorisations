package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

@Service
public class ApplicationTypePredicateProvider implements ApplicationSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public ApplicationTypePredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {

    return applicationSearchParameters.getPwaApplicationType() != null;
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {

    var cb = entityManager.getCriteriaBuilder();
    return cb.equal(searchCoreRoot.get(ApplicationDetailView_.APPLICATION_TYPE), applicationSearchParameters.getPwaApplicationType());
  }
}
