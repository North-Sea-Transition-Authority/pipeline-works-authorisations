package uk.co.ogauthority.pwa.externalapi;

class PwaDtoTestUtil {

  private PwaDtoTestUtil() {}

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Integer id;
    private String reference;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public PwaDto build() {
      return new PwaDto(
          id,
          reference
      );
    }
  }
}
