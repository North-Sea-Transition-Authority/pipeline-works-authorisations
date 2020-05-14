package uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;

public enum CrossingAgreementTask {

  LICENCE_AND_BLOCK_NUMBERS("Licence and block numbers", BlockCrossingService.class, 10),
  CROSSING_TYPES("Types of crossing", CrossingTypesService.class, 20),
  PIPELINE_CROSSINGS("Pipeline crossings", PadPipelineCrossingService.class, 30),
  CABLE_CROSSINGS("Cable crossings", PadCableCrossingService.class, 40),
  MEDIAN_LINE("Median line crossing", PadMedianLineAgreementService.class, 50);

  private final String displayText;
  private final Class<? extends ApplicationFormSectionService> sectionClass;
  private final int displayOrder;

  CrossingAgreementTask(String displayText, Class<? extends ApplicationFormSectionService> sectionClass, int displayOrder) {
    this.displayText = displayText;
    this.sectionClass = sectionClass;
    this.displayOrder = displayOrder;
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
