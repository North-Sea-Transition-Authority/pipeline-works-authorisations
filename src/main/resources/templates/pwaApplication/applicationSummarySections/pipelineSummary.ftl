<#include '../../pwaLayoutImports.ftl'>
<#import '_pipelineIdentSummary.ftl' as pipelineIdentSummary/>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="pipelinesHeader">${sectionDisplayText}</h2>
  <p class="govuk-body">some pipelines content</p>
  <@pipelineIdentSummary.identView/>
</div>

