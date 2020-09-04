package uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class ApplicationUpdateRequestForm {

  @NotNull(message = "A reason for the update request must be provided")
  @Length(max = 4000, message = "The reason for update request must be less than {max} characters")
  private String requestReason;

  public String getRequestReason() {
    return requestReason;
  }

  public void setRequestReason(String requestReason) {
    this.requestReason = requestReason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationUpdateRequestForm that = (ApplicationUpdateRequestForm) o;
    return Objects.equals(requestReason, that.requestReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestReason);
  }
}
