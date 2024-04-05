package uk.co.ogauthority.pwa.externalapi;

import java.util.Comparator;
import java.util.function.Function;

public class PwaReferenceComparator implements Comparator<PwaDto> {

  /**
   * PWA references come in 4 formats - the majority are in the format 00/W/YY.
   * There are 3 legacy formats: PA/1, NONPWA/1, PWADATE/2000-01-01
   */
  @Override
  public int compare(PwaDto firstPwa, PwaDto secondPwa) {
    var firstPwaReference = firstPwa.getReference();
    var secondPwaReference = secondPwa.getReference();

    // if the first reference is one of the legacy references and the second is a new reference
    // then return 1, to indicate that the first reference comes after the second reference when sorted
    if (firstPwaReference.split("/").length < secondPwaReference.split("/").length) {
      return 1;
    }

    // if the first reference is a new reference and the second is one of the legacy references
    // then return -1, to indicate that the first reference comes after the second reference when sorted
    if (firstPwaReference.split("/").length > secondPwaReference.split("/").length) {
      return -1;
    }

    // if both references are in the legacy format, then alphabetically order the first part (ie PA, NONPWA or PWADATE)
    // and numerically order the digits that come after the /
    if (firstPwaReference.split("/").length == 2 && secondPwaReference.split("/").length == 2) {
      return Comparator.comparing(alphabeticallyForLegacyReferences())
          .thenComparing(numericallyForLegacyReferences())
          .compare(firstPwaReference, secondPwaReference);
    }

    // if both references are in the new format, then first sort by their consent date (YY), then sort by
    // their reference letter (ie W, V or D) and finally sort by their consent number (the first 2 digits before the /)
    return Comparator.comparing(byConsentDate())
        .thenComparing(byRefLetter())
        .thenComparing(byConsentNumber())
        .compare(firstPwaReference, secondPwaReference);
  }

  private Function<String, Integer> byConsentDate() {
    return pwaReference -> {
      try {
        return Integer.valueOf(pwaReference.split("/")[2]);
      } catch (Exception e) {
        return 999;
      }
    };
  }

  private Function<String, String> byRefLetter() {
    return pwaReference -> {
      try {
        return pwaReference.split("/")[1].toLowerCase();
      } catch (Exception e) {
        return "zzz";
      }
    };
  }

  private Function<String, Integer> byConsentNumber() {
    return pwaReference -> {
      try {
        return Integer.valueOf(pwaReference.split("/")[0]);
      } catch (Exception e) {
        return 999;
      }
    };
  }

  private Function<String, String> alphabeticallyForLegacyReferences() {
    return pwaReference -> {
      try {
        return pwaReference.split("/")[0].toLowerCase();
      } catch (Exception e) {
        return "zzz";
      }
    };
  }

  private Function<String, Integer> numericallyForLegacyReferences() {
    return pwaReference -> {
      try {
        var pwaReferenceDigits = pwaReference
            .replace("-", "")
            .split("/");
        return Integer.valueOf(pwaReferenceDigits[1]);
      } catch (Exception e) {
        return 999;
      }
    };
  }
}
