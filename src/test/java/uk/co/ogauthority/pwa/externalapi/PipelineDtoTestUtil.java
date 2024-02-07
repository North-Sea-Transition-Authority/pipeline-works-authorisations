package uk.co.ogauthority.pwa.externalapi;

class PipelineDtoTestUtil {

  private PipelineDtoTestUtil() {}

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer id;
    private String number;
    private String pwaReference;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withNumber(String number) {
      this.number = number;
      return this;
    }

    public Builder withPwaReference(String pwaReference) {
      this.pwaReference = pwaReference;
      return this;
    }

    public PipelineDto build() {
      return new PipelineDto(
          id,
          number,
          pwaReference
      );
    }
  }
}