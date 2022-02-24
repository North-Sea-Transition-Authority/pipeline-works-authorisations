<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">Where the problem with a pipeline(s) may not be clearly identified and there may be various points
      of possible failure, NSTA may consider an Options case. To apply under the above circumstances, the Holder should
      provide details to why they would like NSTA to consider the case to be handled as an Options.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} from receipt of a
      satisfactory application to issuing the authorisation.</p>

  </@fdsStartPage.startPage>

</@defaultPage>