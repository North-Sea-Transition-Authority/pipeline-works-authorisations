<#import '../../../../diffedViews/diffedPipelineViews.ftl' as diffedPipelineViews>
<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="diffedPipelineSummaryModel" type="java.util.Map<java.lang.String, Object>" -->
<#-- @ftlvariable name="viewPwaPipelineUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pipelinesVersionSearchSelectorItems" type="java.util.Map<java.lang.String, java.lang.String>" -->


<#macro tab diffedPipelineSummaryModel>

    <@fdsForm.htmlForm actionUrl=springUrl(viewPwaPipelineUrl)>
        <@fdsSearchSelector.searchSelectorEnhanced path="form.pipelineDetailId" options=pipelinesVersionSearchSelectorItems labelText="Select version" />

        <@fdsAction.button buttonText="Show version"/>
    </@fdsForm.htmlForm>

    <@diffChanges.toggler/>

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