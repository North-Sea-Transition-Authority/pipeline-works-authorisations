<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>
  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>
    <p class="govuk-body">A Pipeline Records Management variation is used when you need to update the existing PWA for the following reasons:</p>
    <ul class="govuk-list govuk-list--bullet">
      <li>To correct pipeline records with administrative errors</li>
      <li>To update pipeline records in line with the as-built status following the completion of a work scope</li>
      <li>To revert pipeline records back following a failure to complete works</li>
      <li>To upload pipeline Table A data into the PWA Portal</li>
      <li>Bringing previously unnumbered pipelines into the PWA regime outside of any associated work scope</li>
    </ul>
    <p class="govuk-body">This consent will supersede any existing consent relevant to the pipelines contained within and simultaneously update PWA Portal records. Where new pipelines are brought into the regime this becomes the first formal variation.</p>
    <p class="govuk-body">No consultation is required on receipt of a satisfactory application and your consent will be issued within a time deemed reasonable by the Consents team.</p>
  </@fdsStartPage.startPage>
</@defaultPage>