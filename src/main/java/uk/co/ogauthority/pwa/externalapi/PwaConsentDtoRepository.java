package uk.co.ogauthority.pwa.externalapi;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Repository
interface PwaConsentDtoRepository extends CrudRepository<PwaConsent, Integer> {

  @Query("SELECT new uk.co.ogauthority.pwa.externalapi.PwaConsentDto" +
      "(pc.id, pc.reference, pc.consentType, pc.createdInstant, pc.consentInstant, mpd.masterPwa.id," +
      " mpd.reference, mpd.masterPwaDetailStatus)" +
      "FROM MasterPwaDetail mpd " +
      "JOIN PwaConsent pc ON pc.masterPwa.id = mpd.masterPwa.id " +
      "WHERE mpd.endInstant IS NULL " +
      "AND mpd.masterPwa.id IN (:ids) "
  )
  List<PwaConsentDto> searchPwaConsents(List<Integer> ids);
}
