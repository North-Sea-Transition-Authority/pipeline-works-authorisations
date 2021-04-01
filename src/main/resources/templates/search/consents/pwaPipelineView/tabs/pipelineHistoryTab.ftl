<#import '../../../../diffedViews/diffedPipelineViews.ftl' as diffedPipelineViews>

<#-- @ftlvariable name="diffedPipelineSummaryModel" type="java.util.Map<java.lang.String, Object>" -->


<#macro tab diffedPipelineSummaryModel>

    <#if diffedPipelineSummaryModel.pipelineHeader?has_content>
        <@diffedPipelineViews.pipelineHeaderDetails pipelineHeader=diffedPipelineSummaryModel.pipelineHeader pipelineIdentsSize=diffedPipelineSummaryModel.pipelineIdents?size!0/>
    </#if>

    <#if diffedPipelineSummaryModel.pipelineIdents?has_content>
        <@fdsTimeline.timeline>
            <@fdsTimeline.timelineSection sectionHeading="">
                <#list diffedPipelineSummaryModel.pipelineIdents as identView>
                    <@diffedPipelineViews.identViewTimelinePoint identView/>
                </#list>
            </@fdsTimeline.timelineSection>
        </@fdsTimeline.timeline>
    <#else>
        <@fdsInsetText.insetText>No idents have been added to this pipeline.</@fdsInsetText.insetText>
    </#if>

</#macro>