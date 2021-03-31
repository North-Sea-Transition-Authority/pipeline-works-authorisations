package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit_;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;


@Service
public class HolderOrganisationPredicateProvider implements ApplicationSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public HolderOrganisationPredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {
    return applicationSearchParameters.getHolderOrgUnitId() != null;
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {

    var cb = entityManager.getCriteriaBuilder();

    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaHolderOrgUnit> pwaHolderOrgUnitRoot = subQuery.from(PwaHolderOrgUnit.class);

    subQuery.select(pwaHolderOrgUnitRoot.get(PwaHolderOrgUnit_.PWA_ID));

    subQuery.where(
        cb.equal(
            pwaHolderOrgUnitRoot.get(PwaHolderOrgUnit_.OU_ID),
            applicationSearchParameters.getHolderOrgUnitId()
        )
    );

    return cb.and(cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(subQuery));
  }
}
