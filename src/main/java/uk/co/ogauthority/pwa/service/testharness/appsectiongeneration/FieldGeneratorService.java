package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class FieldGeneratorService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadFieldRepository padFieldRepository;
  private final DevukFieldService devukFieldService;

  @Autowired
  public FieldGeneratorService(
      PwaApplicationDetailService pwaApplicationDetailService,
      PadFieldRepository padFieldRepository, DevukFieldService devukFieldService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padFieldRepository = padFieldRepository;
    this.devukFieldService = devukFieldService;
  }


  public void generatePadFields(PwaApplicationDetail pwaApplicationDetail) {

    pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, true);
    var padField = new PadField();
    padField.setPwaApplicationDetail(pwaApplicationDetail);
    var devukFieldId = 2692;
    padField.setDevukField(devukFieldService.findById(devukFieldId));
    padFieldRepository.save(padField);
  }





}
