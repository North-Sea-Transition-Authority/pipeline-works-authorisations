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
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.WorkAreaApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class WorkAreaApplicationDetailSearcher {

  private static final Instant MIN_SORT_INSTANT = LocalDateTime.of(0, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  private static final Instant MAX_SORT_INSTANT = LocalDateTime.of(4000, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);

  private final EntityManager entityManager;
  private final WorkAreaApplicationDetailSearchItemRepository workAreaApplicationDetailSearchItemRepository;

  @Autowired
  public WorkAreaApplicationDetailSearcher(EntityManager entityManager,
                                           WorkAreaApplicationDetailSearchItemRepository workAreaApplicationDetailSearchItemRepository) {
    this.entityManager = entityManager;
    this.workAreaApplicationDetailSearchItemRepository = workAreaApplicationDetailSearchItemRepository;
  }

  public Page<WorkAreaApplicationDetailSearchItem> searchByStatus(Pageable pageable,
                                                                  Set<PwaApplicationStatus> statusFilter) {
    if (statusFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return workAreaApplicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPadStatusIn(
        pageable,
        statusFilter
    );
  }


  public Page<WorkAreaApplicationDetailSearchItem> searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(
      Pageable pageable,
      Set<Integer> pwaApplicationIdFilter,
      Set<PwaApplicationStatus> statusFilter,
      boolean openUpdateRequestFilter) {

    return workAreaApplicationDetailSearchItemRepository.findAllByPwaApplicationIdInAndPadStatusInAndOpenUpdateRequestFlag(
        pageable,
        pwaApplicationIdFilter,
        statusFilter,
        openUpdateRequestFilter
    );

  }

  public Page<WorkAreaApplicationDetailSearchItem> searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
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
      Root<WorkAreaApplicationDetailSearchItem> root,
      Set<Integer> pwaApplicationIdFilter,
      Set<PwaApplicationStatus> statusFilter,
      boolean openUpdateRequestFilter) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaBuilder.In<Integer> appIdFilterPredicate = cb.in(
        root.get(WorkAreaApplicationDetailSearchItem_.pwaApplicationId));
    pwaApplicationIdFilter.forEach(appIdFilterPredicate::value);
    if (pwaApplicationIdFilter.isEmpty()) {
      appIdFilterPredicate.value(cb.nullLiteral(Integer.class));
    }

    CriteriaBuilder.In<PwaApplicationStatus> statusFilterPredicate = cb.in(
        root.get(WorkAreaApplicationDetailSearchItem_.padStatus));
    statusFilter.forEach(statusFilterPredicate::value);

    Predicate openRequestForUpdatePredicate = cb.equal(
        root.get(WorkAreaApplicationDetailSearchItem_.openUpdateRequestFlag), openUpdateRequestFilter);

    Predicate publicNoticeUpdateRequestPredicate = cb.equal(
        root.get(WorkAreaApplicationDetailSearchItem_.publicNoticeStatus), PublicNoticeStatus.APPLICANT_UPDATE);

    Predicate statusOrOpenUpdatePredicate = cb.or(statusFilterPredicate, openRequestForUpdatePredicate, publicNoticeUpdateRequestPredicate);

    Predicate finalPredicate = cb.and(appIdFilterPredicate, statusOrOpenUpdatePredicate);

    return finalPredicate;

  }


  private Page<WorkAreaApplicationDetailSearchItem> createPagedQueryWithPredicate(
      Function<Root<WorkAreaApplicationDetailSearchItem>, Predicate> createAndApplyPredicateToRoot,
      Pageable pageable) {
    var cb = entityManager.getCriteriaBuilder();

    // have to work out total results by doing a count query based off the search query predicate,
    // 1. get count of app possible results given the predicate
    CriteriaQuery<Long> countResultsQuery = cb.createQuery(Long.class);
    Root<WorkAreaApplicationDetailSearchItem> countResultsRoot = countResultsQuery.from(
        WorkAreaApplicationDetailSearchItem.class);
    countResultsQuery
        .select(cb.count(countResultsRoot))
        .where(createAndApplyPredicateToRoot.apply(countResultsRoot));
    var totalResults = entityManager.createQuery(countResultsQuery).getSingleResult();

    // 2. Create results query using predicate
    CriteriaQuery<WorkAreaApplicationDetailSearchItem> searchResultsQuery = cb.createQuery(
        WorkAreaApplicationDetailSearchItem.class);
    Root<WorkAreaApplicationDetailSearchItem> searchResultsRoot = searchResultsQuery.from(
        WorkAreaApplicationDetailSearchItem.class);
    searchResultsQuery.where(createAndApplyPredicateToRoot.apply(searchResultsRoot));

    //3. apply sort from pagebable to query
    searchResultsQuery.orderBy(getOrderListFromPageable(cb, searchResultsRoot, pageable));

    // 4. Limits search results to requested page
    TypedQuery<WorkAreaApplicationDetailSearchItem> typedQuery = entityManager.createQuery(searchResultsQuery);

    // limits results based on pageable args
    typedQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    typedQuery.setMaxResults(pageable.getPageSize());

    var results = typedQuery.getResultList();

    return new PageImpl<>(results, pageable, totalResults);
  }

  private List<Order> getOrderListFromPageable(CriteriaBuilder cb, Root<WorkAreaApplicationDetailSearchItem> root, Pageable pageable) {

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

  /**
   * Get app details where either/or:
   * - the latest version hasn't been confirmed satisfactory
   * - latest version is satisfactory, but no open update requests, consultation requests etc.
   */
  public Page<WorkAreaApplicationDetailSearchItem>
        searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
          Pageable pageable,
          Set<PwaApplicationStatus> statusFilter,
          Set<PublicNoticeStatus> publicNoticeStatusFilter,
          Set<Integer> pwaApplicationIdFilter) {

    if (statusFilter.isEmpty() && pwaApplicationIdFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return workAreaApplicationDetailSearchItemRepository
        .findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsOrAllWaitFlagsMatch(
        pageable,
        // passing null when empty is required or else spring produces invalid sql for the IN condition.
        statusFilter.isEmpty() ? null : statusFilter,
        pwaApplicationIdFilter.isEmpty() ? null : pwaApplicationIdFilter,
        false,
        false,
        publicNoticeStatusFilter.isEmpty() ? null : publicNoticeStatusFilter,
        false
    );

  }

  /**
   * Get app details where the latest version is satisfactory and there is at least one open update request, consultation request etc.
   */
  public Page<WorkAreaApplicationDetailSearchItem>
        searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(
          Pageable pageable,
          Set<PwaApplicationStatus> statusFilter,
          Set<PublicNoticeStatus> publicNoticeStatusFilter,
          Set<Integer> pwaApplicationIdFilter) {

    if (statusFilter.isEmpty() && pwaApplicationIdFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return workAreaApplicationDetailSearchItemRepository
        .findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsAndAnyWaitFlagsMatch(
        pageable,
        // passing null when empty is required or else spring produces invalid sql for the IN condition.
        statusFilter.isEmpty() ? null : statusFilter,
        pwaApplicationIdFilter.isEmpty() ? null : pwaApplicationIdFilter,
        true,
        true,
        publicNoticeStatusFilter.isEmpty() ? null : publicNoticeStatusFilter,
        true
    );

  }

  public Optional<WorkAreaApplicationDetailSearchItem> searchByApplicationDetailId(Integer pwaApplicationDetailId) {
    return workAreaApplicationDetailSearchItemRepository.findByPwaApplicationDetailIdEquals(pwaApplicationDetailId);
  }


}
