package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.springframework.data.annotation.CreatedDate;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_pipeline_transfers")
public class PadPipelineTransfer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "PAD_PIPELINE_ID")
  @OneToOne
  private PadPipeline padPipeline;

  @JoinColumn(name = "DONOR_PAD_ID")
  @OneToOne
  private PwaApplicationDetail donorApplication;

  @JoinColumn(name = "RECIPIENT_PAD_ID")
  @OneToOne
  private PwaApplicationDetail recipientApplication;

  @CreatedDate
  private Instant createdTimestamp;

  public PadPipelineTransfer() {
    createdTimestamp = Instant.now();
  }

  @VisibleForTesting
  public PadPipelineTransfer(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public PadPipelineTransfer setId(Integer id) {
    this.id = id;
    return this;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public PadPipelineTransfer setPadPipeline(
      PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
    return this;
  }

  public PwaApplicationDetail getDonorApplication() {
    return donorApplication;
  }

  public PadPipelineTransfer setDonorApplication(
      PwaApplicationDetail donorApplication) {
    this.donorApplication = donorApplication;
    return this;
  }

  public PwaApplicationDetail getRecipientApplication() {
    return recipientApplication;
  }

  public PadPipelineTransfer setRecipientApplication(
      PwaApplicationDetail recipientApplication) {
    this.recipientApplication = recipientApplication;
    return this;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public PadPipelineTransfer setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
    return this;
  }
}
