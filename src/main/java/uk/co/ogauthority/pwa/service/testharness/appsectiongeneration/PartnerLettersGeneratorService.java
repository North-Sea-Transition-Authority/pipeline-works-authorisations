package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("development")
public class PartnerLettersGeneratorService {

  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final TestHarnessFileService testHarnessFileService;

  @Autowired
  public PartnerLettersGeneratorService(
      PwaApplicationDetailRepository pwaApplicationDetailRepository,
      TestHarnessFileService testHarnessFileService) {
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.testHarnessFileService = testHarnessFileService;
  }



  public void generatePartnerLetters(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    setPartnerLetterData(user, pwaApplicationDetail);
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }


  private void setPartnerLetterData(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {
    pwaApplicationDetail.setPartnerLettersRequired(true);
    pwaApplicationDetail.setPartnerLettersConfirmed(true);
    testHarnessFileService.generateInitialUpload(user, pwaApplicationDetail, ApplicationDetailFilePurpose.PARTNER_LETTERS);
  }




}
