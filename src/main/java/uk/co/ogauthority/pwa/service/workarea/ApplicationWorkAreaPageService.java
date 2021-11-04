package uk.co.ogauthority.pwa.service.workarea;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment_;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab_;

/**
 * Responsible for querying application data and returning the appropriate page of application work area items
 * for the person making the request.
 */
@Service
class ApplicationWorkAreaPageService {

  private static final Instant MIN_SORT_INSTANT = LocalDateTime.of(0, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  private static final Instant MAX_SORT_INSTANT = LocalDateTime.of(4000, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);

  private final EntityManager entityManager;

  @Autowired
  public ApplicationWorkAreaPageService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Page<WorkAreaApplicationDetailSearchItem> getUsersWorkAreaTabContents(
      WorkAreaContext workAreaContext,
      WorkAreaTabCategory workAreaTabCategory,
      Pageable pageable) {

    return createPagedQuery(
        workAreaContext,
        workAreaTabCategory,
        pageable
    );
  }

  private <T_SELECT> Predicate getAppContactPredicate(Root<WorkAreaAppUserTab> root,
                                                      CriteriaQuery<T_SELECT> query,
                                                      WorkAreaContext workAreaContext,
                                                      WorkAreaTabCategory workAreaTabCategory) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate appContactAppRestriction = cb.isFalse(cb.literal(true));

    if (workAreaContext.containsWorkAreaUserType(WorkAreaUserType.APPLICATION_CONTACT)) {

      Subquery<Integer> contactAppIdSubQuery = query.subquery(Integer.class);

      Root<PwaContact> contactRoot = contactAppIdSubQuery.from(PwaContact.class);
      Join<PwaContact, PwaApplication> contactToApplicationJoin = contactRoot.join(PwaContact_.pwaApplication);
      Path<Integer> appIdPath = contactToApplicationJoin.get(PwaApplication_.ID);

      contactAppIdSubQuery.select(appIdPath);
      contactAppIdSubQuery.where(cb.and(
          cb.equal(contactRoot.get(PwaContact_.PERSON), workAreaContext.getAuthenticatedUserAccount().getLinkedPerson()),
          cb.equal(root.get(WorkAreaAppUserTab_.APP_USER_WORKAREA_CATEGORY), workAreaTabCategory)
      ));

      appContactAppRestriction = cb.in(root.get(WorkAreaApplicationDetailSearchItem_.PWA_APPLICATION_ID)).value(contactAppIdSubQuery);

    }

    return appContactAppRestriction;

  }

  private <T_SELECT> Predicate getCaseOfficerPredicate(Root<WorkAreaAppUserTab> root,
                                                       CriteriaQuery<T_SELECT> query,
                                                       WorkAreaContext workAreaContext,
                                                       WorkAreaTabCategory workAreaTabCategory) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate caseOfficerAppRestriction = cb.isFalse(cb.literal(true));

    if (workAreaContext.containsWorkAreaUserType(WorkAreaUserType.CASE_OFFICER)) {
      Subquery<Integer> assignmentSubQuery = query.subquery(Integer.class);
      Root<Assignment> assignmentRoot = assignmentSubQuery.from(Assignment.class);

      Path<Integer> appIdPath = assignmentRoot.get(Assignment_.BUSINESS_KEY);

      assignmentSubQuery.select(cb.toInteger(appIdPath));
      assignmentSubQuery.where(cb.and(
          cb.equal(assignmentRoot.get(Assignment_.ASSIGNEE_PERSON_ID), workAreaContext.getPersonId())),
          cb.equal(assignmentRoot.get(Assignment_.WORKFLOW_TYPE), WorkflowType.PWA_APPLICATION),
          cb.equal(assignmentRoot.get(Assignment_.WORKFLOW_ASSIGNMENT), WorkflowAssignment.CASE_OFFICER),
          cb.equal(root.get(WorkAreaAppUserTab_.CASE_OFFICER_WORKAREA_CATEGORY), workAreaTabCategory)
      );

      caseOfficerAppRestriction = cb.in(root.get(WorkAreaApplicationDetailSearchItem_.PWA_APPLICATION_ID)).value(assignmentSubQuery);
    }

