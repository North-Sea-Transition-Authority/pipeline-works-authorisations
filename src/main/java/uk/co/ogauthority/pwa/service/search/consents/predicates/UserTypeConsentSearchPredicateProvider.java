package uk.co.ogauthority.pwa.service.search.consents.predicates;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgGrp;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgGrp_;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class UserTypeConsentSearchPredicateProvider implements ConsentSearchPredicateProvider {

  private final EntityManager entityManager;
  private final UserTypeService userTypeService;

  @Autowired
  public UserTypeConsentSearchPredicateProvider(EntityManager entityManager,
                                                UserTypeService userTypeService) {
    this.entityManager = entityManager;
    this.userTypeService = userTypeService;
  }

  @Override
  public boolean shouldApplyToSearch(ConsentSearchParams searchParams, ConsentSearchContext searchContext) {
    return userTypeService.getPriorityUserTypeOrThrow(searchContext.getUser()) == UserType.INDUSTRY;
  }

  @Override
  public Predicate getPredicate(ConsentSearchParams searchParams,
                                ConsentSearchContext searchContext,
                                CriteriaQuery<?> criteriaQuery,
                                Root<ConsentSearchItem> queryRoot) {

    var cb = entityManager.getCriteriaBuilder();

    var subQuery = criteriaQuery.subquery(Integer.class);
    // FROM PwaHolderOrgGrp
    Root<PwaHolderOrgGrp> holderOrgGrp = subQuery.from(PwaHolderOrgGrp.class);

    // SELECT pwa_id
    subQuery.select(holderOrgGrp.get(PwaHolderOrgGrp_.PWA_ID));

    // WHERE org_grp_id IN <set of org groups user has access to>
    subQuery.where(cb.in(holderOrgGrp.get(PwaHolderOrgGrp_.ORG_GRP_ID)).value(searchContext.getOrgGroupIdsUserInTeamFor()));

    return cb.and(cb.in(queryRoot.get(ConsentSearchItem_.PWA_ID)).value(subQuery));

  }

}
