package uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


@Repository
public interface PadDepositDrawingLinkRepository extends CrudRepository<PadDepositDrawingLink, Integer> {

  List<PadDepositDrawingLink> getAllByPadDepositDrawingIn(List<PadDepositDrawing> drawings);
  List<PadDepositDrawingLink> getAllByPadDepositDrawing(PadDepositDrawing drawing);

}
