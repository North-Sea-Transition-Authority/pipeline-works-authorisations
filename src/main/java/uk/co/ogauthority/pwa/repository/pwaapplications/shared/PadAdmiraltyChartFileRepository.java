package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadAdmiraltyChartFile;

@Repository
public interface PadAdmiraltyChartFileRepository extends CrudRepository<PadAdmiraltyChartFile, Integer> {

  List<PadAdmiraltyChartFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadAdmiraltyChartFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                      String fileId);

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
