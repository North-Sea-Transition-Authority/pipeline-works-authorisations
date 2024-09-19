package uk.co.ogauthority.pwa.service.search.applicationsearch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
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
  private final ApplicationSearchParamsValidator applicationSearchParamsValidator;
  private final EntityManager entityManager;

  @Autowired
  public ApplicationDetailSearchService(List<ApplicationSearchPredicateProvider> applicationSearchPredicateProviders,
                                        ApplicationSearchParamsValidator applicationSearchParamsValidator,
                                        EntityManager entityManager) {
    this.applicationSearchPredicateProviders = applicationSearchPredicateProviders;
    this.applicationSearchParamsValidator = applicationSearchParamsValidator;
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

    List<Predicate> countPredicateList = new ArrayList<>();
    // loop providers and get applicable predicates
    applicationSearchPredicateProviders.stream()
        .filter(applicationSearchPredicateProvider -> applicationSearchPredicateProvider.doesPredicateApply(
            applicationSearchContext,
            searchParameters
        ))
        .forEach(applicationSearchPredicateProvider -> countPredicateList.add(applicationSearchPredicateProvider.createPredicate(
            applicationSearchContext,
            searchParameters,
            countQuery,
            count
        )));

    countQuery.select(cb.count(count))
        .where(countPredicateList.toArray(Predicate[]::new));

    long countQueryResult = entityManager.createQuery(countQuery).getSingleResult();

    return new SearchScreenView<>(countQueryResult, results);

  }

  public BindingResult validateSearchParamsUsingContext(ApplicationSearchParameters searchParameters,
                                                        ApplicationSearchContext applicationSearchContext) {
    var bindingResult = new BeanPropertyBindingResult(searchParameters, "form");

    applicationSearchParamsValidator.validate(searchParameters, bindingResult, applicationSearchContext);

    return bindingResult;

  }

}
