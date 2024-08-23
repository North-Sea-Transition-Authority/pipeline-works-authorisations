package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;
  private final PadPipelineService padPipelineService;
  private final PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;
  private final PipelineDetailService pipelineDetailService;

  private static final Map<PwaResourceType, List<PwaResourceType>> claimableResourceTypes =
      Map.of(
          PwaResourceType.PETROLEUM, List.of(PwaResourceType.PETROLEUM),
          PwaResourceType.HYDROGEN, List.of(PwaResourceType.HYDROGEN),
          PwaResourceType.CCUS, List.of(PwaResourceType.PETROLEUM, PwaResourceType.CCUS)
      );

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository,
                                    PadPipelineService padPipelineService,
                                    PadPipelineTransferClaimValidator padPipelineTransferClaimValidator,
                                    PipelineDetailService pipelineDetailService) {
    this.transferRepository = transferRepository;
    this.padPipelineService = padPipelineService;
    this.padPipelineTransferClaimValidator = padPipelineTransferClaimValidator;
    this.pipelineDetailService = pipelineDetailService;
  }

  @Transactional
  public void releasePipeline(PadPipeline transferredPipeline, PwaApplicationDetail applicationDetail) {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(transferredPipeline.getPipeline())
        .setDonorApplicationDetail(applicationDetail);
    transferRepository.save(transfer);
  }

  @Transactional
  public void claimPipeline(PadPipelineTransferClaimForm form, PwaApplicationDetail recipientApplicationDetail) {
    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(form.getPipelineId());
    var unclaimedTransfer = findUnclaimedByDonorPipeline(pipelineDetail.getPipeline());
    if (unclaimedTransfer.isPresent()) {
      var claimedPipeline = padPipelineService.createTransferredPipeline(form, recipientApplicationDetail);

      var transfer = unclaimedTransfer.get();
      transfer.setRecipientApplicationDetail(recipientApplicationDetail);
      transfer.setRecipientPipeline(claimedPipeline.getPipeline());
      transfer.setCompatibleWithTarget(form.isCompatibleWithTarget());
      if (form.getLastIntelligentlyPigged() != null) {
        transfer.setLastIntelligentlyPigged(DateUtils.datePickerStringToInstant(form.getLastIntelligentlyPigged()));
      }

      transferRepository.save(transfer);

    }
  }

  public Map<String, String> getClaimablePipelinesForForm(PwaResourceType resourceType) {
    var pipelineIds = StreamSupport.stream(transferRepository.findAll().spliterator(), false)
        .filter(padPipelineTransfer -> hasBeenClaimedByValidRecipient(padPipelineTransfer.getRecipientApplicationDetail()))
        .filter(padPipelineTransfer -> isClaimableResourceType(
            padPipelineTransfer.getDonorApplicationDetail().getResourceType(),
            resourceType))
        .map(padPipelineTransfer -> padPipelineTransfer.getDonorPipeline().getId())
        .collect(Collectors.toList());

    return pipelineDetailService.getLatestPipelineDetailsForIds(pipelineIds)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            pipelineDetail -> String.valueOf(pipelineDetail.getPipelineId().getPipelineIdAsInt()),
            PipelineDetail::getPipelineNumber)
        );
  }

  private boolean hasBeenClaimedByValidRecipient(PwaApplicationDetail recipientApplicationDetail) {
    if (Objects.isNull(recipientApplicationDetail)) {
      return true;
    }

    return PwaApplicationStatus.DELETED.equals(recipientApplicationDetail.getStatus());
  }

  public BindingResult validateClaimForm(PadPipelineTransferClaimForm form, BindingResult bindingResult, PwaResourceType resourceType) {
    padPipelineTransferClaimValidator.validate(form, bindingResult, resourceType.name());
    return bindingResult;
  }

  public List<PadPipelineTransfer> findUnclaimedByDonorApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByDonorApplicationDetail(applicationDetail)
        .stream()
        .filter(padPipelineTransfer -> hasBeenClaimedByValidRecipient(padPipelineTransfer.getRecipientApplicationDetail()))
        .collect(Collectors.toList());
  }

  public List<PadPipelineTransfer> getWithdrawnPipelineClaims() {
    return transferRepository.findAllByDonorApplicationDetailIsNullAndRecipientApplicationDetailIsNotNull();
  }

  private Optional<PadPipelineTransfer> findUnclaimedByDonorPipeline(Pipeline donorPipeline) {
    return transferRepository.findByDonorPipelineAndRecipientApplicationDetailIsNull(donorPipeline);
  }

  public List<PadPipelineTransfer> findByRecipientApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByRecipientApplicationDetail(applicationDetail);
  }

  public List<String> getUnclaimedPipelineNumbers(List<PadPipelineTransfer> unclaimedTransfers) {
    var unclaimedPipelines = unclaimedTransfers.stream()
        .map(PadPipelineTransfer::getDonorPipeline)
        .map(Pipeline::getId)
        .collect(Collectors.toList());

    return pipelineDetailService.getLatestPipelineDetailsForIds(unclaimedPipelines).stream()
        .map(PipelineDetail::getPipelineNumber)
        .collect(Collectors.toList());
  }

  @Transactional
  public void checkAndRemoveFromTransfer(Pipeline pipeline) {
    var recipientToRemove = transferRepository.findByRecipientPipeline(pipeline);

    recipientToRemove.ifPresentOrElse(
        this::removeRecipient,
        () -> {
          var donorToRemove = transferRepository.findByDonorPipeline(pipeline);

          donorToRemove.ifPresent(this::removeDonor);
        });
  }

  public Collection<PadPipelineTransfer> getTransfersByApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return transferRepository
        .findAllByDonorApplicationDetailEqualsOrRecipientApplicationDetailEquals(pwaApplicationDetail, pwaApplicationDetail);
  }

  private void removeRecipient(PadPipelineTransfer padPipelineTransfer) {

    if (padPipelineTransfer.getDonorPipeline() == null) {
      transferRepository.delete(padPipelineTransfer);
    } else {
      padPipelineTransfer
          .setRecipientPipeline(null)
          .setRecipientApplicationDetail(null);
      transferRepository.save(padPipelineTransfer);
    }
  }

  private void removeDonor(PadPipelineTransfer padPipelineTransfer) {

    if (padPipelineTransfer.getRecipientPipeline() == null) {
      transferRepository.delete(padPipelineTransfer);
    } else {
      padPipelineTransfer
          .setDonorPipeline(null)
          .setDonorApplicationDetail(null);
      transferRepository.save(padPipelineTransfer);
    }
  }

  public Map<Pipeline, PadPipelineTransfer> getPipelineToTransferMap(PwaApplicationDetail pwaApplicationDetail) {

    var transfers = getTransfersByApplicationDetail(pwaApplicationDetail);
    var pipelinesTransferredOut = new HashMap<Pipeline, PadPipelineTransfer>();
    var pipelinesTransferredIn = new HashMap<Pipeline, PadPipelineTransfer>();

    transfers.forEach(transfer -> {
      if (Objects.equals(transfer.getDonorApplicationDetail(), pwaApplicationDetail)) {
        pipelinesTransferredOut.put(transfer.getDonorPipeline(), transfer);
      } else if (Objects.equals(transfer.getRecipientApplicationDetail(), pwaApplicationDetail)) {
        pipelinesTransferredIn.put(transfer.getRecipientPipeline(), transfer);
      }
    });

    return Stream.of(pipelinesTransferredOut, pipelinesTransferredIn)
        .flatMap(map -> map.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  private boolean isClaimableResourceType(PwaResourceType sourceResourceType, PwaResourceType targetResourceType) {
    return claimableResourceTypes.get(targetResourceType).contains(sourceResourceType);
  }

}
