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
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail_;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa_;
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

    Root<MasterPwa> masterPwaRoot = criteriaQuery.from(MasterPwa.class);
    Join<MasterPwa, MasterPwaDetail> masterPwaMasterPwaDetailJoin = masterPwaRoot.join(MasterPwa_.MASTER_PWA_DETAILS);
    Join<MasterPwa, Pipeline> masterPwaPipelineJoin = masterPwaRoot.join(MasterPwa_.PIPELINES);
    Join<Pipeline, PipelineDetail> pipelinePipelineDetailJoin = masterPwaPipelineJoin.join(Pipeline_.PIPELINE_DETAILS);

    var predicates = new ArrayList<Predicate>();

    if (Objects.nonNull(pipelineIds)) {
      var pipelineIdsPredicate = OraclePartitionUtil.partitionedList(pipelineIds)
          .stream()
          .map(pipelineIdsSubList -> criteriaBuilder.in(masterPwaPipelineJoin.get(Pipeline_.ID)).value(pipelineIdsSubList))
          .toArray(Predicate[]::new);
      predicates.add(criteriaBuilder.or(pipelineIdsPredicate));
    }

    if (Objects.nonNull(pipelineNumber)) {
      predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(pipelinePipelineDetailJoin.get(PipelineDetail_.PIPELINE_NUMBER)),
          getSqlLikeString(pipelineNumber))
      );
    }

    if (Objects.nonNull(pwaReference)) {
      predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(masterPwaMasterPwaDetailJoin.get(MasterPwaDetail_.REFERENCE)),
          getSqlLikeString(pwaReference))
      );
    }

    predicates.add(
        criteriaBuilder.and(
            criteriaBuilder.isNull(masterPwaMasterPwaDetailJoin.get(MasterPwaDetail_.END_INSTANT))
        )
    );

    predicates.add(
        criteriaBuilder.and(
            criteriaBuilder.equal(pipelinePipelineDetailJoin.get(PipelineDetail_.TIP_FLAG), 1)
        )
    );

    criteriaQuery.multiselect(
        masterPwaPipelineJoin.get(Pipeline_.id),
        pipelinePipelineDetailJoin.get(PipelineDetail_.pipelineNumber),
        masterPwaMasterPwaDetailJoin.get(MasterPwaDetail_.reference)
      )
      .where(predicates.toArray(new Predicate[0]))
      .orderBy(criteriaBuilder.asc(masterPwaPipelineJoin.get(Pipeline_.id)));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

  private String getSqlLikeString(String target) {
    return Optional.ofNullable(target)
        .map(string -> "%" + string.toLowerCase() + "%")
        .orElse(null);
  }
}
