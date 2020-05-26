package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class WorkScheduleForm {
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkScheduleForm.class);

  private TwoFieldDateInput workStart;

  private TwoFieldDateInput workEnd;

  private List<Integer> padPipelineIds = new ArrayList<>();

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
        "workStart=" + workStart.toString() +
        ", workEnd=" + workEnd.toString() +
        ", padPipelineIds_size=" + (padPipelineIds != null ? String.valueOf(padPipelineIds.size()) : "IS_NULL") +
        '}';
  }
}
