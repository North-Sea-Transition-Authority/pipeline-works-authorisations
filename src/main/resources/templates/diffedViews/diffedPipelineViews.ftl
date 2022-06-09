<#include '../pwaLayoutImports.ftl'>

<#macro pipelineHeaderDetails pipelineHeader pipelineIdentsSize drawingSummaryView=[] urlFactory=[] consentedModel=false>

    <@fdsCheckAnswers.checkAnswers>

        <#if !consentedModel>
            <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline name" actionUrl="" screenReaderActionText="" actionText="">
               <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_pipelineName />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if pipelineHeader.hasTemporaryPipelineNumber>
            <@fdsCheckAnswers.checkAnswersRow keyText="Reference used in drawing" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_temporaryPipelineNumber />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


        <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline status" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_pipelineStatusDisplayStr />
        </@fdsCheckAnswers.checkAnswersRow>

        <#if pipelineHeader.headerQuestions?seq_contains("OUT_OF_USE_ON_SEABED_REASON")>
            <@fdsCheckAnswers.checkAnswersRow keyText="Why is the pipeline not being returned to shore?" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_pipelineStatusReason />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <@fdsCheckAnswers.checkAnswersRow keyText="Number of idents" actionUrl="" screenReaderActionText="" actionText="">
             ${pipelineIdentsSize}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Length (${unitMeasurements.METRE.suffixDisplay})" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_length />
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="From location" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_fromLocation />
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="From (WGS 84)" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_fromCoordinates multiLineTextBlockClass="govuk-summary-list"/>
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="To location" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_toLocation />          
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="To (WGS 84)" actionUrl="" screenReaderActionText="" actionText="">
            <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_toCoordinates multiLineTextBlockClass="govuk-summary-list" />
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Component parts" actionUrl="" screenReaderActionText="" actionText="">
             <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_componentParts multiLineTextBlockClass="govuk-summary-list" />
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Products to be conveyed" actionUrl="" screenReaderActionText="" actionText="">
             <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_productsToBeConveyed multiLineTextBlockClass="govuk-summary-list" />
        </@fdsCheckAnswers.checkAnswersRow>

        <#if !consentedModel>
            <@fdsCheckAnswers.checkAnswersRow keyText="Will be trenched and/or buried and/or backfilled?" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_trenchedBuriedBackfilled />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if pipelineHeader.PipelineHeaderView_trenchedBuriedBackfilled?has_content && pipelineHeader.PipelineHeaderView_trenchedBuriedBackfilled.currentValue?lower_case == "yes">
            <@fdsCheckAnswers.checkAnswersRow keyText="Method of trenching/burying/backfilling" actionUrl="" screenReaderActionText="" actionText="">
                 <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_trenchingMethodsDescription multiLineTextBlockClass="govuk-summary-list" />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if !consentedModel>
            <@fdsCheckAnswers.checkAnswersRow keyText="Flexible or rigid?" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_pipelineFlexibility />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if !consentedModel>
            <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline material" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_pipelineMaterial />
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if pipelineHeader.PipelineHeaderView_pipelineMaterial?has_content && pipelineHeader.PipelineHeaderView_pipelineMaterial.currentValue?lower_case == "other">
            <@fdsCheckAnswers.checkAnswersRow keyText="Other material used" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_otherPipelineMaterialUsed multiLineTextBlockClass="govuk-summary-list"/>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if pipelineHeader.headerQuestions?seq_contains("ALREADY_EXISTS_ON_SEABED")>

            <@fdsCheckAnswers.checkAnswersRow keyText="Does the pipeline already exist on the seabed?" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_alreadyExistsOnSeabed />
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Is the pipeline in use?" actionUrl="" screenReaderActionText="" actionText="">
                <@diffChanges.renderDiff pipelineHeader.PipelineHeaderView_pipelineInUse />
            </@fdsCheckAnswers.checkAnswersRow>

        </#if>

        <#if !consentedModel>
            <@fdsCheckAnswers.checkAnswersRow keyText="Schematic drawing" actionUrl="" screenReaderActionText="" actionText="">
                <#if drawingSummaryView?has_content>
                    <@fdsAction.link linkText=drawingSummaryView.fileName linkUrl=springUrl(urlFactory.getPipelineDrawingDownloadUrl(drawingSummaryView.fileId))
                        linkClass="govuk-link" linkScreenReaderText="Download ${drawingSummaryView.fileName}" role=false start=false openInNewTab=true/>
                <#else>
                    No drawing uploaded
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if pipelineHeader.canShowFootnote>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Special features">
                <@diffChanges.renderDiff diffedField=pipelineHeader.PipelineHeaderView_footnote multiLineTextBlockClass="govuk-summary-list"/>
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </#if>  

    </@fdsCheckAnswers.checkAnswers>




</#macro>




<#macro identViewTimelinePoint identView>
    <#-- Detect if ident is completely removed and hide on page load -->
    <#if identView.IdentDiffableView_fromLocation.diffType == "DELETED">
        <#local diffHideGroup = "hide-when-diff-disabled"/>
    </#if>

    <#-- interpret diffed Boolean -->
    <#local connectedToNext = identView.IdentDiffableView_connectedToNext.currentValue=='Yes'/>

    <#-- Put all diffed values into so that the diff macro output can be given to fds macros expecting variables, not nested content -->
    <#local fromLocation><@diffChanges.renderDiff identView.IdentDiffableView_fromLocation /></#local>
    <#local toLocation><@diffChanges.renderDiff identView.IdentDiffableView_toLocation /></#local>

    <#local identNumber><@diffChanges.renderDiff identView.IdentDiffableView_identNumber /></#local>
    <#local definingStructure><@diffChanges.renderDiff identView.IdentDiffableView_definingStructure /></#local>
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

    <@fdsTimeline.timelineTimeStamp timeStampHeading=fromLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp ${diffHideGroup!}" >

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValuesNumber smallNumber=true key="${identNumber}" value="Ident number"/>
            <@fdsDataItems.dataValues key="Length (${unitMeasurements.METRE.suffixDisplay})" value="${length!}"/>
            <@fdsDataItems.dataValues key="From (WGS 84)" value="${fromCoordinates}"/>
            <@fdsDataItems.dataValues key="To (WGS 84)" value="${toCoordinates}"/>
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Is this ident defining a structure?" value="${definingStructure!}"/>
            <@fdsDataItems.dataValues key="External diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${externalDiameter}" />
            <@fdsDataItems.dataValues key="Internal diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${internalDiameter}"/>
            <@fdsDataItems.dataValues key="Wall thickness (${unitMeasurements.MILLIMETRE.suffixDisplay})" value="${wallThickness}" />
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="MAOP (${unitMeasurements.BAR_G.suffixDisplay})" value="${maop}" />
            <@fdsDataItems.dataValues key="Insulation / coating type" value="${insulationCoatingType}" />
            <@fdsDataItems.dataValues key="Products to be conveyed" value="${productsToBeConveyed}" />
        </@fdsDataItems.dataItem>
        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Description of component parts" value="${componentPartsDescription}"/>
        </@fdsDataItems.dataItem>

    </@fdsTimeline.timelineTimeStamp>

    <#if !connectedToNext>
        <@fdsTimeline.timelineTimeStamp timeStampHeading=toLocation nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border ${diffHideGroup!}"/>
    </#if>

</#macro>