package uk.co.ogauthority.pwa.service.search.consents;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem_;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.search.consents.predicates.ConsentSearchPredicateProvider;

@Service
public class ConsentSearchService {

  static final int MAX_RESULTS_SIZE = 50;

  private final EntityManager entityManager;
  private final List<ConsentSearchPredicateProvider> consentSearchPredicateProviders;

  @Autowired
  public ConsentSearchService(EntityManager entityManager,
                              List<ConsentSearchPredicateProvider> consentSearchPredicateProviders) {
    this.entityManager = entityManager;
    this.consentSearchPredicateProviders = consentSearchPredicateProviders;
  }

  public SearchScreenView search(ConsentSearchParams searchParams,
                                 ConsentSearchContext searchContext) {

    var cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<ConsentSearchItem> criteriaQuery = cb.createQuery(ConsentSearchItem.class);

    // FROM ConsentSearchItem
    Root<ConsentSearchItem> consentSearchItem = criteriaQuery.from(ConsentSearchItem.class);

    // get WHERE clause predicates that we should apply to the query
    List<Predicate> predicates = consentSearchPredicateProviders.stream()
        .filter(predicateProvider -> predicateProvider.shouldApplyToSearch(searchParams, searchContext))
        .map(predicateProvider -> predicateProvider.getPredicate(searchParams, searchContext, criteriaQuery,
            consentSearchItem))
        .collect(Collectors.toList());

    // SELECT ConsentSearchItem
    criteriaQuery.select(consentSearchItem)
        // WHERE applicable predicates are true
        .where(predicates.toArray(Predicate[]::new))
        // ORDER BY pwa_id DESC
        .orderBy(cb.desc(consentSearchItem.get(ConsentSearchItem_.PWA_ID)));

    TypedQuery<ConsentSearchItem> typedQuery = entityManager.createQuery(criteriaQuery)
        // LIMIT MAX_RESULTS_SIZE
        .setMaxResults(MAX_RESULTS_SIZE);

    List<ConsentSearchResultView> results = typedQuery.getResultList().stream()
        .map(ConsentSearchResultView::fromSearchItem)
        .collect(Collectors.toList());

    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<ConsentSearchItem> count = countQuery.from(ConsentSearchItem.class);
    countQuery.select(cb.count(count))
        .where(criteriaQuery.getRestriction());

    long countQueryResult = entityManager.createQuery(countQuery).getSingleResult();

    return new SearchScreenView(countQueryResult, results);

  }

}
