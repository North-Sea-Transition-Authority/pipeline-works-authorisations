package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_technical_drawing_links")
public class PadTechnicalDrawingLink implements ChildEntity<Integer, PadTechnicalDrawing> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pp_id")
  private PadPipeline pipeline;

  @ManyToOne
  @JoinColumn(name = "ptd_id")
  private PadTechnicalDrawing technicalDrawing;

  public PadTechnicalDrawingLink() {
  }

  // ChildEntity Methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadTechnicalDrawing parentEntity) {
    this.technicalDrawing = parentEntity;
  }

  @Override
  public PadTechnicalDrawing getParent() {
    return this.technicalDrawing;
  }

  // generated methods
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(PadPipeline pipeline) {
    this.pipeline = pipeline;
  }

  public PadTechnicalDrawing getTechnicalDrawing() {
    return technicalDrawing;
  }

  public void setTechnicalDrawing(
      PadTechnicalDrawing technicalDrawing) {
    this.technicalDrawing = technicalDrawing;
  }
}
