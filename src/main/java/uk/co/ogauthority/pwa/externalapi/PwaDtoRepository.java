package uk.co.ogauthority.pwa.externalapi;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@Repository
interface PwaDtoRepository extends CrudRepository<MasterPwaDetail, Integer> {

  @Query("SELECT new uk.co.ogauthority.pwa.externalapi.PwaDto(mpd.masterPwa.id, mpd.reference) " +
      "FROM MasterPwaDetail mpd " +
      "WHERE mpd.endInstant IS NULL " +
      "AND (mpd.masterPwa.id in (:ids) or COALESCE(:ids, null) is null) " +
      "AND (LOWER(mpd.reference) like LOWER('%'||:reference||'%') or :reference is null) "
  )
  List<PwaDto> searchPwas(List<Integer> ids, String reference);
}
