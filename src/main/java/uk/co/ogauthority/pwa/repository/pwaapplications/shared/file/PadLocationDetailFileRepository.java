package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.location.PadLocationDetailFile;

@Repository
public interface PadLocationDetailFileRepository extends CrudRepository<PadLocationDetailFile, Integer> {

  List<PadLocationDetailFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadLocationDetailFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail detail, String fileId);

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
