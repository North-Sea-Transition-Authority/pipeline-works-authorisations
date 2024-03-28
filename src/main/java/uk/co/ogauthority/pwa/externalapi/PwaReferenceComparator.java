package uk.co.ogauthority.pwa.externalapi;

import java.util.Comparator;
import java.util.function.Function;

public class PwaReferenceComparator implements Comparator<PwaDto> {

  @Override
  public int compare(PwaDto firstPwa, PwaDto secondPwa) {
    var firstPwaReference = firstPwa.getReference();
    var secondPwaReference = secondPwa.getReference();

    if (firstPwaReference.split("/").length < secondPwaReference.split("/").length) {
      return 1;
    }

    if (firstPwaReference.split("/").length > secondPwaReference.split("/").length) {
      return -1;
    }

    if (firstPwaReference.split("/").length == 2 && secondPwaReference.split("/").length == 2) {
      return Comparator.comparing(alphabeticallyForLegacyReferences())
          .thenComparing(numericallyForLegacyReferences())
          .compare(firstPwaReference, secondPwaReference);
    }

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
