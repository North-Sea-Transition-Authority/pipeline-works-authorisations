<#include '../../../layout.ftl'>
<#import 'drawingSummary.ftl' as drawingSummary>

<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory" -->

<#macro pipelineDrawingManagement urlFactory pipelineDrawingSummaryViews=[] validatorFactory="">
  <h2 class="govuk-heading-l">
    Pipeline drawings
  </h2>
    <#if pipelineDrawingSummaryViews?has_content>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>

        <#list pipelineDrawingSummaryViews as summary>
            <@drawingSummary.drawingSummary summary=summary urlFactory=urlFactory validatorFactory=validatorFactory showReferenceAsKey=false showActions=true />
        </#list>
    <#else>
        <@fdsInsetText.insetText>
          No pipeline drawings have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>