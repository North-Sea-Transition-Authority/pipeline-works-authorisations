<#include '../../pwaLayoutImports.ftl'>
<#import '_pipelineIdentSummary.ftl' as pipelineIdentSummary/>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="pipelines" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary>" -->
<#-- @ftlvariable name="unitMeasurements" type="java.util.Map<java.lang.String, uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="pipelinesHeader">${sectionDisplayText}</h2>

    <#list pipelines as diffablePipeline>
      <h3 class="govuk-heading-m">${diffablePipeline.pipelineName}</h3>
      <p class="govuk-body">TODO PIPELINES HEADER INFO</p>

        <@fdsTimeline.timeline>
            <@fdsTimeline.timelineSection sectionHeading="">
                <#list diffablePipeline.identViews as identView>
                  <@pipelineIdentSummary.identViewTimelinePoint identView unitMeasurements/>
                </#list>
            </@fdsTimeline.timelineSection>
        </@fdsTimeline.timeline>
    </#list>

</div>

