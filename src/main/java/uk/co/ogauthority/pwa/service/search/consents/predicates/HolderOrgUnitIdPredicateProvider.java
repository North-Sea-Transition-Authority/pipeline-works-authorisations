package uk.co.ogauthority.pwa.service.search.consents.predicates;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit_;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

@Service
public class HolderOrgUnitIdPredicateProvider implements ConsentSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public HolderOrgUnitIdPredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean shouldApplyToSearch(ConsentSearchParams searchParams, ConsentSearchContext searchContext) {
    return searchParams.getHolderOrgUnitId() != null;
  }

  @Override
  public Predicate getPredicate(ConsentSearchParams searchParams,
                                ConsentSearchContext searchContext,
                                CriteriaQuery<?> criteriaQuery,
                                Root<ConsentSearchItem> queryRoot) {

    var cb = entityManager.getCriteriaBuilder();

    var subQuery = criteriaQuery.subquery(Integer.class);
    // FROM PwaHolderOrgUnit
    Root<PwaHolderOrgUnit> holderOrgUnit = subQuery.from(PwaHolderOrgUnit.class);

    // SELECT pwa_id
    subQuery.select(holderOrgUnit.get(PwaHolderOrgUnit_.PWA_ID));

    // WHERE ou_id = <ou_id selected by user>
    subQuery.where(cb.equal(holderOrgUnit.get(PwaHolderOrgUnit_.OU_ID), searchParams.getHolderOrgUnitId()));

    return cb.and(cb.in(queryRoot.get(ConsentSearchItem_.PWA_ID)).value(subQuery));

  }

}
