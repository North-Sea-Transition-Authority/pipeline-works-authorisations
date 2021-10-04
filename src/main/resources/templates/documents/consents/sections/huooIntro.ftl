<#include '../../../layout.ftl'>
<#import '../fragments/introParagraph.ftl' as documentIntroParagraph>

<#-- @ftlvariable name="sectionType" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection"-->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->
<#-- @ftlvariable name="introParagraph" type="String"-->
<#-- @ftlvariable name="orgRoleNameToTextMap" type="java.util.Map<String, String>"-->

<div>

  <div class="pwa-clause-section-header">
    <h2 class="govuk-heading-m">THE OIL AND GAS AUTHORITY</h2>
    <h2 class="govuk-heading-m">PETROLEUM ACT 1998</h2>
    <h2 class="govuk-heading-m">CONSENT TO CHANGE THE SPECIFIED ${orgRoleNameToTextMap["HOLDER"]}, ${orgRoleNameToTextMap["USER"]}, 
    ${orgRoleNameToTextMap["OPERATOR"]} & ${orgRoleNameToTextMap["OWNER"]}</h2>
    <h2 class="govuk-heading-m">OF THE</h2>
    <h2 class="govuk-heading-m">PIPELINE WORKS AUTHORISATION</h2>
  </div>

  <@documentIntroParagraph.paragraph paragraphText=introParagraph/>

  <@pwaClauseList.list documentView=docView clauseActionsUrlProvider="" showSectionHeading=false showClauseHeadings=sectionType.clauseDisplay == "SHOW_HEADING" />

</div>