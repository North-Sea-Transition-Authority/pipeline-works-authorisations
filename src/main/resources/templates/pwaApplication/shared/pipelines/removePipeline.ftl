<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Remove pipeline" pageHeading="Are you sure you want to remove this pipeline?" breadcrumbs=true>

    <@fdsCheckAnswers.checkAnswers>
        <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline name" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.pipelineName}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Number of idents" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getNumberOfIdents()}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Length" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getLength()}m
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="From" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getFromLocation()}
          <br/>
            <@pwaCoordinate.display coordinatePair=pipeline.getFromCoordinates()! />
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="To" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getToLocation()}
          <br/>
            <@pwaCoordinate.display coordinatePair=pipeline.getToCoordinates()! />
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Component parts" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getComponentParts()!""}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Products to be conveyed" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.getProductsToBeConveyed()!""}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Will be trenched and/or buried and/or backfilled?" actionUrl="" screenReaderActionText="" actionText="">
            <#if pipeline.trenchedBuriedBackfilled?has_content>
                ${pipeline.trenchedBuriedBackfilled?string("Yes", "No")}
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>
        <#if pipeline.trenchedBuriedBackfilled?has_content && pipeline.trenchedBuriedBackfilled == true>
            <@fdsCheckAnswers.checkAnswersRow keyText="Method of trenching/burying/backfilling" actionUrl="" screenReaderActionText="" actionText="">
                ${pipeline.trenchingMethodsDescription!""}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>
        <@fdsCheckAnswers.checkAnswersRow keyText="Flexible or rigid?" actionUrl="" screenReaderActionText="" actionText="">
            <#if pipeline.pipelineFlexibility?has_content>
                ${pipeline.pipelineFlexibility.displayText}
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline material" actionUrl="" screenReaderActionText="" actionText="">
            <#if pipeline.pipelineMaterial?has_content>
                ${pipeline.pipelineMaterial.displayText}
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>
        <#if pipeline.pipelineMaterial?has_content && pipeline.pipelineMaterial == "OTHER">
            <@fdsCheckAnswers.checkAnswersRow keyText="Other material used" actionUrl="" screenReaderActionText="" actionText="">
                <#if pipeline.otherPipelineMaterialUsed?has_content>
                    ${pipeline.otherPipelineMaterialUsed}
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>
        <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline status" actionUrl="" screenReaderActionText="" actionText="">
            ${pipeline.pipelineStatus.displayText}
        </@fdsCheckAnswers.checkAnswersRow>
        <#if pipeline.pipelineStatus == "OUT_OF_USE_ON_SEABED">
            <@fdsCheckAnswers.checkAnswersRow keyText="Reason for leaving on seabed" actionUrl="" screenReaderActionText="" actionText="">
                ${pipeline.pipelineStatusReason}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>
    </@fdsCheckAnswers.checkAnswers>
    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>