package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoRepository;

@Service
@Profile("development")
public class GeneralTechDetailsGeneratorService {

  private final PadPipelineTechInfoRepository padPipelineTechInfoRepository;


  @Autowired
  public GeneralTechDetailsGeneratorService(
      PadPipelineTechInfoRepository padPipelineTechInfoRepository) {
    this.padPipelineTechInfoRepository = padPipelineTechInfoRepository;
  }


  public void generateGeneralTechDetails(PwaApplicationDetail pwaApplicationDetail) {

    var generalTechDetails = new PadPipelineTechInfo();
    setGeneralTechDetailsData(pwaApplicationDetail, generalTechDetails);
    padPipelineTechInfoRepository.save(generalTechDetails);
  }


  private void setGeneralTechDetailsData(PwaApplicationDetail pwaApplicationDetail, PadPipelineTechInfo generalTechDetails) {

    generalTechDetails.setPwaApplicationDetail(pwaApplicationDetail);
    generalTechDetails.setEstimatedFieldLife(50);
    generalTechDetails.setPipelineDesignedToStandards(false);
    generalTechDetails.setCorrosionDescription("My description of the corrosion management strategy");
    generalTechDetails.setPlannedPipelineTieInPoints(false);
  }

}