    return caseOfficerAppRestriction;
  }

  private <T_SELECT> Predicate getPwaManagerPredicate(Root<WorkAreaAppUserTab> root,
                                                      CriteriaQuery<T_SELECT> query,
                                                      WorkAreaContext workAreaContext,
                                                      WorkAreaTabCategory workAreaTabCategory) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate pwaManagerAppPredicate = cb.isFalse(cb.literal(true));
    if (workAreaContext.containsWorkAreaUserType(WorkAreaUserType.PWA_MANAGER)) {
      pwaManagerAppPredicate = cb.equal(root.get(WorkAreaAppUserTab_.PWA_MANAGER_WORKAREA_CATEGORY), workAreaTabCategory);
    }

    return pwaManagerAppPredicate;
  }

  // this helper creates the predicate that does all the core heavy lifting so we return the applications for a user
  // T_SELECT is the type of selection used by the core query. Generic to support both the count query and the core results query.
  private <T_SELECT> Predicate getWorkAreaUserTypeAndTabCategoryTypePredicate(
      Root<WorkAreaAppUserTab> root,
      CriteriaQuery<T_SELECT> query,
      WorkAreaContext workAreaContext,
      WorkAreaTabCategory workAreaTabCategory) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    Predicate appContactAppRestriction = getAppContactPredicate(
        root, query, workAreaContext, workAreaTabCategory
    );

    Predicate caseOfficerAppRestriction = getCaseOfficerPredicate(
        root, query, workAreaContext, workAreaTabCategory
    );

    Predicate pwaManagerAppPredicate = getPwaManagerPredicate(
        root, query, workAreaContext, workAreaTabCategory
    );

    Predicate workAreaUserTypeAndTabCategoryPredicate = cb.or(
        pwaManagerAppPredicate,
        appContactAppRestriction,
        caseOfficerAppRestriction
    );

    return workAreaUserTypeAndTabCategoryPredicate;

  }

  // using non parameterised CriteriaQuery to be able to use same predicate provider for count and results query
  private Page<WorkAreaApplicationDetailSearchItem> createPagedQuery(
      WorkAreaContext workAreaContext,
      WorkAreaTabCategory workAreaTabCategory,
      Pageable pageable) {
    var cb = entityManager.getCriteriaBuilder();

    // have to work out total results by doing a count query based off the search query predicate,
    // 1. get count of app possible results given the predicate
    CriteriaQuery<Long> countResultsQuery = cb.createQuery(Long.class);
    Root<WorkAreaAppUserTab> countResultsRoot = countResultsQuery.from(WorkAreaAppUserTab.class);
    Join<WorkAreaAppUserTab, WorkAreaApplicationDetailSearchItem> countWorkAreaSearchItemJoin = countResultsRoot
        .join(WorkAreaAppUserTab_.workAreaApplicationDetailSearchItem);
    countResultsQuery
        .select(cb.count(countWorkAreaSearchItemJoin))
        .where(
            getWorkAreaUserTypeAndTabCategoryTypePredicate(
                countResultsRoot,
                countResultsQuery,
                workAreaContext,
                workAreaTabCategory
            )
      );
    var totalResults = entityManager.createQuery(countResultsQuery).getSingleResult();

    // 2. Create results query using predicate
    CriteriaQuery<WorkAreaApplicationDetailSearchItem> searchResultsQuery = cb.createQuery(
        WorkAreaApplicationDetailSearchItem.class);
    Root<WorkAreaAppUserTab> searchResultsRoot = searchResultsQuery.from(WorkAreaAppUserTab.class);
    Join<WorkAreaAppUserTab, WorkAreaApplicationDetailSearchItem> workAreaSearchItemJoin = searchResultsRoot
        .join(WorkAreaAppUserTab_.workAreaApplicationDetailSearchItem);
    searchResultsQuery.select(workAreaSearchItemJoin);
    searchResultsQuery.where(
        getWorkAreaUserTypeAndTabCategoryTypePredicate(
            searchResultsRoot,
            searchResultsQuery,
            workAreaContext,
            workAreaTabCategory
        )
    );

    //3. apply sort from pageable to query
    searchResultsQuery.orderBy(getOrderListFromPageable(cb, workAreaSearchItemJoin, pageable));

    // 4. Limits search results to requested page
    TypedQuery<WorkAreaApplicationDetailSearchItem> typedQuery = entityManager.createQuery(searchResultsQuery);

    // limits results based on pageable args
    typedQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    typedQuery.setMaxResults(pageable.getPageSize());

    var results = typedQuery.getResultList();

    return new PageImpl<>(results, pageable, totalResults);
  }

  private List<Order> getOrderListFromPageable(CriteriaBuilder cb,
                                               Join<WorkAreaAppUserTab, WorkAreaApplicationDetailSearchItem> orderingJoin,
                                               Pageable pageable) {

    List<Order> orderList = new ArrayList<>();

    pageable.getSort().iterator().forEachRemaining(springOrder -> {
      Order order;

      var dataType = orderingJoin.get(springOrder.getProperty()).getJavaType();

      if (dataType.equals(Instant.class)) {
        order = springOrderToJpaOrder(cb, orderingJoin, springOrder, MIN_SORT_INSTANT, MAX_SORT_INSTANT);
      } else if (dataType.equals(Integer.class)) {
        order = springOrderToJpaOrder(cb, orderingJoin, springOrder, 0, 99999);
      } else {
        throw new UnsupportedOperationException(String.format("Cannot decode class %s for default value when null", dataType));

      }

      orderList.add(order);

    });

    return orderList;

  }

  private <T> Order springOrderToJpaOrder(CriteriaBuilder cb,
                                          Join<WorkAreaAppUserTab, WorkAreaApplicationDetailSearchItem> orderingJoin,
                                          Sort.Order springOrder,
                                          T minimumWhenNull,
                                          T maximumWhenNull) {

    // out of the box QueryUtils.toOrders() ignores null handling definition and default behaviour is always nulls last
    if (springOrder.getDirection().isAscending()
        && springOrder.getNullHandling().equals(Sort.NullHandling.NULLS_FIRST)) {
      return cb.asc(cb.coalesce(orderingJoin.get(springOrder.getProperty()), minimumWhenNull));

    } else if (springOrder.getDirection().isAscending()) {
      return cb.asc(cb.coalesce(orderingJoin.get(springOrder.getProperty()), maximumWhenNull));

    } else if (springOrder.getDirection().isDescending()
        && springOrder.getNullHandling().equals(Sort.NullHandling.NULLS_FIRST)) {
      return cb.desc(cb.coalesce(orderingJoin.get(springOrder.getProperty()), maximumWhenNull));

    } else {
      return cb.desc(cb.coalesce(orderingJoin.get(springOrder.getProperty()), maximumWhenNull));
    }

  }

}
