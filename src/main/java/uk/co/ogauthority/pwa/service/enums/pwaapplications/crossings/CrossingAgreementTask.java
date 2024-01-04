package uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller.CarbonStorageAreaCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller.MedianLineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller.PipelineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.controller.CrossingTypesController;

public enum CrossingAgreementTask {

  LICENCE_AND_BLOCKS("Licence and blocks", BlockCrossingService.class, BlockCrossingController.class, 10),

  CARBON_STORAGE_AREAS("Carbon storage areas", CarbonStorageAreaCrossingService.class, CarbonStorageAreaCrossingController.class, 15),
  CROSSING_TYPES("Types of crossing", CrossingTypesService.class, CrossingTypesController.class, 20),
  PIPELINE_CROSSINGS("Pipeline crossings", PadPipelineCrossingService.class, PipelineCrossingController.class, 30),
  CABLE_CROSSINGS("Cable crossings", PadCableCrossingService.class, CableCrossingController.class, 40),
  MEDIAN_LINE("Median line crossing", PadMedianLineAgreementService.class, MedianLineCrossingController.class, 50);

  private final String displayText;
  private final Class<? extends ApplicationFormSectionService> sectionClass;
  private final Class<?> controllerClass;
  private final int displayOrder;

  CrossingAgreementTask(String displayText,
                        Class<? extends ApplicationFormSectionService> sectionClass,
                        Class<?> controllerClass, int displayOrder) {
    this.displayText = displayText;
    this.sectionClass = sectionClass;
    this.controllerClass = controllerClass;
    this.displayOrder = displayOrder;
  }

  public Class<?> getControllerClass() {
    return controllerClass;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Class<? extends ApplicationFormSectionService> getSectionClass() {
    return sectionClass;
  }

  public static Stream<CrossingAgreementTask> stream() {
    return Arrays.stream(CrossingAgreementTask.values());
  }
}
