package uk.co.ogauthority.pwa.service.search.consents.predicates;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa_;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail_;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline_;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem_;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

@Service
public class PipelineReferencePredicateProvider implements ConsentSearchPredicateProvider {

  private final EntityManager entityManager;

  @Autowired
  public PipelineReferencePredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean shouldApplyToSearch(ConsentSearchParams searchParams, ConsentSearchContext searchContext) {
    return !StringUtils.isBlank(searchParams.getPipelineReference());
  }

  @Override
  public Predicate getPredicate(ConsentSearchParams searchParams,
                                ConsentSearchContext searchContext,
                                CriteriaQuery<ConsentSearchItem> criteriaQuery,
                                Root<ConsentSearchItem> queryRoot) {

    var cb = entityManager.getCriteriaBuilder();

    var masterPwaSubQuery = criteriaQuery.subquery(Integer.class);
    Root<PipelineDetail> pipelineDetailRoot = masterPwaSubQuery.from(PipelineDetail.class);

    Join<PipelineDetail, Pipeline> pipelineDetailToPipelineJoin = pipelineDetailRoot.join(PipelineDetail_.PIPELINE);
    Join<Pipeline, MasterPwa> pipelineToMasterPwaJoin = pipelineDetailToPipelineJoin.join(Pipeline_.MASTER_PWA);
    masterPwaSubQuery.select(pipelineToMasterPwaJoin.get(MasterPwa_.ID));

    masterPwaSubQuery.where(cb.and(
        cb.like(cb.lower(pipelineDetailRoot.get(PipelineDetail_.PIPELINE_NUMBER)),
            "%" + searchParams.getPipelineReference().toLowerCase() + "%")),
        cb.isTrue(pipelineDetailRoot.get(PipelineDetail_.TIP_FLAG))
    );

    return cb.and(cb.in(queryRoot.get(ConsentSearchItem_.PWA_ID)).value(masterPwaSubQuery));
  }

}
