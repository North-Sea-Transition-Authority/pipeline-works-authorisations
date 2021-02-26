package uk.co.ogauthority.pwa.service.enums.workarea;

import java.util.Arrays;
import java.util.stream.Stream;

public enum WorkAreaFlag {

  TIP_VERSION_SATISFACTORY,

  OPEN_UPDATE_REQUEST,

  OPEN_PUBLIC_NOTICE,

  OPEN_CONSULTATION_REQUEST,

  OPEN_CONSENT_REVIEW_FOREGROUND_FLAG; // whether or not open consent reviews should show in the foreground tab

  public static Stream<WorkAreaFlag> stream() {
    return Arrays.stream(WorkAreaFlag.values());
  }

}
