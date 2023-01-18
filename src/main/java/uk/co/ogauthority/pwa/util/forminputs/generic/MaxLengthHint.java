package uk.co.ogauthority.pwa.util.forminputs.generic;

public class MaxLengthHint {

  private final int maxInputLength;

  public MaxLengthHint(int maxInputLength) {
    this.maxInputLength = maxInputLength;
  }

  public int getMaxInputLength() {
    return maxInputLength;
  }
}
