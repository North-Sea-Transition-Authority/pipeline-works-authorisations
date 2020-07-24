package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Service
public class PadPipelineIdentService {

  private final PadPipelineIdentRepository repository;
  private final PadPipelineIdentDataService identDataService;
  private final PadPipelinePersisterService padPipelinePersisterService;

  @Autowired
  public PadPipelineIdentService(PadPipelineIdentRepository repository,
                                 PadPipelineIdentDataService identDataService,
                                 PadPipelinePersisterService padPipelinePersisterService) {
    this.repository = repository;
    this.identDataService = identDataService;
    this.padPipelinePersisterService = padPipelinePersisterService;
  }

  public PadPipelineIdent getIdent(PadPipeline pipeline, Integer identId) {
    return repository.getPadPipelineIdentByPadPipelineAndId(pipeline, identId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find ident with id '%d' linked to pipeline with id '%d'", identId,
                pipeline.getId())));
  }

  public IdentView getIdentView(PadPipeline pipeline, Integer identId) {
    var padIdent = getIdent(pipeline, identId);
    var identData = identDataService.getIdentData(padIdent);
    return new IdentView(identData);
  }

  public List<IdentView> getIdentViews(PadPipeline pipeline) {
    var idents = repository.getAllByPadPipeline(pipeline);
    var identData = identDataService.getDataFromIdentList(idents);
    return identData.keySet()
        .stream()
        .sorted(Comparator.comparing(PadPipelineIdent::getIdentNo))
        .map(ident -> new IdentView(identData.get(ident)))
        .collect(Collectors.toUnmodifiableList());
  }

  public ConnectedPipelineIdentSummaryView getConnectedPipelineIdentSummaryView(PadPipeline pipeline) {
    List<IdentView> identViews = getIdentViews(pipeline);
    var list = new ArrayList<List<IdentView>>();
    var groupList = new ArrayList<IdentView>();
    list.add(groupList);

    for (int i = 0; i < identViews.size(); i++) {
      if (i == 0) {
        // If first ident, there's nothing to compare to.
        groupList.add(identViews.get(i));
        continue;
      }
      var previousView = identViews.get(i - 1);
      var currentView = identViews.get(i);
      // Compare "fromLocation" to the previous ident's "toLocation".
      // If locations are different, add to a new group. If locations are the same, add to the existing group.
      if (!previousView.getToLocation().equalsIgnoreCase(currentView.getFromLocation())) {
        groupList = new ArrayList<>();
        list.add(groupList);
      }
      groupList.add(identViews.get(i));
    }

    List<ConnectedPipelineIdentsView> connectedIdents = list.stream()
        .filter(viewList -> !viewList.isEmpty())
        .map(ConnectedPipelineIdentsView::new)
        .collect(Collectors.toUnmodifiableList());

    return new ConnectedPipelineIdentSummaryView(connectedIdents);
  }

  public Optional<PadPipelineIdent> getMaxIdent(PadPipeline pipeline) {
    return repository.findTopByPadPipelineOrderByIdentNoDesc(pipeline);
  }

  @Transactional
  public void addIdent(PadPipeline pipeline, PipelineIdentForm form) {

    var numberOfIdents = repository.countAllByPadPipeline(pipeline);
    var ident = new PadPipelineIdent(pipeline, numberOfIdents.intValue() + 1);

    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
  }

  @Transactional
  public void addIdentAtPosition(PadPipeline pipeline, PipelineIdentForm form, Integer position) {

    var ident = new PadPipelineIdent(pipeline, position);

    var idents = repository.getAllByPadPipeline(pipeline);
    idents.stream()
        .filter(existingIdent -> existingIdent.getIdentNo() >= position)
        .forEachOrdered(existingIdent -> existingIdent.setIdentNo(existingIdent.getIdentNo() + 1));

    repository.saveAll(idents);

    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
  }

  @Transactional
  public void updateIdent(PadPipelineIdent ident, PipelineIdentForm form) {
    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
  }

  @Transactional
  public Optional<PadPipelineIdent> getIdentByIdentNumber(PadPipeline pipeline, Integer identNumber) {
    return repository.getByPadPipelineAndAndIdentNo(pipeline, identNumber);
  }

  public void saveEntityUsingForm(PadPipelineIdent ident, PipelineIdentForm form) {

    ident.setFromLocation(form.getFromLocation());
    ident.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    ident.setToLocation(form.getToLocation());
    ident.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));
    ident.setLength(form.getLength());

    repository.save(ident);

    identDataService.getOptionalOfIdentData(ident)
        .ifPresentOrElse(
            (padPipelineIdentData) -> identDataService.updateIdentData(ident, form.getDataForm()),
            () -> identDataService.addIdentData(ident, form.getDataForm()));
  }

  public void mapEntityToForm(PadPipelineIdent ident, PipelineIdentForm form) {
    var fromForm = new CoordinateForm();
    var toForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(ident.getFromCoordinates(), fromForm);
    CoordinateUtils.mapCoordinatePairToForm(ident.getToCoordinates(), toForm);

    form.setFromCoordinateForm(fromForm);
    form.setToCoordinateForm(toForm);
    form.setFromLocation(ident.getFromLocation());
    form.setLength(ident.getLength());
    form.setToLocation(ident.getToLocation());
    var dataForm = identDataService.getDataFormOfIdent(ident);
    form.setDataForm(dataForm);
  }

  @Transactional
  public void removeIdent(PadPipelineIdent pipelineIdent) {
    identDataService.removeIdentData(pipelineIdent);
    repository.delete(pipelineIdent);
    var remainingIdents = repository.getAllByPadPipeline(pipelineIdent.getPadPipeline());

    remainingIdents.stream()
        .filter(ident -> ident.getIdentNo() > pipelineIdent.getIdentNo())
        .forEachOrdered(ident -> ident.setIdentNo(ident.getIdentNo() - 1));

    repository.saveAll(remainingIdents);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(pipelineIdent.getPadPipeline());
  }

  public boolean isSectionValid(PadPipeline pipeline) {
    return !repository.countAllByPadPipeline(pipeline).equals(0L);
  }

  public List<PadPipelineIdent> getAllIdentsByPadPipelineIds(List<PadPipelineId> padPipelineIds) {

    List<Integer> ids = padPipelineIds.stream()
        .map(PadPipelineId::asInt)
        .collect(Collectors.toList());

    return repository.getAllByPadPipeline_IdIn(ids);

  }

}
