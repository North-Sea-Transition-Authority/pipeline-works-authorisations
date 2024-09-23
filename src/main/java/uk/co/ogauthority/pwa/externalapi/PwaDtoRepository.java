package uk.co.ogauthority.pwa.externalapi;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@Repository
interface PwaDtoRepository extends CrudRepository<MasterPwaDetail, Integer> {

  @Query("SELECT new uk.co.ogauthority.pwa.externalapi.PwaDto(mpd.masterPwa.id, mpd.reference, mpd.masterPwaDetailStatus) " +
      "FROM MasterPwaDetail mpd " +
      "WHERE mpd.endInstant IS NULL " +
      "AND (mpd.masterPwa.id in (:ids) or COALESCE(:ids, null) is null) " +
      "AND (LOWER(mpd.reference) like LOWER('%'||:reference||'%') or :reference is null) " +
      "AND (mpd.masterPwaDetailStatus = :status or :status is null) "
  )
  List<PwaDto> searchPwas(List<Integer> ids, String reference, MasterPwaDetailStatus status);
}
