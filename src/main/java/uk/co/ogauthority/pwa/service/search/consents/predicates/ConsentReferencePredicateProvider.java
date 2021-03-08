package uk.co.ogauthority.pwa.service.search.consents.predicates;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaConsentView;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaConsentView_;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

@Service
public class ConsentReferencePredicateProvider implements ConsentSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public ConsentReferencePredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean shouldApplyToSearch(ConsentSearchParams searchParams, ConsentSearchContext searchContext) {
    return !StringUtils.isBlank(searchParams.getConsentReference());
  }

  @Override
  public Predicate getPredicate(ConsentSearchParams searchParams,
                                ConsentSearchContext searchContext,
                                CriteriaQuery<ConsentSearchItem> criteriaQuery,
                                Root<ConsentSearchItem> queryRoot) {

    var cb = entityManager.getCriteriaBuilder();

    var subQuery = criteriaQuery.subquery(Integer.class);
    // FROM PwaConsentView
    Root<PwaConsentView> pwaConsentView = subQuery.from(PwaConsentView.class);

    // SELECT pwa_id
    subQuery.select(pwaConsentView.get(PwaConsentView_.PWA_ID));

    // WHERE LOWER(consent_reference) LIKE '%' || LOWER(<user consent ref to filter>) || '%'
    subQuery.where(cb.like(
        cb.lower(pwaConsentView.get(PwaConsentView_.CONSENT_REFERENCE)),
        "%" + searchParams.getConsentReference().toLowerCase() + "%"));

    return cb.and(cb.in(queryRoot.get(ConsentSearchItem_.PWA_ID)).value(subQuery));

  }

}
