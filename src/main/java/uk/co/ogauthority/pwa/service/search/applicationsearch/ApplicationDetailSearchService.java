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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions.ApplicationSearchPredicateProvider;

/**
 * Service which consumes search criteria and uses that to filter applications results for all purposes.
 */
@Service
public class ApplicationDetailSearchService {

  static final int MAX_RESULTS = 50;

  private final List<ApplicationSearchPredicateProvider> applicationSearchPredicateProviders;
  private final EntityManager entityManager;

  @Autowired
  public ApplicationDetailSearchService(List<ApplicationSearchPredicateProvider> applicationSearchPredicateProviders,
                                        EntityManager entityManager) {
    this.applicationSearchPredicateProviders = applicationSearchPredicateProviders;
    this.entityManager = entityManager;
  }

  public SearchScreenView<ApplicationDetailItemView> search(ApplicationSearchParameters searchParameters,
                                                            ApplicationSearchContext applicationSearchContext) {

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
        .where(predicateList.toArray(Predicate[]::new))
        // app id is directly stored in app ref, sort directly rather than deconstruct the reference so its sortable.
        .orderBy(cb.desc(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID)));

    TypedQuery<ApplicationDetailView> q = entityManager.createQuery(searchCoreQuery)
        .setMaxResults(MAX_RESULTS);

    // required to force simple list of interface where the specific type can be ignored.
    List<ApplicationDetailItemView> results = new ArrayList<>(q.getResultList());

    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<ApplicationDetailView> count = countQuery.from(ApplicationDetailView.class);
    countQuery.select(cb.count(count))
        .where(searchCoreQuery.getRestriction());

    long countQueryResult = entityManager.createQuery(countQuery).getSingleResult();

    return new SearchScreenView<>(countQueryResult, results);

  }

}
