<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentSummaryView" -->
<#-- @ftlvariable name="lastConnectedPipelineIdentView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentsView" -->
<#-- @ftlvariable name="identUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentUrlFactory" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType" -->

<@defaultPage htmlTitle="${pipelineOverview.pipelineNumber} idents" breadcrumbs=true fullWidthColumn=true caption="${pipelineOverview.length}m ${pipelineOverview.pipelineType.displayName}" pageHeading="${pipelineOverview.pipelineNumber} idents">

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

    <#if summaryView?has_content && summaryView.connectedPipelineIdents?has_content>
        <@fdsTimeline.timeline>
            <@fdsTimeline.timelineSection sectionHeading="">
                <#assign pastFirstIteration = false/>
                <#list summaryView.connectedPipelineIdents as connectedPipelineIdentView>
                    <#if pastFirstIteration == true && lastConnectedPipelineIdentView?has_content>
                        <@fdsTimeline.timelineTimeStamp timeStampHeading="${lastConnectedPipelineIdentView.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
                    </#if>
                    <#list connectedPipelineIdentView.identViews as identView>
                        <#assign timelineAction>
                            <@fdsAction.link linkText="Insert ident above" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getInsertAboveUrl(identView.identId)) linkScreenReaderText="Insert ident above" />
                            <@fdsAction.link linkText="Edit ident" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getEditUrl(identView.identId)) linkScreenReaderText="Edit ident ${identView.identNumber}" />
                            <@fdsAction.link linkText="Remove ident" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getRemoveUrl(identView.identId)) linkScreenReaderText="Remove ident ${identView.identNumber}" />
                        </#assign>
                        <@fdsTimeline.timelineTimeStamp timeStampHeading=identView.fromLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp" timelineActionContent=timelineAction>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.identNumber}" value="Ident number"/>
                                <@fdsDataItems.dataValues key="Length" value="${identView.length}m"/>
                                <#assign from>
                                    <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
                                </#assign>
                                <#assign to>
                                    <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
                                </#assign>
                                <@fdsDataItems.dataValues key="From (coordinates)" value=from/>
                                <@fdsDataItems.dataValues key="To (coordinates)" value=to/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@dataValueForCoreType coreType=coreType key="External diameter" valueSingleCore=(identView.externalDiameter)! valueMultiCore=(identView.externalDiameterMultiCore)! measurementUnit="mm"/>
                                <@dataValueForCoreType coreType=coreType key="Internal diameter" valueSingleCore=(identView.internalDiameter)! valueMultiCore=(identView.internalDiameterMultiCore)! measurementUnit="mm"/>
                                <@dataValueForCoreType coreType=coreType key="Wall thickness" valueSingleCore=(identView.wallThickness)! valueMultiCore=(identView.wallThicknessMultiCore)! measurementUnit="mm"/>
                                <@dataValueForCoreType coreType=coreType key="MAOP" valueSingleCore=(identView.maop)! valueMultiCore=(identView.maopMultiCore)! measurementUnit="barg"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@dataValueForCoreType coreType=coreType key="Insulation / coating type" valueSingleCore=(identView.insulationCoatingType)! valueMultiCore=(identView.insulationCoatingTypeMultiCore)!/>
                                <@dataValueForCoreType coreType=coreType key="Products to be conveyed" valueSingleCore=(identView.productsToBeConveyed)! valueMultiCore=(identView.productsToBeConveyedMultiCore)!/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValues key="Description of component parts" value="${identView.componentPartsDescription}"/>
                            </@fdsDataItems.dataItem>
                        </@fdsTimeline.timelineTimeStamp>
                    </#list>
                    <#assign lastConnectedPipelineIdentView = connectedPipelineIdentView/>
                    <#assign pastFirstIteration = true/>
                </#list>
                <@fdsTimeline.timelineTimeStamp timeStampHeading="${lastConnectedPipelineIdentView.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
            </@fdsTimeline.timelineSection>
        </@fdsTimeline.timeline>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl) errorMessage=errorMessage!/>
    </@fdsForm.htmlForm>

</@defaultPage>


<#macro dataValueForCoreType coreType key valueSingleCore valueMultiCore measurementUnit="">
    <#assign unit = measurementUnit/>  
    <#if coreType == "SINGLE_CORE">  
        <#if (valueSingleCore?has_content) == false>
            <#assign unit = ""/>              
        </#if>        
        <@fdsDataItems.dataValues key=key value="${valueSingleCore}${unit}"/>        
    <#else>
        <#if (valueMultiCore?has_content) == false>
            <#assign unit = ""/>              
        </#if>        
        <@fdsDataItems.dataValues key=key value="${valueMultiCore} ${unit}"/>
    </#if>
</#macro>