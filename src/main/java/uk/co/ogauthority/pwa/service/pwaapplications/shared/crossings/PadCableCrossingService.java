package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingRepository;

@Service
public class PadCableCrossingService {

  private final PadCableCrossingRepository padCableCrossingRepository;

  @Autowired
  public PadCableCrossingService(
      PadCableCrossingRepository padCableCrossingRepository) {
    this.padCableCrossingRepository = padCableCrossingRepository;
  }

  public PadCableCrossing getCableCrossingForPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    var cableCrossingIfOptionEmpty = new PadCableCrossing();
    cableCrossingIfOptionEmpty.setPwaApplicationDetail(pwaApplicationDetail);
    return padCableCrossingRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(cableCrossingIfOptionEmpty);
  }

}
