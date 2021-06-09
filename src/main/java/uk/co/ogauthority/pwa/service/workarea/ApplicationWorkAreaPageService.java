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
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppLifecycleEvent;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppLifecycleEvent_;

@Service
class ApplicationWorkAreaPageService {

  private static final Instant MIN_SORT_INSTANT = LocalDateTime.of(0, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  private static final Instant MAX_SORT_INSTANT = LocalDateTime.of(4000, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);

  private final EntityManager entityManager;

  @Autowired
  public ApplicationWorkAreaPageService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Page<WorkAreaApplicationDetailSearchItem> getUsersWorkAreaTabContentsWhereSubscribedEventsExist(
      WorkAreaContext workAreaContext,
      Pageable pageable) {

    return createPagedQuery(
        workAreaContext,
        true,
        pageable

    );

  }

  public Page<WorkAreaApplicationDetailSearchItem> getUsersWorkAreaTabContentsWhereSubscribedEventsDoNotExist(
      WorkAreaContext workAreaContext,
      Pageable pageable) {

    return createPagedQuery(
        workAreaContext,
        false,
        pageable
    );
  }

  // helper method to apply a 'NOT' around a predicate. helpers to interpret the 'subscriptionEventsMustExist' boolean flag
  private Predicate invertPredicate(boolean overrideInvert, Predicate predicate) {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    return overrideInvert
        ? predicate
        : criteriaBuilder.not(predicate);
  }

  // helper to get the lifecycle events for a specific subscriber type and
  // create a subquery that returns the application id where those events exist.
  private <T_SELECT> Subquery<Integer> getAppEventSubQuery(ApplicationEventSubscriberType applicationEventSubscriberType,
                                                CriteriaQuery<T_SELECT> query) {
    var cb = entityManager.getCriteriaBuilder();
    var subscriberTypeEvents = ApplicationLifecycleEvent
        .getForAttentionEventsWhereSubscriberIs(applicationEventSubscriberType);

    Subquery<Integer> appIdAppEventSubQuery = query.subquery(Integer.class);
    Root<WorkAreaAppLifecycleEvent> appEventRoot = appIdAppEventSubQuery.from(WorkAreaAppLifecycleEvent.class);
    appIdAppEventSubQuery.select(appEventRoot.get(WorkAreaAppLifecycleEvent_.PWA_APPLICATION_ID));
    appIdAppEventSubQuery.where(
        cb.in(appEventRoot.get(WorkAreaAppLifecycleEvent_.EVENT_FLAG)).value(subscriberTypeEvents)
    );

    return appIdAppEventSubQuery;
  }



  private <T_ROOT, T_SELECT> Predicate getAppContactSubscriptionTypePredicate(Root<T_ROOT> root,
                                                                              CriteriaQuery<T_SELECT> query,
                                                                              WorkAreaContext workAreaContext,
                                                                              boolean subscriptionEventsMustExist) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate appContactAppRestriction = cb.isFalse(cb.literal(true));

    if (workAreaContext.containsAppEventSubscriberType(ApplicationEventSubscriberType.APPLICATION_CONTACT)) {

      Subquery<Integer> contactAppIdSubQuery = query.subquery(Integer.class);

      Root<PwaContact> contactRoot = contactAppIdSubQuery.from(PwaContact.class);
      Join<PwaContact, PwaApplication> contactToApplicationJoin = contactRoot.join(PwaContact_.pwaApplication);
      Path<Integer> appIdPath = contactToApplicationJoin.get(PwaApplication_.ID);
      var appIdWithEventSubquery = getAppEventSubQuery(ApplicationEventSubscriberType.APPLICATION_CONTACT, query);

      contactAppIdSubQuery.select(appIdPath);
      contactAppIdSubQuery.where(cb.and(
          cb.equal(contactRoot.get(PwaContact_.PERSON),
              workAreaContext.getAuthenticatedUserAccount().getLinkedPerson()),
          invertPredicate(subscriptionEventsMustExist, cb.in(appIdPath).value(appIdWithEventSubquery))
      ));

      appContactAppRestriction = cb.in(root.get(WorkAreaApplicationDetailSearchItem_.PWA_APPLICATION_ID)).value(
          contactAppIdSubQuery);

    }

    return appContactAppRestriction;

  }

