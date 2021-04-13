package uk.co.ogauthority.pwa.model.form.pwa;

public class PwaViewPipelineParams {


  private Integer pipelineDetailId;

  public static PwaViewPipelineParams from(PwaPipelineHistoryForm form) {

    var params = new PwaViewPipelineParams();
    params.setPipelineDetailId(form.getPipelineDetailId());
    return params;
  }

  public Integer getPipelineDetailId() {
    return pipelineDetailId;
  }

  public void setPipelineDetailId(Integer pipelineDetailId) {
    this.pipelineDetailId = pipelineDetailId;
  }

}
