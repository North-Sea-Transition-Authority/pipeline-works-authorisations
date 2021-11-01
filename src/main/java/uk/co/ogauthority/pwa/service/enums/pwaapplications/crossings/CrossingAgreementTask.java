package uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingTypesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.PipelineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingController;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;

public enum CrossingAgreementTask {

  LICENCE_AND_BLOCKS("Licence and blocks", BlockCrossingService.class, BlockCrossingController.class, 10),
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