  private <T_ROOT, T_SELECT> Predicate getCaseOfficerSubscriptionTypePredicate(Root<T_ROOT> root,
                                                                              CriteriaQuery<T_SELECT> query,
                                                                              WorkAreaContext workAreaContext,
                                                                              boolean subscriptionEventsMustExist) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate caseOfficerAppRestriction = cb.isFalse(cb.literal(true));

    if (workAreaContext.containsAppEventSubscriberType(ApplicationEventSubscriberType.CASE_OFFICER)) {
      Subquery<Integer> assignmentSubQuery = query.subquery(Integer.class);
      Root<Assignment> assignmentRoot = assignmentSubQuery.from(Assignment.class);
      var caseOfficerEventSubQuery = getAppEventSubQuery(ApplicationEventSubscriberType.CASE_OFFICER, query);

      Path<Integer> appIdPath = assignmentRoot.get(Assignment_.BUSINESS_KEY);

      assignmentSubQuery.select(cb.toInteger(appIdPath));
      assignmentSubQuery.where(cb.and(
          cb.equal(assignmentRoot.get(Assignment_.ASSIGNEE_PERSON_ID), workAreaContext.getPersonId())),
          cb.equal(assignmentRoot.get(Assignment_.WORKFLOW_ASSIGNMENT), WorkflowAssignment.CASE_OFFICER),
          invertPredicate(subscriptionEventsMustExist, cb.in(appIdPath).value(caseOfficerEventSubQuery))
      );
      caseOfficerAppRestriction = cb.in(root.get(WorkAreaApplicationDetailSearchItem_.PWA_APPLICATION_ID)).value(
          assignmentSubQuery);
    }

