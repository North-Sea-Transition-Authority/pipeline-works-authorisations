package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public interface WorkAreaApplicationDetailSearchItemRepository extends CrudRepository<WorkAreaApplicationDetailSearchItem, Integer> {

  String PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH = "" +
      "FROM WorkAreaApplicationDetailSearchItem waadsi " +
      "WHERE ( waadsi.pwaApplicationId IN :applicationIdFilter OR (waadsi.padStatus IN :statusFilter AND waadsi.tipFlag = 1)) " +
      "AND ( " +
      "  (waadsi.tipVersionSatisfactoryFlag = :tipVersionSatisfactoryFlag) OR ( " +
      "    waadsi.openUpdateRequestFlag = :openForUpdateFlag " +
      "    AND waadsi.publicNoticeStatus IN :publicNoticeStatuses " +
      "    AND waadsi.openConsultationRequestFlag = :openConsultationRequestFlag" +
      "  ) " +
      ")";

  String PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH = "" +
      "FROM WorkAreaApplicationDetailSearchItem waadsi " +
      "WHERE (waadsi.pwaApplicationId IN :applicationIdFilter OR (waadsi.padStatus IN :statusFilter AND waadsi.tipFlag = 1)) " +
      "AND ( " +
      "  (waadsi.tipVersionSatisfactoryFlag = :tipVersionSatisfactoryFlag) AND ( " +
      "    waadsi.openUpdateRequestFlag = :openForUpdateFlag " +
      "    OR waadsi.publicNoticeStatus IN :publicNoticeStatuses " +
      "    OR waadsi.openConsultationRequestFlag = :openConsultationRequestFlag" +
      "  ) " +
      ")";

  Page<WorkAreaApplicationDetailSearchItem> findAllByTipFlagIsTrueAndPadStatusIn(Pageable pageable,
                                                                                 Collection<PwaApplicationStatus> statusFilter);

  Page<WorkAreaApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdIn(Pageable pageable,
                                                                                     Collection<PwaApplicationStatus> statusFilter,
                                                                                     Collection<Integer> applicationIdFilter);


  // we can use standard JPQL + Pageable here as the sort values will never be null after submission.
  @Query(value = PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH,
      countQuery = "SELECT COUNT(waadsi) " +
          PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH
  )
  Page<WorkAreaApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsOrAllWaitFlagsMatch(
      Pageable pageable,
      @Param("statusFilter") Collection<PwaApplicationStatus> statusFilter,
      @Param("applicationIdFilter") Collection<Integer> applicationIdFilter,
      @Param("tipVersionSatisfactoryFlag") Boolean tipVersionSatisfactoryFlag,
      @Param("openForUpdateFlag") Boolean openForUpdateFlag,
      @Param("publicNoticeStatuses") Collection<PublicNoticeStatus> publicNoticeStatuses,
      @Param("openConsultationRequestFlag") Boolean openConsultationRequestFlag
  );

  // we can use standard JPQL + Pageable here as the sort values will never be null after submission.
  @Query(value = PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH,
      countQuery = "SELECT COUNT(waadsi) " +
          PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH
  )
  Page<WorkAreaApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsAndAnyWaitFlagsMatch(
      Pageable pageable,
      @Param("statusFilter") Collection<PwaApplicationStatus> statusFilter,
      @Param("applicationIdFilter") Collection<Integer> applicationIdFilter,
      @Param("tipVersionSatisfactoryFlag") Boolean tipVersionSatisfactoryFlag,
      @Param("openForUpdateFlag") Boolean openForUpdateFlag,
      @Param("publicNoticeStatuses") Collection<PublicNoticeStatus> publicNoticeStatuses,
      @Param("openConsultationRequestFlag") Boolean openConsultationRequestFlag
  );

  Page<WorkAreaApplicationDetailSearchItem> findAllByPwaApplicationIdInAndPadStatusInAndOpenUpdateRequestFlag(
      Pageable pageable,
      Collection<Integer> applicationIdFilter,
      Collection<PwaApplicationStatus> statusFilter,
      Boolean openUpdateRequestFlag);

  Optional<WorkAreaApplicationDetailSearchItem> findByPwaApplicationDetailIdEquals(Integer pwaApplicationDetailId);

}
