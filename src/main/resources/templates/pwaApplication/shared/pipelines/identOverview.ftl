<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->

<@defaultPage htmlTitle="${pipelineOverview.pipelineNumber} idents" pageHeading="${pipelineOverview.pipelineNumber} idents" breadcrumbs=true fullWidthColumn=true>

    <@pipelineOverviewMacro.header pipeline=pipelineOverview />

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

    <#if groupedIdentViews?has_content>
        <@fdsTimeline.timeline>
            <@fdsTimeline.timelineSection sectionHeading="">
                <#assign pastFirstIteration = false/>
                <#list groupedIdentViews as groupedView>
                    <#if pastFirstIteration == true && lastGroup?has_content>
                        <#-- The lastGroup error can be ignored as it is defined further below, and will not be ran if empty. -->
                        <@fdsTimeline.timelineTimeStamp timeStampHeading="${lastGroup.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border">
                          <br/><br/>
                        </@fdsTimeline.timelineTimeStamp>
                    </#if>
                    <#list groupedView.identViews as identView>
                        <#assign timelineAction>
                            <@fdsAction.link linkText="Edit ident" linkClass="govuk-link" linkUrl=springUrl("#")/>
                            <@fdsAction.link linkText="Remove ident" linkClass="govuk-link" linkUrl=springUrl("#")/>
                        </#assign>
                        <@fdsTimeline.timelineTimeStamp timeStampHeading=identView.fromLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp" timelineActionContent=timelineAction>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.identNumber}" value="Ident number"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.length}m" value="Length"/>
                                <#assign from>
                                    <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
                                </#assign>
                                <#assign to>
                                    <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
                                </#assign>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key=from value="From"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key=to value="To"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.externalDiameter}mm" value="External diameter"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.internalDiameter}mm" value="Internal diameter"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.wallThickness}mm" value="Wall thickness"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.maop}barg" value="MAOP"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.insulationCoatingType}" value="Insulation coating type"/>
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.productsToBeConveyed}" value="Products to be conveyed"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.componentPartsDescription}" value="Component part description"/>
                            </@fdsDataItems.dataItem>
                        </@fdsTimeline.timelineTimeStamp>
                    </#list>
                    <#assign lastGroup = groupedView/>
                    <#assign pastFirstIteration = true/>
                </#list>
                <@fdsTimeline.timelineTimeStamp timeStampHeading="${lastGroup.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
            </@fdsTimeline.timelineSection>
        </@fdsTimeline.timeline>
    </#if>

</@defaultPage>