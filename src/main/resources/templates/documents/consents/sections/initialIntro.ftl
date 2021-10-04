<#include '../../../layout.ftl'>
<#import '../fragments/introParagraph.ftl' as documentIntroParagraph>

<#-- @ftlvariable name="sectionType" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection"-->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->
<#-- @ftlvariable name="fieldNameOrOther" type="String"-->
<#-- @ftlvariable name="introParagraph" type="String"-->

<div>

  <div class="pwa-clause-section-header">
    <h2 class="govuk-heading-m">${fieldNameOrOther} DEVELOPMENT</h2>
    <h2 class="govuk-heading-m" style="text-decoration: underline">
      THE OIL AND GAS AUTHORITY<br/>
      PETROLEUM ACT 1998<br/>
      SUBMARINE PIPELINE WORKS AUTHORISATION</h2>
  </div>

  <@documentIntroParagraph.paragraph paragraphText=introParagraph/>

  <@pwaClauseList.list documentView=docView clauseActionsUrlProvider="" showSectionHeading=false showClauseHeadings=sectionType.clauseDisplay == "SHOW_HEADING" />

</div>