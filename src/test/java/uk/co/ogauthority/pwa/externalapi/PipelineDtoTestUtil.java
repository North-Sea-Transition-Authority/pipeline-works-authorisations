package uk.co.ogauthority.pwa.externalapi;

import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

public class PipelineDtoTestUtil {

  private PipelineDtoTestUtil() {}

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id;
    private String number;
    private Integer pwaId;
    private String pwaReference;
    private MasterPwaDetailStatus pwaStatus;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withNumber(String number) {
      this.number = number;
      return this;
    }

    public Builder withPwaId(Integer id) {
      this.pwaId = id;
      return this;
    }

    public Builder withPwaReference(String pwaReference) {
      this.pwaReference = pwaReference;
      return this;
    }

    public Builder withStatus(MasterPwaDetailStatus pwaStatus) {
      this.pwaStatus = pwaStatus;
      return this;
    }

    public PipelineDto build() {
      return new PipelineDto(
          id,
          number,
          pwaId,
          pwaReference,
          pwaStatus
      );
    }
  }
}