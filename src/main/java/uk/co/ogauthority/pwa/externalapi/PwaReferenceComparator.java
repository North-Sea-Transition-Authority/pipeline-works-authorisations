package uk.co.ogauthority.pwa.externalapi;

import java.util.Comparator;
import java.util.function.Function;

public class PwaReferenceComparator implements Comparator<PwaDto> {

  @Override
  public int compare(PwaDto firstPwa, PwaDto secondPwa) {
    var firstPwaReference = firstPwa.getReference();
    var secondPwaReference = secondPwa.getReference();

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
}
