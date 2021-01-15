package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailViewRepository;
import uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions.ApplicationSearchPredicateProvider;

/**
 * Service which consumes search criteria and uses that to filter applications results for all purposes.
 */
@Service
public class ApplicationDetailSearchService {

  private final ApplicationDetailViewRepository applicationDetailViewRepository;
  private final List<ApplicationSearchPredicateProvider> applicationSearchPredicateProviders;
  private final EntityManager entityManager;


  @Autowired
  public ApplicationDetailSearchService(ApplicationDetailViewRepository applicationDetailViewRepository,
                                        List<ApplicationSearchPredicateProvider> applicationSearchPredicateProviders,
                                        EntityManager entityManager) {
    this.applicationDetailViewRepository = applicationDetailViewRepository;
    this.applicationSearchPredicateProviders = applicationSearchPredicateProviders;
    this.entityManager = entityManager;
  }

  public List<ApplicationDetailItemView> search(ApplicationSearchParameters searchParameters,
                                                ApplicationSearchContext applicationSearchContext) {
    // copying list allows return type of list to be simple interface, not an ? extends so we can ignore impl type.

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ApplicationDetailView> searchCoreQuery = cb.createQuery(ApplicationDetailView.class);
    Root<ApplicationDetailView> searchCoreRoot = searchCoreQuery.from(ApplicationDetailView.class);

    List<Predicate> predicateList = new ArrayList<>();
    // loop providers and get applicable predicates
    applicationSearchPredicateProviders.stream()
        .filter(applicationSearchPredicateProvider -> applicationSearchPredicateProvider.doesPredicateApply(
            applicationSearchContext,
            searchParameters
        ))
        .forEach(applicationSearchPredicateProvider -> predicateList.add(applicationSearchPredicateProvider.createPredicate(
            applicationSearchContext,
            searchParameters,
            searchCoreQuery,
            searchCoreRoot
        )));

    // set selection and apply provided predicates
    searchCoreQuery.select(searchCoreRoot)
        .where(predicateList.toArray(Predicate[]::new));

    TypedQuery<ApplicationDetailView> q = entityManager.createQuery(searchCoreQuery);

    List<ApplicationDetailItemView> applicationDetailViews = List.copyOf(q.getResultList());
    return applicationDetailViews;

  }


}
