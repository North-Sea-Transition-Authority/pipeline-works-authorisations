package uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;


@Repository
public interface PadDepositDrawingRepository extends CrudRepository<PadDepositDrawing, Integer> {

  List<PadDepositDrawing> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadDepositDrawing> findByPwaApplicationDetailAndReferenceIgnoreCase(
      PwaApplicationDetail pwaApplicationDetail, String reference);

  Optional<PadDepositDrawing> findByPwaApplicationDetailAndAndFile(
      PwaApplicationDetail pwaApplicationDetail, PadFile file);
}
