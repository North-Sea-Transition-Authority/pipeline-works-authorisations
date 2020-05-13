package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Service
public class PadPipelineIdentService {

  private final PadPipelineIdentRepository repository;
  private final PadPipelineIdentDataService identDataService;

  @Autowired
  public PadPipelineIdentService(PadPipelineIdentRepository repository,
                                 PadPipelineIdentDataService identDataService) {
    this.repository = repository;
    this.identDataService = identDataService;
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

  public List<GroupedIdentView> getGroupedIdentViews(PadPipeline pipeline) {
    var identViews = getIdentViews(pipeline);
    var list = new ArrayList<List<IdentView>>();
    var groupList = new ArrayList<IdentView>();

    for (int i = 0; i < identViews.size(); i++) {
      if (i == 0) {
        groupList.add(identViews.get(i));
        continue;
      }
      var previousView = identViews.get(i - 1);
      var currentView = identViews.get(i);
      if (!previousView.getToLocation().toLowerCase().equals(currentView.getFromLocation().toLowerCase())) {
        list.add(groupList);
        groupList = new ArrayList<>();
      }
      groupList.add(identViews.get(i));
    }

    list.add(groupList);

    return list.stream()
        .filter(viewList -> !viewList.isEmpty())
        .map(GroupedIdentView::new)
        .collect(Collectors.toUnmodifiableList());
  }

  public Optional<PadPipelineIdent> getMaxIdent(PadPipeline pipeline) {
    return repository.findTopByPadPipelineOrderByIdentNoDesc(pipeline);
  }

  @Transactional
  public void addIdent(PadPipeline pipeline, PipelineIdentForm form) {

    var numberOfIdents = repository.countAllByPadPipeline(pipeline);
    var ident = new PadPipelineIdent(pipeline, numberOfIdents.intValue() + 1);

    saveEntityUsingForm(ident, form);

    identDataService.addIdentData(ident, form.getDataForm());

  }

  public void saveEntityUsingForm(PadPipelineIdent ident, PipelineIdentForm form) {

    ident.setFromLocation(form.getFromLocation());
    ident.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));

    ident.setToLocation(form.getToLocation());
    ident.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));

    ident.setLength(form.getLength());

    repository.save(ident);

  }

}
