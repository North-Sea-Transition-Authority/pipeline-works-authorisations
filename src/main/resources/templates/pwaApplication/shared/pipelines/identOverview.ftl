<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.ConnectedPipelineIdentSummaryView" -->
<#-- @ftlvariable name="lastConnectedPipelineIdentView" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.ConnectedPipelineIdentsView" -->
<#-- @ftlvariable name="identUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.IdentUrlFactory" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType" -->
<#-- @ftlvariable name="identSummaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->


<@defaultPage htmlTitle="${pipelineOverview.getPipelineName()} idents" breadcrumbs=true fullWidthColumn=true>


    <@validationResult.singleErrorSummary summaryValidationResult=identSummaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=identSummaryValidationResult! />

    <h1 class="govuk-heading-xl">${pipelineOverview.getPipelineName()} idents</h1>

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

    <@fdsDetails.summaryDetails summaryTitle="What is an ident and what do I need to provide?">
        <p>Each component part of a pipeline has a unique identifier known as an ident. This represents a specific component or section of a pipeline. Each main component should be added as a separate ident.</p>
        <p>Main component parts such as ESDV, Manifolds, SSIV, Termination Units or component(s) that affect flow should have their own ident with the from, to and description defined as that component. This only applies to the main production or umbilical pipeline.</p>
        <p>All idents are to follow the direction of flow.</p>
    </@fdsDetails.summaryDetails>
    <#if summaryView?has_content && summaryView.connectedPipelineIdents?has_content>
        <@fdsCheckAnswers.checkAnswers>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Total idents length">
                ${summaryView.totalIdentLength}
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </@fdsCheckAnswers.checkAnswers>
        <@fdsTimeline.timeline>
            <@fdsTimeline.timelineSection sectionHeading="">
                <#assign pastFirstIteration = false/>
                <#list summaryView.connectedPipelineIdents as connectedPipelineIdentView>
                    <#if pastFirstIteration == true && lastConnectedPipelineIdentView?has_content>
                        <@fdsTimeline.timelineTimeStamp timeStampHeadingSize="h2" timeStampHeading="${lastConnectedPipelineIdentView.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
                    </#if>
                    <#list connectedPipelineIdentView.identViews as identView>
                        <#assign timelineAction>
                            <@fdsAction.link linkText="Insert ident above" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getInsertAboveUrl(identView.identId)) linkScreenReaderText="Insert ident above" />
                            <@fdsAction.link linkText="Edit ident" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getEditUrl(identView.identId)) linkScreenReaderText="Edit ident ${identView.identNumber}" />
                            <@fdsAction.link linkText="Remove ident" linkClass="govuk-link" linkUrl=springUrl(identUrlFactory.getRemoveUrl(identView.identId)) linkScreenReaderText="Remove ident ${identView.identNumber}" />
                        </#assign>
                        <@fdsTimeline.timelineTimeStamp timeStampHeadingSize="h2" timeStampHeading=identView.fromLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp" timelineActionContent=timelineAction>
                            <#if identSummaryValidationResult?has_content>
                                <#list identSummaryValidationResult.errorItems as errorItem>
                                    <#assign subIdPrefix = errorItem?index?c/>
                                    <#assign sectionId = validationResult.constructObjectId(identSummaryValidationResult!, subIdPrefix + identView.identId) />
                                    <#assign hasErrors = validationResult.hasErrors(identSummaryValidationResult!, sectionId) />
                                    <#assign sectionErrorMessage = validationResult.errorMessageOrEmptyString(identSummaryValidationResult!, sectionId) />
                                    <#if sectionErrorMessage?has_content>
                                        <span id=${sectionId} class="govuk-error-message">
                                            <span class="govuk-visually-hidden">Error:</span> ${sectionErrorMessage}<br/>
                                        </span>
                                    </#if>
                                </#list>
                            </#if>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.identNumber}" value="Ident number"/>
                                <#assign lengthDisplay = identView.length?has_content?then(identView.length + "m", "")/>
                                <@fdsDataItems.dataValues key="Length" value=lengthDisplay/>
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
                                <@fdsDataItems.dataValues key="Is this ident defining a structure?" value="${identView.definingStructure?then('Yes', 'No')}"/>
                                <@dataValueForCoreType coreType=coreType key="External diameter" valueSingleCore=(identView.externalDiameter)! valueMultiCore=(identView.externalDiameterMultiCore)! measurementUnit="mm"/>
                                <@dataValueForCoreType coreType=coreType key="Internal diameter" valueSingleCore=(identView.internalDiameter)! valueMultiCore=(identView.internalDiameterMultiCore)! measurementUnit="mm"/>
                                <@dataValueForCoreType coreType=coreType key="Wall thickness" valueSingleCore=(identView.wallThickness)! valueMultiCore=(identView.wallThicknessMultiCore)! measurementUnit="mm"/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@dataValueForCoreType coreType=coreType key="MAOP" valueSingleCore=(identView.maop)! valueMultiCore=(identView.maopMultiCore)! measurementUnit="barg"/>
                                <@dataValueForCoreType coreType=coreType key="Insulation / coating type" valueSingleCore=(identView.insulationCoatingType)! valueMultiCore=(identView.insulationCoatingTypeMultiCore)!/>
                                <@dataValueForCoreType coreType=coreType key="Products to be conveyed" valueSingleCore=(identView.productsToBeConveyed)! valueMultiCore=(identView.productsToBeConveyedMultiCore)!/>
                            </@fdsDataItems.dataItem>
                            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                                <@dataValueForCoreType coreType=coreType key="Description of component part" valueSingleCore=(identView.componentPartsDescription)! valueMultiCore=(identView.componentPartsDescription)!/>
                            </@fdsDataItems.dataItem>
                        </@fdsTimeline.timelineTimeStamp>
                    </#list>
                    <#assign lastConnectedPipelineIdentView = connectedPipelineIdentView/>
                    <#assign pastFirstIteration = true/>
                </#list>
                <@fdsTimeline.timelineTimeStamp timeStampHeadingSize="h2" timeStampHeading="${lastConnectedPipelineIdentView.endIdent.toLocation}" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
            </@fdsTimeline.timelineSection>
        </@fdsTimeline.timeline>
        <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />
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