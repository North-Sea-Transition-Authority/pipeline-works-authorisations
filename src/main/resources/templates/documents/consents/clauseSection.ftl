<#include '../../layout.ftl'>

<#-- @ftlvariable name="sectionType" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection"-->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->

<div class="clauseSection" style="page-break-before: always">

  <@sectionHeader sectionType=sectionType />

  <@pwaClauseList.list documentView=docView clauseActionsUrlFactory="" showSectionHeading=false showClauseHeadings=sectionType.clauseDisplay == "SHOW_HEADING" />

</div>

<#macro sectionHeader sectionType=sectionType>

  <#switch sectionType>

      <#case "SCHEDULE_2">
        <div class="pwa-clause-section-header">
          <h2 class="govuk-heading-l">${sectionType.displayName}</h2>
          <p style="text-decoration: underline">Provision of Works Authorisations and Notifications Required</p>
        </div>
        <#break>

    <#default>
  </#switch>

</#macro>