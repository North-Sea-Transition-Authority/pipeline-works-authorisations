package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInfoFile;

public interface PermanentDepositInfoFileRepository extends CrudRepository<PermanentDepositInfoFile, Integer> {

  List<PermanentDepositInfoFile> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PermanentDepositInfoFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail, String fileId);

}
