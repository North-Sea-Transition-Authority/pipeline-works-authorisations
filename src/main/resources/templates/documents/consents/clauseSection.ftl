<#include '../../layout.ftl'>

<#-- @ftlvariable name="sectionType" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection"-->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->

<div class="clauseSection">

  <@sectionHeader sectionType=sectionType />

  <@pwaClauseList.list documentView=docView clauseActionsUrlFactory="" showSectionHeading=false/>

</div>

<#macro sectionHeader sectionType=sectionType>

  <#switch sectionType>

      <#case "SCHEDULE_2">
        <div class="clauseSectionHeader" style="text-align:center">
          <h2 class="govuk-heading-l">${sectionType.displayName}</h2>
          <p style="text-decoration: underline">Provision of Works Authorisations and Notifications Required</p>
        </div>

    <#default>
  </#switch>

</#macro>