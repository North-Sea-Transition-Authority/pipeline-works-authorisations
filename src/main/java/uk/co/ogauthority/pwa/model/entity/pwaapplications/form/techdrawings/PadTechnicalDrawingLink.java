package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

@Entity
@Table(name = "pad_technical_drawing_links")
public class PadTechnicalDrawingLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pp_id")
  private PadPipeline pipeline;

  @ManyToOne
  @JoinColumn(name = "ptd_id")
  private PadTechnicalDrawing technicalDrawing;

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