    return caseOfficerAppRestriction;
  }

  private <T_ROOT, T_SELECT> Predicate getPwaManagerSubscriptionTypePredicate(Root<T_ROOT> root,
                                                                               CriteriaQuery<T_SELECT> query,
                                                                               WorkAreaContext workAreaContext,
                                                                               boolean subscriptionEventsMustExist) {
    var cb = entityManager.getCriteriaBuilder();

    Predicate pwaManagerAppPredicate = cb.isFalse(cb.literal(true));
    if (workAreaContext.containsAppEventSubscriberType(ApplicationEventSubscriberType.PWA_MANAGER)) {
      // pwa managers want to know about any app if there are suitable app events.

      Subquery<Integer> appEventSubQuery = query.subquery(Integer.class);

      Root<WorkAreaAppLifecycleEvent> appLifecycleEventRoot = appEventSubQuery.from(WorkAreaAppLifecycleEvent.class);

      var pwaManagerEvents = ApplicationLifecycleEvent
          .getForAttentionEventsWhereSubscriberIs(ApplicationEventSubscriberType.PWA_MANAGER);

      appEventSubQuery.select(appLifecycleEventRoot.get(WorkAreaAppLifecycleEvent_.PWA_APPLICATION_ID));
      appEventSubQuery.where(
          invertPredicate(subscriptionEventsMustExist,
              cb.in(appLifecycleEventRoot.get(WorkAreaAppLifecycleEvent_.EVENT_FLAG)).value(pwaManagerEvents))
      );

      pwaManagerAppPredicate = cb.and(

          cb.in(root.get(WorkAreaApplicationDetailSearchItem_.PWA_APPLICATION_ID)).value(appEventSubQuery),
          // this is required because pwa manager shouldn't see drafts unless app contact
          cb.not(cb.equal(root.get(WorkAreaApplicationDetailSearchItem_.PAD_STATUS), PwaApplicationStatus.DRAFT))
      );
    }

    return pwaManagerAppPredicate;
  }

  // this helper creates the predicate that does all the core heavy lifting so we return the applications for a user where
  // apps they should be seeing in the workarea are filtered based on the existance (or not) of the lifecycle events they subscribe to.
  private <T_ROOT, T_SELECT> Predicate getSubscriptionTypePredicate(
      Root<T_ROOT> root,
      CriteriaQuery<T_SELECT> query,
      WorkAreaContext workAreaContext,
      boolean subscriptionEventsMustExist) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    Predicate appContactAppRestriction = getAppContactSubscriptionTypePredicate(
        root, query, workAreaContext, subscriptionEventsMustExist
    );

    Predicate caseOfficerAppRestriction = getCaseOfficerSubscriptionTypePredicate(
        root, query, workAreaContext, subscriptionEventsMustExist
    );

    Predicate pwaManagerAppPredicate = getPwaManagerSubscriptionTypePredicate(
        root, query, workAreaContext, subscriptionEventsMustExist
    );

    Predicate userAppRestrictionWithAppEventsPredicate = cb.or(
        pwaManagerAppPredicate, appContactAppRestriction, caseOfficerAppRestriction
    );

    return userAppRestrictionWithAppEventsPredicate;

  }

  // using non parameterised CriteriaQuery to be able to use same predicate provider for count and results query
  private Page<WorkAreaApplicationDetailSearchItem> createPagedQuery(
      WorkAreaContext workAreaContext,
      boolean subscriptionEventsMustExist,
      Pageable pageable) {
    var cb = entityManager.getCriteriaBuilder();

    // have to work out total results by doing a count query based off the search query predicate,
    // 1. get count of app possible results given the predicate
    CriteriaQuery<Long> countResultsQuery = cb.createQuery(Long.class);
    Root<WorkAreaApplicationDetailSearchItem> countResultsRoot = countResultsQuery.from(
        WorkAreaApplicationDetailSearchItem.class);
    countResultsQuery
        .select(cb.count(countResultsRoot))
        .where(
            getSubscriptionTypePredicate(
                countResultsRoot,
                countResultsQuery,
                workAreaContext,
                subscriptionEventsMustExist
            )
        );
    var totalResults = entityManager.createQuery(countResultsQuery).getSingleResult();

    // 2. Create results query using predicate
    CriteriaQuery<WorkAreaApplicationDetailSearchItem> searchResultsQuery = cb.createQuery(
        WorkAreaApplicationDetailSearchItem.class);
    Root<WorkAreaApplicationDetailSearchItem> searchResultsRoot = searchResultsQuery.from(
        WorkAreaApplicationDetailSearchItem.class);
    searchResultsQuery.where(
        getSubscriptionTypePredicate(
            searchResultsRoot,
            searchResultsQuery,
            workAreaContext,
            subscriptionEventsMustExist
        )
    );

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

  private List<Order> getOrderListFromPageable(CriteriaBuilder cb, Root<WorkAreaApplicationDetailSearchItem> root,
                                               Pageable pageable) {

    List<Order> orderList = new ArrayList<>();

    pageable.getSort().iterator().forEachRemaining(springOrder -> {
      Order order;

      var dataType = root.get(springOrder.getProperty()).getJavaType();
      if (!dataType.equals(Instant.class)) {
        throw new UnsupportedOperationException("Cannot decode class %s for default value when null");
      }

      // oh god I'm sorry, hardcode in default sort value when null and limit to just Instant type sorting for now.
      // out of the box QueryUtils.toOrders() ignores null handling definition and default behaviour is always nulls last
      if (springOrder.getDirection().isAscending() && springOrder.getNullHandling().equals(
          Sort.NullHandling.NULLS_FIRST)) {
        order = cb.asc(cb.coalesce(root.get(springOrder.getProperty()), MIN_SORT_INSTANT));
      } else if (springOrder.getDirection().isAscending()) {
        order = cb.asc(cb.coalesce(root.get(springOrder.getProperty()), MAX_SORT_INSTANT));
      } else if (springOrder.getDirection().isDescending() && springOrder.getNullHandling().equals(
          Sort.NullHandling.NULLS_FIRST)) {
        order = cb.desc(cb.coalesce(root.get(springOrder.getProperty()), MAX_SORT_INSTANT));
      } else {
        order = cb.desc(cb.coalesce(root.get(springOrder.getProperty()), MIN_SORT_INSTANT));
      }

      orderList.add(order);

    });

    return orderList;

  }

}
