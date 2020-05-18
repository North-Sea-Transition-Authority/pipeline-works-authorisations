<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentSummaryView" -->
<#-- @ftlvariable name="lastConnectedPipelineIdentView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentsView" -->

<@defaultPage htmlTitle="${pipelineOverview.pipelineNumber} idents" pageHeading="${pipelineOverview.pipelineNumber} idents" breadcrumbs=true fullWidthColumn=true>

    <@pipelineOverviewMacro.header pipeline=pipelineOverview />

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
                            <@fdsAction.link linkText="Edit ident" linkClass="govuk-link" linkUrl=springUrl("#") linkScreenReaderText="Edit ident ${identView.identNumber}" />
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
                                <@fdsDataItems.dataValues key="External diameter" value="${identView.externalDiameter}mm"/>
                                <@fdsDataItems.dataValues key="Internal diameter" value="${identView.internalDiameter}mm"/>
                                <@fdsDataItems.dataValues key="Wall thickness" value="${identView.wallThickness}mm"/>
                                <@fdsDataItems.dataValues key="MAOP" value="${identView.maop}barg"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValues key="Insulation / coating type" value="${identView.insulationCoatingType}"/>
                                <@fdsDataItems.dataValues key="Products to be conveyed" value="${identView.productsToBeConveyed}"/>
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

</@defaultPage>