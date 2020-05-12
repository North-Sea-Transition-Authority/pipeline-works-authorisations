package uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;

public enum CrossingAgreementTask {

  LICENCE_AND_BLOCK_NUMBERS("Licence and block numbers", BlockCrossingService.class, 10),
  CROSSING_TYPES("Types of crossings", CrossingTypesService.class, 20),
  CABLE_CROSSINGS("Cable crossings", PadCableCrossingService.class, 40),
  MEDIAN_LINE("Median line crossings", PadMedianLineAgreementService.class, 50);

  private String displayText;
  private Class<? extends TaskListSection> section;
  private int displayOrder;

  CrossingAgreementTask(String displayText, Class<? extends TaskListSection> section, int displayOrder) {
    this.displayText = displayText;
    this.section = section;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Class<? extends TaskListSection> getSection() {
    return section;
  }

  public static Stream<CrossingAgreementTask> stream() {
    return Arrays.stream(CrossingAgreementTask.values());
  }
}
