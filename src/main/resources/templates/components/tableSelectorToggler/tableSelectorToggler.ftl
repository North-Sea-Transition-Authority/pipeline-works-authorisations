<#include '../../layout.ftl'/>

<#macro linksToggler tableId
  selectAllText="Select all"
  selectNoneText="Select none"
  selectAllScreenReaderText=""
  selectNoneScreenReaderText="">
    <#local defaultLinkClass="govuk-link govuk-link--no-visited-state" />

    <div class="table-selection-toggler" data-table-id="${tableId}">
      <@fdsAction.link linkText=selectAllText linkUrl="#" linkClass="${defaultLinkClass} table-selection-toggler__select-all-link" linkScreenReaderText=selectAllScreenReaderText/>
      <@fdsAction.link linkText=selectNoneText linkUrl="#" linkClass="${defaultLinkClass} table-selection-toggler__select-none-link" linkScreenReaderText=selectNoneScreenReaderText/>
    </div>
</#macro>



