package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

@Service
public class ApplicationStatusPredicateProvider implements ApplicationSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public ApplicationStatusPredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {
    return true;
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {

    var cb = entityManager.getCriteriaBuilder();
    var excludeAppsWithStatus = BooleanUtils.isTrue(applicationSearchParameters.getIncludeCompletedOrWithdrawnApps())
        ? PwaApplicationStatus.getStatusesWithState(ApplicationState.DELETED)
        : PwaApplicationStatus.getStatusesWithState(ApplicationState.DELETED, ApplicationState.COMPLETED);


    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaApplicationDetail> subRoot = subQuery.from(PwaApplicationDetail.class);

    subQuery.select(cb.literal(1));
    subQuery.where(cb.and(
        cb.equal(
            searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID),
            subRoot.get(PwaApplicationDetail_.PWA_APPLICATION)
        ),
        cb.in(subRoot.get(PwaApplicationDetail_.STATUS)).value(excludeAppsWithStatus)
    ));

    return cb.not(cb.exists(subQuery));
  }
}
