package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadAdmiralityChartFile;

@Repository
public interface PadAdmiralityChartFileRepository extends CrudRepository<PadAdmiralityChartFile, Integer> {

  List<PadAdmiralityChartFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadAdmiralityChartFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                       String fileId);

  int countPadCrossedBlockByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
