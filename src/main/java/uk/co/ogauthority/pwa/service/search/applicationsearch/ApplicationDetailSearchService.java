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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions.ApplicationSearchPredicateProvider;

/**
 * Service which consumes search criteria and uses that to filter applications results for all purposes.
 */
@Service
public class ApplicationDetailSearchService {

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

    // required to force simple list of interface where the specific type can be ignored.
    return new ArrayList<>(q.getResultList());

  }


  public BindingResult validateSearchParamsUsingContext(ApplicationSearchParameters searchParameters,
                                                        ApplicationSearchContext applicationSearchContext) {
    var bindingResult = new BeanPropertyBindingResult(searchParameters, "form");

    applicationSearchParamsValidator.validate(searchParameters, bindingResult, applicationSearchContext);

    return bindingResult;

  }


}
