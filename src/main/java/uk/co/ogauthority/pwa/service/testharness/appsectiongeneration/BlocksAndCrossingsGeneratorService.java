package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.CrossedBlockOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesForm;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
@Profile("test-harness")
class BlocksAndCrossingsGeneratorService implements TestHarnessAppFormService {

  private final BlockCrossingService blockCrossingService;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.CROSSING_AGREEMENTS;


  @Autowired
  public BlocksAndCrossingsGeneratorService(BlockCrossingService blockCrossingService,
                                            PwaApplicationDetailService pwaApplicationDetailService) {
    this.blockCrossingService = blockCrossingService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var blockCrossingForm = createBlockCrossingForm();
    blockCrossingService.createAndSaveBlockCrossingAndOwnersFromForm(appFormServiceParams.getApplicationDetail(), blockCrossingForm);

    var crossingTypesForm = createCrossingTypesForm();
    pwaApplicationDetailService.updateCrossingTypes(appFormServiceParams.getApplicationDetail(), crossingTypesForm);
  }


  private AddBlockCrossingForm createBlockCrossingForm() {

    var form = new AddBlockCrossingForm();
    var blockRef = "10/1a101a300";
    form.setPickedBlock(blockRef);
    form.setCrossedBlockOwner(CrossedBlockOwner.HOLDER);
    return form;
  }

  private CrossingTypesForm createCrossingTypesForm() {
    var form = new CrossingTypesForm();
    form.setCablesCrossed(false);
    form.setPipelinesCrossed(false);
    form.setMedianLineCrossed(false);
    return form;
  }
}
