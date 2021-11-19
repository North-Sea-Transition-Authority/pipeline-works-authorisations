package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;


@Service
public class ApplicationCaseOfficerPredicateProvider implements ApplicationSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public ApplicationCaseOfficerPredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {
    return applicationSearchParameters.getCaseOfficerPersonId() != null;
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {

    var cb = entityManager.getCriteriaBuilder();

    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaAppAssignmentView> pwaAppAssignmentViewRoot = subQuery.from(PwaAppAssignmentView.class);

    subQuery.select(pwaAppAssignmentViewRoot.get(PwaAppAssignmentView_.PWA_APPLICATION_ID));

    subQuery.where(cb.and(
        cb.equal(
            pwaAppAssignmentViewRoot.get(PwaAppAssignmentView_.assigneePersonId),
            applicationSearchParameters.getCaseOfficerPersonId()
        ),
        cb.equal(pwaAppAssignmentViewRoot.get(PwaAppAssignmentView_.assignment), WorkflowAssignment.CASE_OFFICER)
    ));

    return cb.and(cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID)).value(subQuery));
  }
}
