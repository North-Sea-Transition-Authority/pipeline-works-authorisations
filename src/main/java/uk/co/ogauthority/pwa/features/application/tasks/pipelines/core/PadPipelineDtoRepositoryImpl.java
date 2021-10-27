package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

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
            "SELECT new uk.co.ogauthority.pwa.features.application.tasks.pipelines.core(" +
            "  pp.id " +
            ", p.id " +
            ", pp.pipelineType " +
            ", pp.pipelineRef " +
            ", pp.temporaryRef" +
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
            ", pp.maxExternalDiameter " +
            ", pp.pipelineInBundle " +
            ", pp.bundleName " +
            ", pp.pipelineFlexibility" +
            ", pp.pipelineMaterial " +
            ", pp.otherPipelineMaterialUsed " +
            ", pp.trenchedBuriedBackfilled " +
            ", pp.trenchingMethodsDescription " +
            ", pp.pipelineStatus" +
            ", pp.pipelineStatusReason " +
            ", pp.alreadyExistsOnSeabed" +
            ", pp.pipelineInUse" +
            ", pp.footnote" +
            ") " +
            "FROM PadPipeline pp " +
            "JOIN Pipeline p ON pp.pipeline = p " +
            "LEFT JOIN PadPipelineIdent ppi " +
            "ON pp.id = ppi.padPipeline.id " +
            "WHERE pp.pwaApplicationDetail = :detail " +
            "AND (:padPipelineLineFilter IS NULL OR pp = :padPipelineLineFilter) " +
            "GROUP BY " +
            "  pp.id " +
            ", p.id " +
            ", pp.pipelineType " +
            ", pp.pipelineRef " +
            ", pp.temporaryRef " +
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
            ", pp.toLongitudeDirection " +
            ", pp.maxExternalDiameter " +
            ", pp.pipelineInBundle " +
            ", pp.bundleName " +
            ", pp.pipelineFlexibility " +
            ", pp.pipelineMaterial " +
            ", pp.otherPipelineMaterialUsed " +
            ", pp.trenchedBuriedBackfilled " +
            ", pp.trenchingMethodsDescription" +
            ", pp.pipelineStatus" +
            ", pp.pipelineStatusReason " +
            ", pp.alreadyExistsOnSeabed" +
            ", pp.pipelineInUse" +
            ", pp.footnote",
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
        "FROM PadPipeline pp " +
        "LEFT JOIN PadPipelineIdent ppi " +
        "ON pp.id = ppi.padPipeline.id " +
        "WHERE pp.pwaApplicationDetail = :detail " +
        "AND ppi IS NULL", Long.class)
        .setParameter("detail", pwaApplicationDetail)
        .getSingleResult();
  }

  @Override
  public List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto(" +
          "pp.pipeline.id, " +
          "pp.bundleName" +
        ") " +
        "FROM PadPipeline pp " +
        "WHERE pp.pwaApplicationDetail = :detail " +
        "AND pp.bundleName IS NOT NULL ", PipelineBundlePairDto.class)
        .setParameter("detail", pwaApplicationDetail)
        .getResultList();
  }

  @Override
  public Set<PipelineId> getMasterPipelineIdsOnApplication(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT p.id " +
        "FROM PadPipeline pp " +
        "JOIN Pipeline p ON pp.pipeline.id = p.id " +
        "WHERE pp.pwaApplicationDetail = :detail", Integer.class)
        .setParameter("detail", pwaApplicationDetail)
        .getResultStream()
        .map(PipelineId::new)
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Integer getMaxTemporaryNumberByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT NVL(MAX(pp.temporaryNumber), 0) " +
        "FROM PadPipeline pp " +
        "WHERE pp.pwaApplicationDetail = :detail", Integer.class)
        .setParameter("detail", pwaApplicationDetail)
        .getSingleResult();
  }


  @Override
  public List<PadPipeline> findApplicationsWherePipelineNumberExistsOnDraftOrLastSubmittedVersion(
      String pipelineNumber) {
    return entityManager.createQuery("" +
        "SELECT pp " +
        "FROM PadPipeline pp " +
        "JOIN PwaApplicationDetail pad ON pp.pwaApplicationDetail = pad " +
        "JOIN PwaApplication pa ON pad.pwaApplication = pa " +
        "JOIN PadVersionLookup pvl ON pa.id = pvl.pwaApplicationId " +
        "WHERE pad.versionNo IN (pvl.maxDraftVersionNo, pvl.latestSubmittedVersionNo) " +
        "AND pp.pipelineRef = :pipelineNum", PadPipeline.class)
        .setParameter("pipelineNum", pipelineNumber)
        .getResultList();
  }
}
