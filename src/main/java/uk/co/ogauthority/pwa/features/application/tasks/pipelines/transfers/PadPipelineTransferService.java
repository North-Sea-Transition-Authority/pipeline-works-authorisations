package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.TransferHistoryView;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;
  private final PadPipelineService padPipelineService;
  private final PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;
  private final PipelineDetailService pipelineDetailService;
  private final MasterPwaService masterPwaService;

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository,
                                    PadPipelineService padPipelineService,
                                    PadPipelineTransferClaimValidator padPipelineTransferClaimValidator,
                                    PipelineDetailService pipelineDetailService, MasterPwaService masterPwaService) {
    this.transferRepository = transferRepository;
    this.padPipelineService = padPipelineService;
    this.padPipelineTransferClaimValidator = padPipelineTransferClaimValidator;
    this.pipelineDetailService = pipelineDetailService;
    this.masterPwaService = masterPwaService;
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
      transferRepository.save(transfer);

      var claimedDetail = pipelineDetailService.getLatestByPipelineId(claimedPipeline.getPipeline().getId());
      pipelineDetailService.updateTransferredPipelineDetails(pipelineDetail, claimedDetail);
    }
  }

  public Map<String, String> getClaimablePipelinesForForm(PwaResourceType resourceType) {
    var pipelineIds = transferRepository.findAllByRecipientApplicationDetailIsNull().stream()
        .filter(padPipelineTransfer -> padPipelineTransfer.getDonorApplicationDetail().getResourceType().equals(resourceType))
        .map(padPipelineTransfer -> padPipelineTransfer.getDonorPipeline().getId())
        .collect(Collectors.toList());

    return pipelineDetailService.getLatestPipelineDetailsForIds(pipelineIds)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            pipelineDetail -> String.valueOf(pipelineDetail.getPipelineId().getPipelineIdAsInt()),
            PipelineDetail::getPipelineNumber)
        );
  }

  public BindingResult validateClaimForm(PadPipelineTransferClaimForm form, BindingResult bindingResult) {
    padPipelineTransferClaimValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public List<PadPipelineTransfer> findUnclaimedByDonorApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(applicationDetail);
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

  public List<TransferHistoryView> getTransferHistoryViews(List<Integer> pipelineIds) {
    Map<PadPipelineTransfer, Boolean> transfers = findAllByPipelineIds(pipelineIds).stream()
        .collect(Collectors.toMap(
            Function.identity(),
            padPipelineTransfer -> isTransfereeDonor(padPipelineTransfer, pipelineIds))
        );

    Map<PadPipelineTransfer, MasterPwa> refMap = transfers.keySet().stream()
            .collect(StreamUtils.toLinkedHashMap(
                Function.identity(),
                padPipelineTransfer -> getTransfereeDetail(padPipelineTransfer, pipelineIds).getMasterPwa()
            ));

    var masterPwaDetails = masterPwaService.findAllCurrentDetailsIn(refMap.values()).stream()
        .collect(Collectors.toMap(MasterPwaDetail::getMasterPwa, Function.identity()));

    return transfers.entrySet().stream()
        .map(entry -> {
          var originalId = getOriginalPipelineId(entry.getKey(), entry.getValue());

          var transfereeConsentRef = masterPwaDetails.get(getTransfereeDetail(entry.getKey(), pipelineIds)
                  .getMasterPwa()).getReference();

          var viewUrl = getViewConsentUrl(getTransfereeDetail(entry.getKey(), pipelineIds).getMasterPwa().getId());

          return new TransferHistoryView()
              .setOriginalPipelineId(originalId)
              .setTransfereeConsentReference(transfereeConsentRef)
              .setViewUrl(viewUrl);
        })
        .collect(Collectors.toList());
  }

  private Integer getOriginalPipelineId(PadPipelineTransfer padPipelineTransfer, boolean isTransfereeDonor) {
    return isTransfereeDonor
        ? padPipelineTransfer.getRecipientPipeline().getId()
        : padPipelineTransfer.getDonorPipeline().getId();
  }

  private PwaApplicationDetail getTransfereeDetail(PadPipelineTransfer padPipelineTransfer, List<Integer> pipelineIds) {
    return isTransfereeDonor(padPipelineTransfer, pipelineIds)
        ? padPipelineTransfer.getDonorApplicationDetail()
        : padPipelineTransfer.getRecipientApplicationDetail();
  }

  private boolean isTransfereeDonor(PadPipelineTransfer padPipelineTransfer, List<Integer> pipelineIds) {
    return pipelineIds.contains(padPipelineTransfer.getRecipientPipeline().getId());
  }

  private String getViewConsentUrl(int id) {
    return ReverseRouter.route(on(PwaViewController.class).renderViewPwa(id, PwaViewTab.PIPELINES, null, null, false));
  }

  private List<PadPipelineTransfer> findAllByPipelineIds(List<Integer> pipelineIds) {
    return transferRepository.findAllByDonorPipeline_IdInOrRecipientPipeline_IdIn(pipelineIds, pipelineIds);
  }

  private Optional<PadPipelineTransfer> findByPipelineId(Integer pipelineId) {
    return transferRepository.findByDonorPipeline_IdOrRecipientPipeline_Id(pipelineId, pipelineId);
  }

  private void removeRecipient(PadPipelineTransfer padPipelineTransfer) {
    pipelineDetailService.clearTransferredPipelineDetails(padPipelineTransfer.getRecipientPipeline().getId(), false);

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
    pipelineDetailService.clearTransferredPipelineDetails(padPipelineTransfer.getDonorPipeline().getId(), true);

    if (padPipelineTransfer.getRecipientPipeline() == null) {
      transferRepository.delete(padPipelineTransfer);
    } else {
      padPipelineTransfer
          .setDonorPipeline(null)
          .setDonorApplicationDetail(null);
      transferRepository.save(padPipelineTransfer);
    }
  }
}
