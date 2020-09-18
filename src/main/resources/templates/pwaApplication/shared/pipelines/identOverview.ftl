<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentSummaryView" -->
<#-- @ftlvariable name="lastConnectedPipelineIdentView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConnectedPipelineIdentsView" -->
<#-- @ftlvariable name="identUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentUrlFactory" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType" -->

<@defaultPage htmlTitle="${pipelineOverview.getPipelineName()} idents" breadcrumbs=true fullWidthColumn=true  pageHeading="${pipelineOverview.getPipelineName()} idents">

    <@validationResult.singleErrorSummary summaryValidationResult=identSummaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=identSummaryValidationResult! />

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

        <@fdsDetails.summaryDetails summaryTitle="What is an ident and what do I need to provide?">
            <p>Each component part of a pipeline has a unique identifier known as an ident. This represents a specific component or section of a pipeline. Each main component should be added as a separate ident.</p>
            <p>Main component parts such as ESDV, Manifolds, SSIV, Termination Units or component(s) that affect flow should have their own ident with the from, to and description defined as that component. This only applies to the main production or umbilical pipeline.</p>
            <p>All idents are to follow the direction of flow.</p>
        </@fdsDetails.summaryDetails>
            

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
                            <#assign validationObjectId = validationResult.constructObjectId(identSummaryValidationResult!, identView.identNumber) />
                            <#assign errorMessage = validationResult.errorMessageOrEmptyString(identSummaryValidationResult!, validationObjectId) />
                            <#if errorMessage?has_content>
                              <p id="ident-${identView.identNumber}" class="govuk-error-message">${errorMessage}</p>
                            </#if>
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
                                <@dataValueForCoreType coreType=coreType key="Description of component parts" valueSingleCore=(identView.componentPartsDescription)! valueMultiCore=(identView.componentPartsDescription)!/>
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
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl) />
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
        <#local whiteSpacePreservedValue><@multiLineText.multiLineText blockClass="fds-data-items-list">${valueMultiCore}</@multiLineText.multiLineText></#local>
        <#local bracketedUnit = unit?has_content?then("(" + unit + ")", "")/>
        <@fdsDataItems.dataValues key="${key}${bracketedUnit}" value="${whiteSpacePreservedValue}"/>
    </#if>
</#macro>