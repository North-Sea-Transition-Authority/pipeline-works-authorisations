package uk.co.ogauthority.pwa.externalapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail_;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail_;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline_;

@Repository
class PipelineDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  PipelineDtoRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  List<PipelineDto> searchPipelineDtos(List<Integer> pipelineIds,
                                       String pipelineNumber,
                                       String pwaReference) {

    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var criteriaQuery = criteriaBuilder.createQuery(PipelineDto.class);
    Root<PipelineDetail> pipelineDetailRoot = criteriaQuery.from(PipelineDetail.class);

    Join<PipelineDetail, Pipeline> pipelineDetailToPipelineJoin = pipelineDetailRoot.join(PipelineDetail_.PIPELINE);

    Root<MasterPwaDetail> masterPwaDetailRoot = criteriaQuery.from(MasterPwaDetail.class);
    Predicate pipelineMasterPwaDetailJoin = criteriaBuilder.and(
        criteriaBuilder.equal(
            pipelineDetailToPipelineJoin.get(Pipeline_.MASTER_PWA),
            masterPwaDetailRoot.get(MasterPwaDetail_.MASTER_PWA)
    ));

    var predicates = new ArrayList<Predicate>();
    predicates.add(pipelineMasterPwaDetailJoin);

    if (Objects.nonNull(pipelineIds)) {
      var pipelineIdsPredicate = OraclePartitionUtil.partitionedList(pipelineIds)
          .stream()
          .map(pipelineIdsSubList -> criteriaBuilder.in(pipelineDetailToPipelineJoin.get(Pipeline_.ID)).value(pipelineIdsSubList))
          .toArray(Predicate[]::new);
      predicates.add(criteriaBuilder.or(pipelineIdsPredicate));
    }

    if (Objects.nonNull(pipelineNumber)) {
      predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(pipelineDetailRoot.get(PipelineDetail_.PIPELINE_NUMBER)),
          getSqlLikeString(pipelineNumber))
      );
    }

    if (Objects.nonNull(pwaReference)) {
      predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(masterPwaDetailRoot.get(MasterPwaDetail_.REFERENCE)),
          getSqlLikeString(pwaReference))
      );
    }

    predicates.add(
        criteriaBuilder.and(
            criteriaBuilder.isNull(masterPwaDetailRoot.get(MasterPwaDetail_.END_INSTANT))
        )
    );

    predicates.add(
        criteriaBuilder.and(
            criteriaBuilder.equal(pipelineDetailRoot.get(PipelineDetail_.TIP_FLAG), 1)
        )
    );

    criteriaQuery.multiselect(
        pipelineDetailToPipelineJoin.get(Pipeline_.id),
        pipelineDetailRoot.get(PipelineDetail_.pipelineNumber),
        masterPwaDetailRoot.get(MasterPwaDetail_.reference)
        )
        .where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

  private String getSqlLikeString(String target) {
    return Optional.ofNullable(target)
        .map(string -> "%" + string.toLowerCase() + "%")
        .orElse(null);
  }
}
