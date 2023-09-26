package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.springframework.data.annotation.CreatedDate;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_pipeline_transfers")
public class PadPipelineTransfer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "donor_pipeline_id")
  @OneToOne
  private Pipeline donorPipeline;

  @JoinColumn(name = "recipient_pipeline_id")
  @OneToOne
  private Pipeline recipientPipeline;

  @JoinColumn(name = "donor_pad_id")
  @OneToOne
  private PwaApplicationDetail donorApplicationDetail;

  @JoinColumn(name = "recipient_pad_id")
  @OneToOne
  private PwaApplicationDetail recipientApplicationDetail;

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

  public Pipeline getDonorPipeline() {
    return donorPipeline;
  }

  public PadPipelineTransfer setDonorPipeline(Pipeline donorPipeline) {
    this.donorPipeline = donorPipeline;
    return this;
  }

  public PwaApplicationDetail getDonorApplicationDetail() {
    return donorApplicationDetail;
  }

  public PadPipelineTransfer setDonorApplicationDetail(PwaApplicationDetail donorApplicationDetail) {
    this.donorApplicationDetail = donorApplicationDetail;
    return this;
  }

  public PwaApplicationDetail getRecipientApplicationDetail() {
    return recipientApplicationDetail;
  }

  public PadPipelineTransfer setRecipientApplicationDetail(PwaApplicationDetail recipientApplicationDetail) {
    this.recipientApplicationDetail = recipientApplicationDetail;
    return this;
  }

  public Pipeline getRecipientPipeline() {
    return recipientPipeline;
  }

  public PadPipelineTransfer setRecipientPipeline(
      Pipeline recipientPipeline) {
    this.recipientPipeline = recipientPipeline;
    return this;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public PadPipelineTransfer setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
    return this;
  }

  public TransferParticipantType getTransferParticipantType(Pipeline pipeline) {
    if (Objects.equals(this.donorPipeline, pipeline)) {
      return TransferParticipantType.DONOR;
    } else if (Objects.equals(this.recipientPipeline, pipeline)) {
      return TransferParticipantType.RECIPIENT;
    } else {
      throw new RuntimeException(String.format(
          "Pipeline with ID: %s not associated with PadPipelineTransfer with ID: %s", pipeline.getId(), this.id));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineTransfer that = (PadPipelineTransfer) o;
    return Objects.equals(id, that.id) && Objects.equals(donorPipeline,
        that.donorPipeline) && Objects.equals(recipientPipeline,
        that.recipientPipeline) && Objects.equals(donorApplicationDetail,
        that.donorApplicationDetail) && Objects.equals(recipientApplicationDetail,
        that.recipientApplicationDetail) && Objects.equals(createdTimestamp, that.createdTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, donorPipeline, recipientPipeline, donorApplicationDetail, recipientApplicationDetail,
        createdTimestamp);
  }

}
