package uk.co.ogauthority.pwa.temp.components;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.view.PipelineView;

@Component
@Scope("session")
public class PipelineGodObject implements Serializable {

  List<PipelineView> pipelineViewList;

  public PipelineGodObject() {
    this.pipelineViewList = List.of();
  }

  public List<PipelineView> getPipelineViewList() {
    return pipelineViewList;
  }

  public void setPipelineViewList(List<PipelineView> pipelineViewList) {
    this.pipelineViewList = pipelineViewList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineGodObject that = (PipelineGodObject) o;
    return Objects.equals(pipelineViewList, that.pipelineViewList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineViewList);
  }
}
