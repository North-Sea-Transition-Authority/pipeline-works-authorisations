<#include '../../pwaLayoutImports.ftl'>
<#import '../../diffedViews/diffedPipelineViews.ftl' as diffedPipelineViews>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="unitMeasurements" type="java.util.Map<java.lang.String, uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="pipelinesHeader">${sectionDisplayText}</h2>

    <#list pipelines as diffablePipeline>
        <h3 class="govuk-heading-m"><@diffChanges.renderDiff diffablePipeline.pipelineHeader.PipelineHeaderView_pipelineName /></h3>
        <@diffedPipelineViews.pipelineHeaderDetails pipelineHeader=diffablePipeline.pipelineHeader pipelineIdentsSize=diffablePipeline.pipelineIdents?size!0 drawingSummaryView=(diffablePipeline.drawingSummaryView)! urlFactory=pipelineDrawingUrlFactory/>
        <#if diffablePipeline.pipelineIdents?has_content>
            <@fdsTimeline.timeline>
                <@fdsTimeline.timelineSection sectionHeading="">
                    <#list diffablePipeline.pipelineIdents as identView>
                        <@diffedPipelineViews.identViewTimelinePoint identView/>
                    </#list>
                </@fdsTimeline.timelineSection>
            </@fdsTimeline.timeline>
        <#else>
            <@fdsInsetText.insetText>No idents have been added to this pipeline.</@fdsInsetText.insetText>
        </#if>


    </#list>

</div>







