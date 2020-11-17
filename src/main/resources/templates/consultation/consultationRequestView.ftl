<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="consultationRequestViewData" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->
<#-- @ftlvariable name="consultationsUrlFactory" type="uk.co.ogauthority.pwa.service.consultations.ConsultationsUrlFactory" -->


<#macro consultationRequestView consultationRequestViewData displayAsHistoricalRequest=false>

    <#if displayAsHistoricalRequest>
        <h3 class="govuk-heading-m"> Requested ${consultationRequestViewData.requestDateDisplay} </h3>
    <#else>
        <h3 class="govuk-heading-m"> ${consultationRequestViewData.consulteeGroupName} </h3>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">
        <#nested/>
        <#if !displayAsHistoricalRequest>
            <@fdsCheckAnswers.checkAnswersRow keyText="Requested" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.requestDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
            ${consultationRequestViewData.status.getDisplayName()}
        </@fdsCheckAnswers.checkAnswersRow>

        <#if consultationRequestViewData.status == "RESPONDED">
            <@fdsCheckAnswers.checkAnswersRow keyText=consultationRequestViewData.status.getDisplayName() actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        <#elseif consultationRequestViewData.status == "WITHDRAWN">
            <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.withdrawnByUser}
            </@fdsCheckAnswers.checkAnswersRow>
            <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn on" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.endTimeStamp}
            </@fdsCheckAnswers.checkAnswersRow>
        <#else>
            <@fdsCheckAnswers.checkAnswersRow keyText="Response due" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.dueDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


        <#if consultationRequestViewData.responseType?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseType.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if consultationRequestViewData.responseConfirmReason?has_content>
          <@fdsCheckAnswers.checkAnswersRow keyText="Confirm conditions" actionText="" actionUrl="" screenReaderActionText="">
            ${consultationRequestViewData.responseConfirmReason}
          </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if consultationRequestViewData.responseRejectionReason?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Rejection reason" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseRejectionReason}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


        <#if consultationRequestViewData.responseByPerson?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseByPerson}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


    </@fdsCheckAnswers.checkAnswers>


</#macro>

