package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadData;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadDataRepository;

@Service
public class PadDataService {

  private final PadDataRepository padDataRepository;

  @Autowired
  public PadDataService(
      PadDataRepository padDataRepository) {
    this.padDataRepository = padDataRepository;
  }

  public PadData getPadData(PwaApplicationDetail pwaApplicationDetail) {
    var adminDetail = padDataRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadData());
    adminDetail.setPwaApplicationDetail(pwaApplicationDetail);
    return adminDetail;
  }

  public PadData save(PadData padData) {
    return padDataRepository.save(padData);
  }

}
