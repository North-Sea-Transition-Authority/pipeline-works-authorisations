package uk.co.ogauthority.pwa.temp.components;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;
import uk.co.ogauthority.pwa.temp.model.view.PipelineView;
import uk.co.ogauthority.pwa.temp.model.view.TechnicalDetailsView;

@Component
@Scope("session")
public class PipelineGodObject implements Serializable {

  List<PipelineView> pipelineViewList;

  public PipelineGodObject() {
    PipelineView firstPipeline = new PipelineView("PL1", PipelineType.PRODUCTION_FLOWLINE, List.of());
    firstPipeline.setLength(99);
    firstPipeline.setFrom("Schiehallion FPSO");
    firstPipeline.setFromLatitudeDegrees("1");
    firstPipeline.setFromLatitudeMinutes("2");
    firstPipeline.setFromLatitudeSeconds("3");
    firstPipeline.setFromLongitudeDegrees("3");
    firstPipeline.setFromLongitudeMinutes("2");
    firstPipeline.setFromLongitudeSeconds("1");
    firstPipeline.setToLatitudeDegrees("5");
    firstPipeline.setToLatitudeMinutes("4");
    firstPipeline.setToLatitudeSeconds("3");
    firstPipeline.setToLongitudeDegrees("10");
    firstPipeline.setToLongitudeMinutes("89");
    firstPipeline.setToLongitudeSeconds("77");
    firstPipeline.setTo("Sullom Voe Terminal");
    firstPipeline.setProductsToBeConveyed("Oil");
    firstPipeline.setComponentParts("Sullom Voe Terminal");
    firstPipeline.setIdents(List.of());
    firstPipeline.setHolders(List.of("Royal Dutch Shell"));
    firstPipeline.setUsers(List.of("Conocophillips", "Taqa Brittani"));
    firstPipeline.setOperators(List.of("Wintershall BV"));
    firstPipeline.setOwners(List.of("GASSCO AS"));
    firstPipeline.setTechnicalDetailsView(TechnicalDetailsView.createExampleTechDetails());
    this.pipelineViewList = List.of(firstPipeline);
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
