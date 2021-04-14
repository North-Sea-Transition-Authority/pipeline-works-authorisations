<#include '../../pwaLayoutImports.ftl'>
<#include 'appSummaryUtils.ftl'>


<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="locationDetailsView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.LocationDetailsView" -->
<#-- @ftlvariable name="locationDetailsUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.location.LocationDetailsUrlFactory" -->
<#-- @ftlvariable name="requiredQuestions" type="java.util.Set<uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="locationDetails">${sectionDisplayText}</h2>

    <@locationDetails locationDetailsView/>

</div>


<#macro locationDetails locationDetailsView>

    <#local multiLineTextBlockClass = "govuk-summary-list" />

    <@fdsCheckAnswers.checkAnswers>

        <#if requiredQuestions?seq_contains("APPROXIMATE_PROJECT_LOCATION_FROM_SHORE")>
                <@fdsCheckAnswers.checkAnswersRow keyText="Approximate project location from shore" actionUrl="" screenReaderActionText="" actionText="">
                ${locationDetailsView.approximateProjectLocationFromShore!}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if requiredQuestions?seq_contains("WITHIN_SAFETY_ZONE")>
            <@fdsCheckAnswers.checkAnswersRow keyText="Will work be carried out within a HSE recognised 500m safety zone?" actionUrl="" screenReaderActionText="" actionText="">
                <#if locationDetailsView.withinSafetyZone?has_content>
                    ${locationDetailsView.withinSafetyZone.getDisplayText()}
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>
        

            <#if locationDetailsView.withinSafetyZone?has_content && locationDetailsView.withinSafetyZone == "YES">
                <@fdsCheckAnswers.checkAnswersRow keyText="Which structures are within 500m?" actionUrl="" screenReaderActionText="" actionText="">
                    <ul class="govuk-list">
                        <#list locationDetailsView.facilitiesIfYes as facility>
                            <li> ${facility} </li>
                        </#list>
                    </ul>
                </@fdsCheckAnswers.checkAnswersRow>

                <@psrQuestion locationDetailsView.psrNotificationSubmissionDate locationDetailsView.psrNotificationSubmitted/>
            </#if>

            <#if locationDetailsView.withinSafetyZone?has_content && locationDetailsView.withinSafetyZone == "PARTIALLY">
                <@fdsCheckAnswers.checkAnswersRow keyText="Which structures are within 500m?" actionUrl="" screenReaderActionText="" actionText="">
                    <ul class="govuk-list">
                        <#list locationDetailsView.facilitiesIfPartially as facility>
                            <li> ${facility} </li>
                        </#list>
                    </ul>
                </@fdsCheckAnswers.checkAnswersRow>

                <@psrQuestion locationDetailsView.psrNotificationSubmissionDate locationDetailsView.psrNotificationSubmitted/>
            </#if>
        </#if>

        <#if requiredQuestions?seq_contains("FACILITIES_OFFSHORE")>
            <@fdsCheckAnswers.checkAnswersRow keyText="Are all facilities wholly offshore and subsea?" actionUrl="" screenReaderActionText="" actionText="">
                <#if locationDetailsView.facilitiesOffshore?has_content>
                    <@showYesNoForBool locationDetailsView.facilitiesOffshore/>
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>

            <#if locationDetailsView.facilitiesOffshore?has_content && locationDetailsView.facilitiesOffshore == false>
                <@fdsCheckAnswers.checkAnswersRow keyText="Where do the pipelines come ashore?" actionUrl="" screenReaderActionText="" actionText="">
                    <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${locationDetailsView.pipelineAshoreLocation!} </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>
        </#if>

        <#if requiredQuestions?seq_contains("TRANSPORTS_MATERIALS_TO_SHORE")>
            <@fdsCheckAnswers.checkAnswersRow keyText="Will the pipeline(s) be used to transport products / facilitate the transportation of products to shore?" actionUrl="" screenReaderActionText="" actionText="">
                <#if locationDetailsView.transportsMaterialsToShore?has_content>
                    <@showYesNoForBool locationDetailsView.transportsMaterialsToShore/>
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>

            <#if locationDetailsView.transportsMaterialsToShore?has_content && locationDetailsView.transportsMaterialsToShore>
                <@fdsCheckAnswers.checkAnswersRow keyText="Method of transportation to shore" actionUrl="" screenReaderActionText="" actionText="">
                    <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${locationDetailsView.transportationMethod!} </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>
        </#if>

        <#if requiredQuestions?seq_contains("ROUTE_SURVEY_UNDERTAKEN")>            
            <@fdsCheckAnswers.checkAnswersRow keyText="Has a pipeline route survey been undertaken?" actionUrl="" screenReaderActionText="" actionText="">
                <#if locationDetailsView.routeSurveyUndertaken?has_content>
                    <@showYesNoForBool locationDetailsView.routeSurveyUndertaken/>
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>

            <#if locationDetailsView.routeSurveyUndertaken?has_content && locationDetailsView.routeSurveyUndertaken>
                <@fdsCheckAnswers.checkAnswersRow keyText="When was the pipeline route survey concluded?" actionUrl="" screenReaderActionText="" actionText="">
                    ${locationDetailsView.surveyConcludedDate!}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline route details" actionUrl="" screenReaderActionText="" actionText="">
                    <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${locationDetailsView.pipelineRouteDetails!} </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>

            <#elseif locationDetailsView.routeSurveyUndertaken?has_content && !locationDetailsView.routeSurveyUndertaken>
                <@fdsCheckAnswers.checkAnswersRow keyText="Why has a pipeline route survey not been undertaken?" actionUrl="" screenReaderActionText="" actionText="">
                    <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${locationDetailsView.routeSurveyNotUndertakenReason!} </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>
        </#if>

        <#if requiredQuestions?seq_contains("WITHIN_LIMITS_OF_DEVIATION")>
            <@fdsCheckAnswers.checkAnswersRow keyText="Confirmation that the limit of deviation during construction will be ±100m" actionUrl="" screenReaderActionText="" actionText="">
                <#if locationDetailsView.withinLimitsOfDeviation?has_content>
                    <@showConfirmedForBool locationDetailsView.withinLimitsOfDeviation/>
                <#else>
                    Not provided
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


    </@fdsCheckAnswers.checkAnswers>

    <#if requiredQuestions?seq_contains("ROUTE_DOCUMENTS")>            
        <#if locationDetailsView.uploadedLetterFileViews?has_content>
            <h3 class="govuk-heading-m"> Pipeline route documents </h3>
            <@pwaFiles.uploadedFileList downloadUrl=springUrl(locationDetailsUrlFactory.getDocumentDownloadUrl()) existingFiles=locationDetailsView.uploadedLetterFileViews />
        <#else>
            <@fdsInsetText.insetText>
            No pipeline route documents have been added to this application.
            </@fdsInsetText.insetText>
        </#if>
    </#if>


</#macro>



<#macro psrQuestion notificationSubmissionDate notificationSubmitted=[]>
   <@fdsCheckAnswers.checkAnswersRow keyText="Have you submitted a Pipelines Safety Regulations notification to HSE?" actionUrl="" screenReaderActionText="" actionText="">
        <#if locationDetailsView.psrNotificationSubmitted?has_content>
            <@showYesNoForBool notificationSubmitted/>
        </#if>                    
    </@fdsCheckAnswers.checkAnswersRow>

    <#if notificationSubmitted?has_content && notificationSubmitted>
        <@fdsCheckAnswers.checkAnswersRow keyText="Date submitted" actionUrl="" screenReaderActionText="" actionText="">
            ${notificationSubmissionDate}
        </@fdsCheckAnswers.checkAnswersRow>

    <#elseif notificationSubmitted?has_content && !notificationSubmitted>
        <@fdsCheckAnswers.checkAnswersRow keyText="Expected submission date" actionUrl="" screenReaderActionText="" actionText="">
            ${notificationSubmissionDate}
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>
</#macro>

