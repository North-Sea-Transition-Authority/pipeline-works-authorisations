package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


@Repository
public interface PadDepositDrawingLinkRepository extends CrudRepository<PadDepositDrawingLink, Integer> {

  @EntityGraph(attributePaths = { "padDepositDrawing" })
  List<PadDepositDrawingLink> getAllByPadDepositDrawingIn(List<PadDepositDrawing> drawings);

  List<PadDepositDrawingLink> getAllByPadDepositDrawing(PadDepositDrawing drawing);

  List<PadDepositDrawingLink> getAllByPadPermanentDeposit(PadPermanentDeposit padPermanentDeposits);

  List<PadDepositDrawingLink> getAllByPadPermanentDepositIn(Collection<PadPermanentDeposit> deposits);

  List<PadDepositDrawingLink> findByPadPermanentDeposit_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
