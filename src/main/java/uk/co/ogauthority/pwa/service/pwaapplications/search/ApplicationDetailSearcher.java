package uk.co.ogauthority.pwa.service.pwaapplications.search;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class ApplicationDetailSearcher {

  private static final Instant MIN_SORT_INSTANT = LocalDateTime.of(0, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  private static final Instant MAX_SORT_INSTANT = LocalDateTime.of(4000, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);

  private final EntityManager entityManager;
  private final ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository;

  @Autowired
  public ApplicationDetailSearcher(EntityManager entityManager,
                                   ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository) {
    this.entityManager = entityManager;
    this.applicationDetailSearchItemRepository = applicationDetailSearchItemRepository;
  }

  public Page<ApplicationDetailSearchItem> searchByStatus(Pageable pageable,
                                                          Set<PwaApplicationStatus> statusFilter) {
    if (statusFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return applicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPadStatusIn(
        pageable,
        statusFilter
    );
  }


  public Page<ApplicationDetailSearchItem> searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(
      Pageable pageable,
      Set<Integer> pwaApplicationIdFilter,
      Set<PwaApplicationStatus> statusFilter,
      boolean openUpdateRequestFilter) {

    return applicationDetailSearchItemRepository.findAllByPwaApplicationIdInAndPadStatusInAndOpenUpdateRequestFlag(
        pageable,
        pwaApplicationIdFilter,
        statusFilter,
        openUpdateRequestFilter
    );

  }

  public Page<ApplicationDetailSearchItem> searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
      Pageable pageable,
      Set<Integer> pwaApplicationIdFilter,
      Set<PwaApplicationStatus> statusFilter,
      boolean openUpdateRequestFilter) {

    return createPagedQueryWithPredicate((root) ->
        getPredicateWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequestIs(
            root, pwaApplicationIdFilter, statusFilter, openUpdateRequestFilter
        ), pageable
    );

  }

  private Predicate getPredicateWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequestIs(
      Root<ApplicationDetailSearchItem> root,
      Set<Integer> pwaApplicationIdFilter,
      Set<PwaApplicationStatus> statusFilter,
      boolean openUpdateRequestFilter) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaBuilder.In<Integer> appIdFilterPredicate = cb.in(
        root.get(ApplicationDetailSearchItem_.pwaApplicationId));
    pwaApplicationIdFilter.forEach(appIdFilterPredicate::value);

    CriteriaBuilder.In<PwaApplicationStatus> statusFilterPredicate = cb.in(
        root.get(ApplicationDetailSearchItem_.padStatus));
    statusFilter.forEach(statusFilterPredicate::value);

    Predicate openRequestForUpdatePredicate = cb.equal(
        root.get(ApplicationDetailSearchItem_.openUpdateRequestFlag), openUpdateRequestFilter);

    Predicate statusOrOpenUpdatePredicate = cb.or(statusFilterPredicate, openRequestForUpdatePredicate);

    Predicate finalPredicate = cb.and(appIdFilterPredicate, statusOrOpenUpdatePredicate);

    return finalPredicate;

  }


  private Page<ApplicationDetailSearchItem> createPagedQueryWithPredicate(
      Function<Root<ApplicationDetailSearchItem>, Predicate> createAndApplyPredicateToRoot,
      Pageable pageable) {
    var cb = entityManager.getCriteriaBuilder();

    // have to work out total results by doing a count query based off the search query predicate,
    // 1. get count of app possible results given the predicate
    CriteriaQuery<Long> countResultsQuery = cb.createQuery(Long.class);
    Root<ApplicationDetailSearchItem> countResultsRoot = countResultsQuery.from(ApplicationDetailSearchItem.class);
    countResultsQuery
        .select(cb.count(countResultsRoot))
        .where(createAndApplyPredicateToRoot.apply(countResultsRoot));
    var totalResults = entityManager.createQuery(countResultsQuery).getSingleResult();

    // 2. Create results query using predicate
    CriteriaQuery<ApplicationDetailSearchItem> searchResultsQuery = cb.createQuery(ApplicationDetailSearchItem.class);
    Root<ApplicationDetailSearchItem> searchResultsRoot = searchResultsQuery.from(ApplicationDetailSearchItem.class);
    searchResultsQuery.where(createAndApplyPredicateToRoot.apply(searchResultsRoot));

    //3. apply sort from pagebable to query
    searchResultsQuery.orderBy(getOrderListFromPageable(cb, searchResultsRoot, pageable));

    // 4. Limits search results to requested page
    TypedQuery<ApplicationDetailSearchItem> typedQuery = entityManager.createQuery(searchResultsQuery);

    // limits results based on pageable args
    typedQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    typedQuery.setMaxResults(pageable.getPageSize());

    var results = typedQuery.getResultList();

    return new PageImpl<>(results, pageable, totalResults);
  }

  private List<Order> getOrderListFromPageable(CriteriaBuilder cb, Root<ApplicationDetailSearchItem> root, Pageable pageable) {

    List<Order> orderList = new ArrayList<>();

    pageable.getSort().iterator().forEachRemaining(springOrder -> {
      Order order;

      var dataType = root.get(springOrder.getProperty()).getJavaType();
      if (!dataType.equals(Instant.class)) {
        throw new UnsupportedOperationException("Cannot decode class %s for default value when null");
      }

      // oh god I'm sorry, hardcode in default sort value when null and limit to just Instant type sorting for now.
      // out of the box QueryUtils.toOrders() ignores null handling definition and default behaviour is always nulls last
      if (springOrder.getDirection().isAscending() && springOrder.getNullHandling().equals(Sort.NullHandling.NULLS_FIRST)) {
        order = cb.asc(cb.coalesce(root.get(springOrder.getProperty()), MIN_SORT_INSTANT));
      } else if (springOrder.getDirection().isAscending()) {
        order = cb.asc(cb.coalesce(root.get(springOrder.getProperty()), MAX_SORT_INSTANT));
      } else if (springOrder.getDirection().isDescending() && springOrder.getNullHandling().equals(Sort.NullHandling.NULLS_FIRST)) {
        order = cb.desc(cb.coalesce(root.get(springOrder.getProperty()), MAX_SORT_INSTANT));
      } else {
        order = cb.desc(cb.coalesce(root.get(springOrder.getProperty()), MIN_SORT_INSTANT));
      }

      orderList.add(order);

    });

    return orderList;


  }

  public Page<ApplicationDetailSearchItem> searchByStatusOrApplicationIds(Pageable pageable,
                                                                          Set<PwaApplicationStatus> statusFilter,
                                                                          Set<Integer> pwaApplicationIdFilter) {

    if (statusFilter.isEmpty() && pwaApplicationIdFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return applicationDetailSearchItemRepository.findAllByPadStatusInOrPwaApplicationIdIn(
        pageable,
        statusFilter,
        pwaApplicationIdFilter
    );

  }

  public Optional<ApplicationDetailSearchItem> searchByApplicationDetailId(Integer pwaApplicationDetailId) {
    return applicationDetailSearchItemRepository.findByPwaApplicationDetailIdEquals(pwaApplicationDetailId);
  }


}
