package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;

@Service
public class PadEnvironmentalDecommissioningService {

  private final PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  @Autowired
  public PadEnvironmentalDecommissioningService(
      PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository) {
    this.padEnvironmentalDecommissioningRepository = padEnvironmentalDecommissioningRepository;
  }

  public PadEnvironmentalDecommissioning getEnvDecomData(PwaApplicationDetail pwaApplicationDetail) {
    var adminDetail = padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadEnvironmentalDecommissioning());
    adminDetail.setPwaApplicationDetail(pwaApplicationDetail);
    return adminDetail;
  }

  public PadEnvironmentalDecommissioning save(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    return padEnvironmentalDecommissioningRepository.save(padEnvironmentalDecommissioning);
  }

}
