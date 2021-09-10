package uk.co.ogauthority.pwa.util;

import java.util.Optional;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;

public class MailMergeUtils {

  private MailMergeUtils() {
    throw new AssertionError();
  }

  public static boolean textContainsManualMergeDelimiters(String text) {
    return Optional.ofNullable(text)
        .map(t -> t.contains(MailMergeFieldType.MANUAL.getOpeningDelimiter())
            || t.contains(MailMergeFieldType.MANUAL.getClosingDelimiter()))
        .orElse(false);
  }

}
