package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailSummaryDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;

@Repository
public class PipelineDetailDtoRepositoryImpl implements PipelineDetailDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PipelineDetailDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // TODO PWA-1047 rename and accept Master PWa not application detail
  @Override
  public List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    var statusFilter = PipelineStatus.currentStatusSet();

    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto(" +
        "pd.pipeline.id, " +
        "pd.bundleName " +
        ") " +
        "FROM PipelineDetail pd " +
        "JOIN PwaConsent pc ON pd.pwaConsent = pc " +
        "WHERE pd.tipFlag = TRUE " +
        "AND pc.masterPwa = :master_pwa " +
        "AND pd.bundleName IS NOT NULL " +
        "AND pd.pipelineStatus IN :statusFilter ", PipelineBundlePairDto.class)
        .setParameter("master_pwa", pwaApplicationDetail.getMasterPwa())
        .setParameter("statusFilter", statusFilter)
        .getResultList();
  }

  @Override
  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwaAndStatus(MasterPwa masterPwa,
                                                                             Set<PipelineStatus> statusFilter) {

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
        // Filter left out of query itself to make extracting unfiltered query out later on easier
        // eventually we need to be able to have this query run over non tip details and with no status restriction
        // so makes sense to avoid building extra restrictions into the query string itself for now.
        .filter(pipelineOverview -> statusFilter.contains(pipelineOverview.getPipelineStatus()))
        .collect(Collectors.toUnmodifiableList());
  }


  @Override
  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwa(MasterPwa masterPwa) {
    var statusFilter = PipelineStatus.currentStatusSet();
    return getAllPipelineOverviewsForMasterPwaAndStatus(masterPwa, statusFilter);
  }

  @Override
  public List<CountPipelineDetailsForPipelineDto> getCountOfPipelineDetailsForPipelines(Set<PipelineId> pipelineIds) {

    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.repository.pipelines.CountPipelineDetailsForPipelineDto( " +
            " p.id," +
            " COUNT(pd.id) " +
            ")" +
            "FROM Pipeline p " +
            "LEFT JOIN PipelineDetail pd ON pd.pipeline = p " +
            "WHERE p.id IN :pipelineIdSet " +
            "GROUP BY p.id",
        CountPipelineDetailsForPipelineDto.class
    )
        .setParameter("pipelineIdSet", pipelineIds.stream().map(PipelineId::getPipelineIdAsInt).collect(Collectors.toSet()))
        .getResultList();

  }
}
