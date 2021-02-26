package uk.co.ogauthority.pwa.service.enums.workarea;

import java.util.Arrays;
import java.util.stream.Stream;

public enum WorkAreaFlag {

  TIP_VERSION_SATISFACTORY,

  OPEN_UPDATE_REQUEST,

  PUBLIC_NOTICE_OVERRIDE,

  OPEN_CONSULTATION_REQUEST,

  OPEN_CONSENT_REVIEW_FOREGROUND_FLAG;

  public static Stream<WorkAreaFlag> stream() {
    return Arrays.stream(WorkAreaFlag.values());
  }

}
