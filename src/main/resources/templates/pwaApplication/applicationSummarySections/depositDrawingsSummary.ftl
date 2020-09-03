<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="depositDrawingViews" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="depositDrawingDetails">${sectionDisplayText}</h2>

    <@depositDrawingDetails depositDrawingViews/>

</div>


<#macro depositDrawingDetails depositDrawingViews>

    <#if depositDrawingViews?has_content>    

        <#list depositDrawingViews as depositDrawingView>
            <h3 class="govuk-heading-m">${depositDrawingView.reference}</h3>

            <@fdsCheckAnswers.checkAnswers>      

                <@fdsCheckAnswers.checkAnswersRow keyText="Deposit drawing" actionUrl="" screenReaderActionText="" actionText="">                
                    ${depositDrawingView.fileName}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="File description" actionUrl="" screenReaderActionText="" actionText="">                
                    ${depositDrawingView.documentDescription}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="Deposits on drawing" actionUrl="" screenReaderActionText="" actionText="">                
                    <ul class="govuk-list">
                        <#list (depositDrawingView.depositReferences)?sort as depositReference>
                            <li> ${depositReference} </li>
                        </#list>
                    </ul>
                </@fdsCheckAnswers.checkAnswersRow>

            </@fdsCheckAnswers.checkAnswers>

        </#list>
        

    <#else>
        <@fdsInsetText.insetText>
            No permanent deposit drawings have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
    

</#macro>


