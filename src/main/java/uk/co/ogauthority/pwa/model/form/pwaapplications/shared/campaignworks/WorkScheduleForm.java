package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.util.List;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class WorkScheduleForm {

  private TwoFieldDateInput workStart;

  private TwoFieldDateInput workEnd;

  private List<Integer> padPipelineIds;

  public List<Integer> getPadPipelineIds() {
    return padPipelineIds;
  }

  public void setPadPipelineIds(List<Integer> padPipelineIds) {
    this.padPipelineIds = padPipelineIds;
  }

  public TwoFieldDateInput getWorkStart() {
    return workStart;
  }

  public void setWorkStart(TwoFieldDateInput workStart) {
    this.workStart = workStart;
  }

  public TwoFieldDateInput getWorkEnd() {
    return workEnd;
  }

  public void setWorkEnd(TwoFieldDateInput workEnd) {
    this.workEnd = workEnd;
  }

  @Override
  public String toString() {
    return "WorkScheduleForm{" +
        "workStart=" + workStart +
        ", workEnd=" + workEnd +
        ", padPipelineIds_size=" + (padPipelineIds != null ? String.valueOf(padPipelineIds.size()) : "IS_NULL") +
        '}';
  }
}
