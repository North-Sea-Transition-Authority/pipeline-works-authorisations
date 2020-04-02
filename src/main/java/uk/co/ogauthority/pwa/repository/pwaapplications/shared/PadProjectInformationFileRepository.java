package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;

public interface PadProjectInformationFileRepository extends CrudRepository<PadProjectInformationFile, Integer> {

  List<PadProjectInformationFile> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadProjectInformationFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail, String fileId);

}
