<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<#-- @ftlvariable name="unitMeasurements" type="java.util.Map<java.lang.String, uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="pipelinesHeader">${sectionDisplayText}</h2>

    <#list pipelines as diffablePipeline>
        <h3 class="govuk-heading-m"><@diffChanges.renderDiff diffablePipeline.pipelineHeader.PipelineDiffableSummary_pipelineName /></h3>
        <@pipelineHeaderDetails diffablePipeline.pipelineHeader/>
        <#if diffablePipeline.pipelineIdents?has_content>
            <@fdsTimeline.timeline>
                <@fdsTimeline.timelineSection sectionHeading="">
                    <#list diffablePipeline.pipelineIdents as identView>
                        <@identViewTimelinePoint identView/>
                    </#list>
                </@fdsTimeline.timelineSection>
            </@fdsTimeline.timeline>
        <#else>
            <@fdsInsetText.insetText>No idents have been added to this pipeline.</@fdsInsetText.insetText>
        </#if>


    </#list>

</div>


<#macro pipelineHeaderDetails pipelineHeader>
  <p class="govuk-body">TODO PIPELINES HEADER INFO</p>
</#macro>


<#macro identViewTimelinePoint identView>
    <#-- interpret diffed Boolean -->
    <#local connectedToNext = identView.IdentDiffableView_connectedToNext.currentValue=='Yes'/>

    <#-- Put all diffed values into so that the diff macro output can be given to fds macros expecting variables, not nested content -->
    <#local fromLocation><@diffChanges.renderDiff identView.IdentDiffableView_fromLocation /></#local>
    <#local toLocation><@diffChanges.renderDiff identView.IdentDiffableView_toLocation /></#local>

    <#local identNumber><@diffChanges.renderDiff identView.IdentDiffableView_identNumber /></#local>
    <#local length><@diffChanges.renderDiff identView.IdentDiffableView_length /></#local>
    <#local fromCoordinates><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_fromCoordinates multiLineTextBlockClass="fds-data-items-list" /></#local>
    <#local toCoordinates><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_toCoordinates multiLineTextBlockClass="fds-data-items-list"/></#local>

    <#-- These data items need the multiLineTextBlockClass as multi core pipelines allow paragraph text to be entered   -->
    <#local externalDiameter><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_externalDiameter multiLineTextBlockClass="fds-data-items-list"/></#local>
    <#local internalDiameter><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_internalDiameter multiLineTextBlockClass="fds-data-items-list"/></#local>
    <#local wallThickness><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_wallThickness multiLineTextBlockClass="fds-data-items-list" /></#local>
    <#local maop><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_maop multiLineTextBlockClass="fds-data-items-list"/></#local>

    <#local insulationCoatingType><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_insulationCoatingType multiLineTextBlockClass="fds-data-items-list"/></#local>
    <#local productsToBeConveyed><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_productsToBeConveyed multiLineTextBlockClass="fds-data-items-list"/></#local>
    <#local componentPartsDescription><@diffChanges.renderDiff diffedField=identView.IdentDiffableView_componentPartsDescription multiLineTextBlockClass="fds-data-items-list" /></#local>

    <@fdsTimeline.timelineTimeStamp timeStampHeading=fromLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp" >

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValuesNumber smallNumber=true key="${identNumber}" value="Ident number"/>
            <@fdsDataItems.dataValues key="Length (${unitMeasurements.METRE.suffixDisplay})" value="${length!}"/>
            <@fdsDataItems.dataValues key="From (WGS 84)" value="${fromCoordinates}"/>
            <@fdsDataItems.dataValues key="To (WGS 84)" value="${toCoordinates}"/>
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="External diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${externalDiameter}" />
            <@fdsDataItems.dataValues key="Internal diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${internalDiameter}"/>
            <@fdsDataItems.dataValues key="Wall thickness (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${wallThickness}" />
            <@fdsDataItems.dataValues key="MAOP (${unitMeasurements.BAR_G.suffixDisplay})" value="${maop}" />
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Insulation / coating type" value="${insulationCoatingType}" />
            <@fdsDataItems.dataValues key="Products to be conveyed" value="${productsToBeConveyed}" />
        </@fdsDataItems.dataItem>
        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Description of component parts" value="${componentPartsDescription}"/>
        </@fdsDataItems.dataItem>

    </@fdsTimeline.timelineTimeStamp>

    <#if !connectedToNext>
        <@fdsTimeline.timelineTimeStamp timeStampHeading=toLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
    </#if>

</#macro>
