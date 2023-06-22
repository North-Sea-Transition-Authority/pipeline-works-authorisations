package uk.co.ogauthority.pwa.features.reassignment;

import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.CASE_OFFICER_REVIEW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab_;

@Service
public class CaseReasignmentService {

  private final EntityManager entityManager;

  public CaseReasignmentService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public List<WorkAreaApplicationDetailSearchItem> getReassignableWorkAreaItems() {
    var cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<WorkAreaApplicationDetailSearchItem> searchResultsQuery = cb.createQuery(
        WorkAreaApplicationDetailSearchItem.class);
    Root<WorkAreaAppUserTab> searchResultsRoot = searchResultsQuery.from(WorkAreaAppUserTab.class);
    Join<WorkAreaAppUserTab, WorkAreaApplicationDetailSearchItem> workAreaSearchItemJoin = searchResultsRoot
        .join(WorkAreaAppUserTab_.workAreaApplicationDetailSearchItem);

    var conditions = new ArrayList<Predicate>();
    conditions.add(cb.equal(workAreaSearchItemJoin.get(ApplicationDetailView_.PAD_STATUS), CASE_OFFICER_REVIEW));
    conditions.add(cb.isTrue(workAreaSearchItemJoin.get(ApplicationDetailView_.TIP_FLAG)));

    searchResultsQuery.select(workAreaSearchItemJoin);
    searchResultsQuery.where(conditions.toArray(new Predicate[]{}));

    var results =  entityManager.createQuery(searchResultsQuery).getResultList();
    return results;
  }

  public Map<String, String> getReassignableCheckboxMap(List<WorkAreaApplicationDetailSearchItem> workAreaItems) {
    return workAreaItems.stream()
        .collect(Collectors.toMap(
            WorkAreaApplicationDetailSearchItem::getPadReference,
            WorkAreaApplicationDetailSearchItem::getPadReference)
        );
  }
}
