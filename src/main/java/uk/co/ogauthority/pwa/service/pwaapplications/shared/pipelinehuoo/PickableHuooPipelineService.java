package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;

/**
 * Get or resolve pipelineIdentifiers using the PickableHuooPipelineOption to translate from form or url arguments to
 * legitimate options that can be picked.
 */
@Service
public class PickableHuooPipelineService {

  private final PipelineService pipelineService;
  private final PadPipelineService padPipelineService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PickableHuooPipelineService(
      PipelineService pipelineService,
      PadPipelineService padPipelineService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.padPipelineService = padPipelineService;
    this.pipelineService = pipelineService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  public Set<PipelineIdentifier> getPickedPipelinesFromStrings(PwaApplicationDetail pwaApplicationDetail,
                                                               HuooRole huooRole,
                                                               Set<String> stringSet) {
    var pickedPipelines = stringSet.stream()
        .map(PickableHuooPipelineId::from)
        .collect(toSet());

    return getPickedPipelines(pwaApplicationDetail, huooRole, pickedPipelines);
  }

  /* Get pipeline entities for pickablePipelineIds */
  private Set<PipelineIdentifier> getPickedPipelines(PwaApplicationDetail pwaApplicationDetail,
                                                     HuooRole huooRole,
                                                     Set<PickableHuooPipelineId> pickedPipelineIds) {
    return reconcilePickablePipelineIds(pwaApplicationDetail, huooRole, pickedPipelineIds)
        .stream()
        .map(ReconciledHuooPickablePipeline::getPipelineIdentifier)
        .collect(toSet());

  }

  /**
   * <p>Pickable pipeline are from aboth the application and PWA as a whole. If an application updates a consented PWA
   * pipeline, we want the pickable option to show the application details and not the consented details.</p>
   *
   * <p>The returned map will have not have any pipeline splits represented as they only exist in the
   * context of an applications HUOO roles.</p>
   */
  private Map<PipelineIdentifier, PickableHuooPipelineOption> getWholePipelinePickableOptionsForApp(
      PwaApplicationDetail pwaApplicationDetail) {
    // 1. get pickable pipeline options from pipelines represented within the application
    // 2. get pickable pipeline options from consented model
    // 3. add consented pipelines to return map where the same pipeline does not exist in application
    // 4. add all application pipelines to return map
    // return complete set.

    Map<PipelineIdentifier, PickableHuooPipelineOption> applicationPipelineIdentifiers = padPipelineService
        .getAllPadPipelineSummaryDtosForApplicationDetail(pwaApplicationDetail)
        .stream()
        .collect(toMap(PadPipelineSummaryDto::getPipelineId, PickableHuooPipelineOption::from));

    Map<PipelineIdentifier, PickableHuooPipelineOption> consentedPipelineIdentifiers = pipelineService
        .getActivePipelineDetailsForApplicationMasterPwa(pwaApplicationDetail.getPwaApplication())
        .stream()
        .collect(toMap(PipelineDetail::getPipelineId, PickableHuooPipelineOption::from));

    Map<PipelineIdentifier, PickableHuooPipelineOption> pickablePipelinesLookup = new HashMap<>();

    consentedPipelineIdentifiers.forEach((key, value) -> {
      if (!applicationPipelineIdentifiers.containsKey(key)) {
        pickablePipelinesLookup.put(key, value);
      }
    });

    pickablePipelinesLookup.putAll(applicationPipelineIdentifiers);

    return pickablePipelinesLookup;
  }

  /**
   * <p>Pickable pipelines for the application as a whole are those added by the app, imported for update by the app,
   * or active pipeline linked to the application's master pwa which are not imported into the application.</p>
   *
   * <p>For every pipeline which has been split for the HUOO role, we need to include each split segment as a discrete option
   * and not include a row which represents the entire pipeline.</p>
   */
  public Set<PickableHuooPipelineOption> getAllPickablePipelinesForApplicationAndRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {

    Map<PipelineIdentifier, PickableHuooPipelineOption> pickablePipelinesLookup = getWholePipelinePickableOptionsForApp(
        pwaApplicationDetail
    );

    Set<PipelineIdentifier> splitPipelinesForRole = padOrganisationRoleService.getPipelineSplitsForRole(
        pwaApplicationDetail,
        huooRole
    );

    // replace entries for whole pipelines where a pipeline has been split
    Set<PipelineId> splitPipelines = new HashSet<>();
    splitPipelinesForRole.forEach(splitPipelineIdentifier -> {
      var splitPipelineOption = PickableHuooPipelineOption.duplicateOptionForPipelineIdentifier(
          splitPipelineIdentifier,
          pickablePipelinesLookup.get(splitPipelineIdentifier.getPipelineId())
      );
      splitPipelines.add(splitPipelineIdentifier.getPipelineId());
      pickablePipelinesLookup.put(splitPipelineIdentifier, splitPipelineOption);
    });

    // remove records for whole pipelines where splits are now within the map
    splitPipelines.forEach(pickablePipelinesLookup::remove);

    return new HashSet<>(pickablePipelinesLookup.values());

  }

  /**
   * Forms and links might provide strings which represent pickable pipelines. Form the string arguments provided
   * by the clients, we need to make sure that we only deal with arguments that can be successfully reconciled
   * with valid options for our application and HUOO role.
   */
  public Set<ReconciledHuooPickablePipeline> reconcilePickablePipelinesFromStrings(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<String> pickableHuooPipelineStringIds) {
    var pickedPipelineIds = pickableHuooPipelineStringIds.stream()
        .map(PickableHuooPipelineId::from)
        .collect(toSet());

    return reconcilePickablePipelineIds(pwaApplicationDetail, huooRole, pickedPipelineIds);

  }

  /**
   * Performs the same function as {@link PickableHuooPipelineService#reconcilePickablePipelinesFromStrings}
   * but deals with strings wrapped in {@link PickableHuooPipelineId} objects.
   */
  public Set<ReconciledHuooPickablePipeline> reconcilePickablePipelineIds(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<PickableHuooPipelineId> pickableHuooPipelineIds) {

    Map<PipelineIdentifier, PickableHuooPipelineOption> allPickablePipelinesForRoleLookup = getAllPickablePipelinesForApplicationAndRole(
        pwaApplicationDetail, huooRole)
        .stream()
        .collect(toMap(PickableHuooPipelineOption::asPipelineIdentifier, o -> o));

    return pickableHuooPipelineIds.stream()
        // Filter out options which cannot be decoded
        .filter(o -> !o.decodePickableStringId().isEmpty())
        // Filter out options which do not appear in the available pickable pipeline options for role
        // .get() here and below is safe as we do the filter above
        .filter(o -> allPickablePipelinesForRoleLookup.containsKey(o.decodePickableStringId().get()))
        // create reconciled object with search key and decoded pipeline identifier
        .map(o -> new ReconciledHuooPickablePipeline(o, o.decodePickableStringId().get()))
        .collect(toSet());

  }

}
