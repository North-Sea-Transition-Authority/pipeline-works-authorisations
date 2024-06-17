package uk.co.ogauthority.pwa.externalapi;

import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

class PwaDtoTestUtil {

  private PwaDtoTestUtil() {}

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Integer id;
    private String reference;
    private MasterPwaDetailStatus status;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder withStatus(MasterPwaDetailStatus status) {
      this.status = status;
      return this;
    }

    public PwaDto build() {
      return new PwaDto(
          id,
          reference,
          status
      );
    }
  }
}
