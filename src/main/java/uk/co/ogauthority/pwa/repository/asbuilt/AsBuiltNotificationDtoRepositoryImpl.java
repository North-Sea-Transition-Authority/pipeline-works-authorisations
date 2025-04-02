package uk.co.ogauthority.pwa.repository.asbuilt;

import static java.util.stream.Collectors.toSet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit_;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class AsBuiltNotificationDtoRepositoryImpl implements AsBuiltNotificationDtoRepository {

  private final EntityManager entityManager;

  private final PwaHolderTeamService pwaHolderTeamService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public AsBuiltNotificationDtoRepositoryImpl(EntityManager entityManager,
                                              PwaHolderTeamService pwaHolderTeamService,
                                              TeamQueryService teamQueryService) {
    this.entityManager = entityManager;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.teamQueryService = teamQueryService;
  }

  private boolean isUserAsBuiltNotificationAdmin(AuthenticatedUserAccount user) {
    return teamQueryService.userHasStaticRole((long) user.getWuaId(), TeamType.REGULATOR, Role.AS_BUILT_NOTIFICATION_ADMIN);
  }

  @Override
  public Page<AsBuiltNotificationWorkareaView> findAllAsBuiltNotificationsForUser(AuthenticatedUserAccount authenticatedUserAccount,
                                                                                  Pageable pageable) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<AsBuiltNotificationWorkareaView> cq = cb.createQuery(AsBuiltNotificationWorkareaView.class);

    Root<AsBuiltNotificationWorkareaView> root = cq.from(AsBuiltNotificationWorkareaView.class);

    //count query
    CriteriaQuery<Long> countResultsQuery = cb.createQuery(Long.class);
    Root<AsBuiltNotificationWorkareaView> countQueryRoot = countResultsQuery.from(AsBuiltNotificationWorkareaView.class);
    countResultsQuery.select(cb.count(countQueryRoot));

    List<Predicate> predicates = new ArrayList<>();
    List<Predicate> countPredicates = new ArrayList<>();

    addPredicateToFilterBasedOnUserType(predicates, authenticatedUserAccount, cq, root);
    addPredicateToFilterBasedOnUserType(countPredicates, authenticatedUserAccount, countResultsQuery, countQueryRoot);

    addPredicateToFilterGroupsWithCompleteStatus(predicates, root);
    addPredicateToFilterGroupsWithCompleteStatus(countPredicates, countQueryRoot);

    //needs to apply same predicates to both result query and count query
    cq.where(predicates.toArray(new Predicate[]{}));
    countResultsQuery.where(countPredicates.toArray(new Predicate[]{}));

    var resultsQuery = createAsBuiltWorkAreaResultsQuery(cb, cq, root, pageable);
    var totalResultCount = entityManager.createQuery(countResultsQuery).getSingleResult();

    return new PageImpl<>(resultsQuery.getResultList(), pageable, totalResultCount);
  }

  private List<PortalOrganisationGroup> getOrgGroupsUserCanAccess(AuthenticatedUserAccount user) {
    return pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(user, Set.of(Role.AS_BUILT_NOTIFICATION_SUBMITTER));
  }

  private Predicate getHolderOrgApplicationsPredicate(List<PortalOrganisationGroup> organisationGroups,
                                                      CriteriaQuery<?> searchCoreQuery,
                                                      Root<AsBuiltNotificationWorkareaView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var holderOrgGroupIds = organisationGroups
        .stream()
        .map(PortalOrganisationGroup::getOrgGrpId)
        .collect(toSet());

    if (holderOrgGroupIds.isEmpty()) {
      // always false
      return cb.isFalse(cb.literal(true));
    }

    Subquery<Integer> applicationSubQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaHolderOrgUnit> holderOrgUnitRoot = applicationSubQuery.from(PwaHolderOrgUnit.class);

    // return master pwa ids from subquery where a holder is one of the org units within searchers org group(s)
    applicationSubQuery.select(holderOrgUnitRoot.get(PwaHolderOrgUnit_.PWA_ID));
    applicationSubQuery.where(cb.in(holderOrgUnitRoot.get(PwaHolderOrgUnit_.ORG_GRP_ID)).value(holderOrgGroupIds));

    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(applicationSubQuery);
  }

  private void addPredicateToFilterBasedOnUserType(
      List<Predicate> predicates,
      AuthenticatedUserAccount user,
      CriteriaQuery<?> query,
      Root<AsBuiltNotificationWorkareaView> root
  ) {
    if (!isUserAsBuiltNotificationAdmin(user)) {
      var groupsUserCanAccess = getOrgGroupsUserCanAccess(user);
      predicates.add(getHolderOrgApplicationsPredicate(groupsUserCanAccess, query, root));
    }
  }

  private void addPredicateToFilterGroupsWithCompleteStatus(
      List<Predicate> predicates,
      Root<AsBuiltNotificationWorkareaView> root
  ) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    predicates.add(cb.notEqual(root.get("status"), AsBuiltNotificationGroupStatus.COMPLETE));
  }

  private TypedQuery<AsBuiltNotificationWorkareaView> createAsBuiltWorkAreaResultsQuery(CriteriaBuilder cb,
                                                                  CriteriaQuery<AsBuiltNotificationWorkareaView> cq,
                                                                  Root<AsBuiltNotificationWorkareaView> asBuiltNotificationWorkareaViewRoot,
                                                                  Pageable pageable) {
    TypedQuery<AsBuiltNotificationWorkareaView> query = entityManager.createQuery(cq
        .orderBy(cb.asc(asBuiltNotificationWorkareaViewRoot.get("deadlineDate")))
    );
    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());
    return query;
  }

}