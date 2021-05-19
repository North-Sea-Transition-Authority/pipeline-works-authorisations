package uk.co.ogauthority.pwa.service.documents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

@Service
public class DocumentViewService {

  public DocumentView createDocumentView(PwaDocumentType documentType,
                                         DocumentSource documentSource,
                                         Collection<SectionClauseVersionDto> clauseVersionDtos) {

    var sectionToClauseVersionMap = clauseVersionDtos.stream()
        .filter(dto -> !SectionClauseVersionStatus.DELETED.equals(dto.getStatus()))
        .collect(Collectors.groupingBy(SectionClauseVersionDto::getSectionName));

    var docTemplateMnem = DocumentTemplateMnem.getMnemFromDocumentSpec(documentSource.getDocumentSpec());

    var docView = new DocumentView(
        documentType,
        documentSource,
        docTemplateMnem);

    var sections = sectionToClauseVersionMap.entrySet().stream()
        .map(entry -> createSectionView(
            documentSource.getDocumentSpec(),
            DocumentSection.valueOf(entry.getKey()),
            entry.getValue()))
        .collect(Collectors.toList());

    docView.setSections(sections);

    docView.getSections()
        .sort(Comparator.comparingInt(SectionView::getDisplayOrder));

    return docView;

  }

  private SectionView createSectionView(DocumentSpec documentSpec,
                                        DocumentSection documentSection,
                                        List<SectionClauseVersionDto> clauseVersionDtos) {

    var sectionView = new SectionView();
    sectionView.setName(documentSection.getDisplayName());
    sectionView.setDisplayOrder(documentSpec.getDisplayOrder(documentSection));
    sectionView.setSectionType(documentSection.getSectionType());

    // group clauses according to their level in the hierarchy, i.e. 1 = top-level, 3 = lowest level, 2 is a child of 1 etc
    var clauseViewLevelToViewMap = clauseVersionDtos.stream()
        .map(SectionClauseVersionView::from)
        .collect(Collectors.groupingBy(SectionClauseVersionView::getLevelNumber));

    // set lowest level children on their parents
    updateParentsWithChildren(clauseViewLevelToViewMap, 3);

    // set mid level children on their parents
    updateParentsWithChildren(clauseViewLevelToViewMap, 2);

    // now we've built the hierarchy, set the top-level clauses (now containing their children) onto the section view
    sectionView.setClauses(clauseViewLevelToViewMap.get(1));

    buildSidebarLinks(sectionView);

    return sectionView;

  }

  private void updateParentsWithChildren(Map<Integer, List<SectionClauseVersionView>> levelNumberToClauseVersionMap,
                                         Integer childLevel) {

    int parentLevel = childLevel - 1;

    levelNumberToClauseVersionMap.getOrDefault(childLevel, List.of()).forEach(child ->

        levelNumberToClauseVersionMap.get(parentLevel).stream()
            .filter(parent -> Objects.equals(parent.getClauseId(), child.getParentClauseId()))
            .findFirst()
            .ifPresent(parent -> parent.getChildClauses().add(child))

    );

  }

  private void buildSidebarLinks(SectionView sectionView) {

    var sidebarLinks = new ArrayList<SidebarSectionLink>();

    sectionView.getClauses().forEach(clause -> {

      var link = SidebarSectionLink.createAnchorLink(clause.getName(), "#clauseId-" + clause.getClauseId());

      sidebarLinks.add(link);

      clause.getChildClauses().forEach(child -> {

        var l = SidebarSectionLink.createAnchorLink(child.getName(), "#clauseId-" + child.getClauseId());

        sidebarLinks.add(l);

      });

    });

    sectionView.setSidebarSectionLinks(sidebarLinks);

  }

}
