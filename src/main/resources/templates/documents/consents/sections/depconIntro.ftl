<#include '../../../layout.ftl'>

<#-- @ftlvariable name="sectionType" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection"-->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->
<#-- @ftlvariable name="introParagraph" type="String"-->

<div>

  <div class="pwa-clause-section-header">
    <h2 class="govuk-heading-m">
      THE OIL AND GAS AUTHORITY<br/>
      PETROLEUM ACT 1998<br/>
      DEPOSIT OF MATERIALS
    </h2>
  </div>

  <p class="multi-line-text" style="text-align: center">${introParagraph?no_esc}</p>

  <@pwaClauseList.list documentView=docView clauseActionsUrlFactory="" showSectionHeading=false showClauseHeadings=sectionType.clauseDisplay == "SHOW_HEADING" />

</div>