<#include '../../../layout.ftl'>
<#import 'drawingSummary.ftl' as drawingSummary>

<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory" -->
<#-- @ftlvariable name="validatorFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingValidationFactory" -->

<#macro pipelineDrawingManagement urlFactory validatorFactory pipelineDrawingSummaryViews=[]>
  <h2 class="govuk-heading-l">
    Pipeline schematics
  </h2>
    <#if pipelineDrawingSummaryViews?has_content>
        <@fdsAction.link linkText="Add pipeline schematic" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>

        <#list pipelineDrawingSummaryViews as summary>
            <@drawingSummary.drawingSummary summary=summary urlFactory=urlFactory validatorFactory=validatorFactory showReferenceAsKey=false showActions=true />
        </#list>
    <#else>
        <@fdsInsetText.insetText>
          No pipeline schematics have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add pipeline schematic" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>
</#macro>