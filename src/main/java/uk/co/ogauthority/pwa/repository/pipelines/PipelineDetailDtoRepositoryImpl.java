package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailSummaryDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

@Repository
public class PipelineDetailDtoRepositoryImpl implements PipelineDetailDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PipelineDetailDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto(" +
        "pd.pipeline.id, " +
        "pd.bundleName " +
        ") " +
        "FROM PipelineDetail pd " +
        "JOIN PwaConsent pc ON pd.pwaConsent = pc " +
        "WHERE pd.tipFlag = 1 " +
        "AND pc.masterPwa = :master_pwa " +
        "AND pd.bundleName IS NOT NULL ", PipelineBundlePairDto.class)
        .setParameter("master_pwa", pwaApplicationDetail.getMasterPwaApplication())
        .getResultList();
  }


  @Override
  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwa(MasterPwa masterPwa) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailSummaryDto(" +
            "  p.id " +
            ", pd.pipelineType " +
            ", pd.pipelineNumber " +
            ", pd.length " +
            ", pd.componentPartsDesc " +
            ", pd.productsToBeConveyed " +
            ", COUNT(pdi) " +
            // From info.
            ", pd.fromLocation " +
            ", pd.fromLatitudeDegrees " +
            ", pd.fromLatitudeMinutes " +
            ", pd.fromLatitudeSeconds " +
            ", pd.fromLatitudeDirection " +
            ", pd.fromLongitudeDegrees " +
            ", pd.fromLongitudeMinutes " +
            ", pd.fromLongitudeSeconds " +
            ", pd.fromLongitudeDirection " +
            // To info.
            ", pd.toLocation " +
            ", pd.toLatitudeDegrees " +
            ", pd.toLatitudeMinutes " +
            ", pd.toLatitudeSeconds " +
            ", pd.toLatitudeDirection " +
            ", pd.toLongitudeDegrees " +
            ", pd.toLongitudeMinutes " +
            ", pd.toLongitudeSeconds " +
            ", pd.toLongitudeDirection " +
            ", pd.maxExternalDiameter " +
            ", pd.pipelineInBundle " +
            ", pd.bundleName " +
            ", pd.pipelineFlexibility" +
            ", pd.pipelineMaterial " +
            ", pd.otherPipelineMaterialUsed " +
            ", pd.trenchedBuriedFilledFlag " +
            ", pd.trenchingMethodsDesc " +
            ", pd.pipelineStatus " +
            ", pd.pipelineStatusReason " +
            ") " +
            "FROM PipelineDetail pd " +
            "JOIN PwaConsent pc ON pd.pwaConsent = pc " +
            "JOIN Pipeline p ON pd.pipeline = p " +
            "LEFT JOIN PipelineDetailIdent pdi ON pd = pdi.pipelineDetail " +
            "WHERE pd.tipFlag = true " +
            "AND pc.masterPwa = :master_pwa " +
            "GROUP BY " +
            "  pd.id " +
            ", p.id " +
            ", pd.pipelineType " +
            ", pd.pipelineNumber " +
            ", pd.length " +
            ", pd.componentPartsDesc " +
            ", pd.productsToBeConveyed " +
            // From info.
            ", pd.fromLocation " +
            ", pd.fromLatitudeDegrees " +
            ", pd.fromLatitudeMinutes " +
            ", pd.fromLatitudeSeconds " +
            ", pd.fromLatitudeDirection " +
            ", pd.fromLongitudeDegrees " +
            ", pd.fromLongitudeMinutes " +
            ", pd.fromLongitudeSeconds " +
            ", pd.fromLongitudeDirection " +
            // To info.
            ", pd.toLocation " +
            ", pd.toLatitudeDegrees " +
            ", pd.toLatitudeMinutes " +
            ", pd.toLatitudeSeconds " +
            ", pd.toLatitudeDirection " +
            ", pd.toLongitudeDegrees " +
            ", pd.toLongitudeMinutes " +
            ", pd.toLongitudeSeconds " +
            ", pd.toLongitudeDirection " +
            ", pd.maxExternalDiameter " +
            ", pd.pipelineInBundle " +
            ", pd.bundleName " +
            ", pd.pipelineFlexibility " +
            ", pd.pipelineMaterial " +
            ", pd.otherPipelineMaterialUsed " +
            ", pd.trenchedBuriedFilledFlag " +
            ", pd.trenchingMethodsDesc" +
            ", pd.pipelineStatus" +
            ", pd.pipelineStatusReason ",
        PipelineDetailSummaryDto.class)
        .setParameter("master_pwa", masterPwa)
        .getResultList()
        .stream()
        .map(PadPipelineOverview::from)
        .collect(Collectors.toUnmodifiableList());
  }
}
