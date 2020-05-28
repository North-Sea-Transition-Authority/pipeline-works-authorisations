package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

@Repository
public class PadPipelineDtoRepositoryImpl implements PadPipelineDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadPipelineDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  private List<PadPipelineSummaryDto> getPipelineSummaryDtosByAppDetailAndOptionalPadPipeline(
      PwaApplicationDetail detail, PadPipeline padPipeline) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto(" +
            "  pp.id " +
            ", p.id " +
            ", pp.pipelineType " +
            ", pp.pipelineRef " +
            ", pp.length " +
            ", pp.componentPartsDescription " +
            ", pp.productsToBeConveyed " +
            ", COUNT(ppi) " +
            // From info.
            ", pp.fromLocation " +
            ", pp.fromLatitudeDegrees " +
            ", pp.fromLatitudeMinutes " +
            ", pp.fromLatitudeSeconds " +
            ", pp.fromLatitudeDirection " +
            ", pp.fromLongitudeDegrees " +
            ", pp.fromLongitudeMinutes " +
            ", pp.fromLongitudeSeconds " +
            ", pp.fromLongitudeDirection " +
            // To info.
            ", pp.toLocation " +
            ", pp.toLatitudeDegrees " +
            ", pp.toLatitudeMinutes " +
            ", pp.toLatitudeSeconds " +
            ", pp.toLatitudeDirection " +
            ", pp.toLongitudeDegrees " +
            ", pp.toLongitudeMinutes " +
            ", pp.toLongitudeSeconds " +
            ", pp.toLongitudeDirection " +
            ") " +
            "FROM uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline pp " +
            "LEFT JOIN uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline p ON pp.pipeline = p " +
            "LEFT JOIN uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent ppi " +
            "ON pp.id = ppi.padPipeline.id " +
            "WHERE pp.pwaApplicationDetail = :detail " +
            "AND (:padPipelineLineFilter IS NULL OR pp = :padPipelineLineFilter) " +
            "GROUP BY " +
            "  pp.id " +
            ", p.id " +
            ", pp.pipelineType " +
            ", pp.pipelineRef " +
            ", pp.length " +
            ", pp.componentPartsDescription " +
            ", pp.productsToBeConveyed " +
            // From info.
            ", pp.fromLocation " +
            ", pp.fromLatitudeDegrees " +
            ", pp.fromLatitudeMinutes " +
            ", pp.fromLatitudeSeconds " +
            ", pp.fromLatitudeDirection " +
            ", pp.fromLongitudeDegrees " +
            ", pp.fromLongitudeMinutes " +
            ", pp.fromLongitudeSeconds " +
            ", pp.fromLongitudeDirection " +
            // To info.
            ", pp.toLocation " +
            ", pp.toLatitudeDegrees " +
            ", pp.toLatitudeMinutes " +
            ", pp.toLatitudeSeconds " +
            ", pp.toLatitudeDirection " +
            ", pp.toLongitudeDegrees " +
            ", pp.toLongitudeMinutes " +
            ", pp.toLongitudeSeconds " +
            ", pp.toLongitudeDirection ",
        PadPipelineSummaryDto.class)
        .setParameter("detail", detail)
        .setParameter("padPipelineLineFilter", padPipeline)
        .getResultList();
  }

  @Override
  public Optional<PadPipelineSummaryDto> findPipelineAsSummaryDtoByPadPipeline(PadPipeline padPipeline) {
    return getPipelineSummaryDtosByAppDetailAndOptionalPadPipeline(padPipeline.getPwaApplicationDetail(), padPipeline)
        .stream()
        .findFirst();
  }

  @Override
  public List<PadPipelineSummaryDto> findAllPipelinesAsSummaryDtoByPwaApplicationDetail(PwaApplicationDetail detail) {

    return getPipelineSummaryDtosByAppDetailAndOptionalPadPipeline(detail, null);

  }

  @Override
  public Long countAllWithNoIdentsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT COUNT(pp) " +
        "FROM uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline pp " +
        "LEFT JOIN uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent ppi " +
        "ON pp.id = ppi.padPipeline.id " +
        "WHERE pp.pwaApplicationDetail = :detail " +
        "AND ppi IS NULL", Long.class)
        .setParameter("detail", pwaApplicationDetail)
        .getSingleResult();
  }
}
