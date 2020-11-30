package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public interface ApplicationDetailSearchItemRepository extends CrudRepository<ApplicationDetailSearchItem, Integer> {

  String PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH = "" +
      "FROM ApplicationDetailSearchItem adsi " +
      "WHERE ( adsi.pwaApplicationId IN :applicationIdFilter OR adsi.padStatus IN :statusFilter) " +
      "AND ( " +
      "  (adsi.tipVersionSatisfactoryFlag = :tipVersionSatisfactoryFlag) OR ( " +
      "    adsi.openUpdateRequestFlag = :openForUpdateFlag " +
      "    AND adsi.openPublicNoticeFlag = :openPublicNoticeFlag " +
      "    AND adsi.openConsultationRequestFlag = :openConsultationRequestFlag" +
      "  ) " +
      ")";

  String PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH = "" +
      "FROM ApplicationDetailSearchItem adsi " +
      "WHERE (adsi.pwaApplicationId IN :applicationIdFilter OR adsi.padStatus IN :statusFilter) " +
      "AND ( " +
      "  (adsi.tipVersionSatisfactoryFlag = :tipVersionSatisfactoryFlag) AND ( " +
      "    adsi.openUpdateRequestFlag = :openForUpdateFlag " +
      "    OR adsi.openPublicNoticeFlag = :openPublicNoticeFlag " +
      "    OR adsi.openConsultationRequestFlag = :openConsultationRequestFlag" +
      "  ) " +
      ")";

  Page<ApplicationDetailSearchItem> findAllByTipFlagIsTrueAndPadStatusIn(Pageable pageable,
                                                                         Collection<PwaApplicationStatus> statusFilter);

  Page<ApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdIn(Pageable pageable,
                                                                             Collection<PwaApplicationStatus> statusFilter,
                                                                             Collection<Integer> applicationIdFilter);


  // we can use standard JPQL + Pageable here as the sort values will never be null after submission.
  @Query(value = PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH,
      countQuery = "SELECT COUNT(adsi) " +
          PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_OR_ALL_OTHER_WAIT_FLAGS_MATCH
  )
  Page<ApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsOrAllWaitFlagsMatch(
      Pageable pageable,
      @Param("statusFilter") Collection<PwaApplicationStatus> statusFilter,
      @Param("applicationIdFilter") Collection<Integer> applicationIdFilter,
      @Param("tipVersionSatisfactoryFlag") Boolean tipVersionSatisfactoryFlag,
      @Param("openForUpdateFlag") Boolean openForUpdateFlag,
      @Param("openPublicNoticeFlag") Boolean openPublicNoticeFlag,
      @Param("openConsultationRequestFlag") Boolean openConsultationRequestFlag
  );

  // we can use standard JPQL + Pageable here as the sort values will never be null after submission.
  @Query(value = PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH,
      countQuery = "SELECT COUNT(adsi) " +
          PADSTATUS_IN_OR_PWAAPPLICATION_ID_IN_AND_WHERE_TIP_SATISFACTORY_FLAG_MATCHES_AND_ANY_OTHER_WAIT_FLAGS_MATCH
  )
  Page<ApplicationDetailSearchItem> findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsAndAnyWaitFlagsMatch(
      Pageable pageable,
      @Param("statusFilter") Collection<PwaApplicationStatus> statusFilter,
      @Param("applicationIdFilter") Collection<Integer> applicationIdFilter,
      @Param("tipVersionSatisfactoryFlag") Boolean tipVersionSatisfactoryFlag,
      @Param("openForUpdateFlag") Boolean openForUpdateFlag,
      @Param("openPublicNoticeFlag") Boolean openPublicNoticeFlag,
      @Param("openConsultationRequestFlag") Boolean openConsultationRequestFlag
  );

  Page<ApplicationDetailSearchItem> findAllByPwaApplicationIdInAndPadStatusInAndOpenUpdateRequestFlag(
      Pageable pageable,
      Collection<Integer> applicationIdFilter,
      Collection<PwaApplicationStatus> statusFilter,
      Boolean openUpdateRequestFlag);

  Optional<ApplicationDetailSearchItem> findByPwaApplicationDetailIdEquals(Integer pwaApplicationDetailId);

}
