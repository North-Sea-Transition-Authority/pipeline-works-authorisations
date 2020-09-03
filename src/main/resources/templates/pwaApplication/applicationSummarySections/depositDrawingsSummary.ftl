<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="depositDrawingViews" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="depositDrawingDetails">${sectionDisplayText}</h2>

    <@depositDrawingDetails depositDrawingViews/>

</div>


<#macro depositDrawingDetails depositDrawingViews>

    <#if depositDrawingViews?has_content>
        <@fdsCheckAnswers.checkAnswers>
            <#list depositDrawingViews as depositDrawingView>

                <@fdsCheckAnswers.checkAnswersRow keyText=depositDrawingView.reference actionUrl="" screenReaderActionText="" actionText="">                
                    <ul class="govuk-list">
                        <#list (depositDrawingView.depositReferences)?sort as depositReference>
                            <li> ${depositReference} </li>
                        </#list>
                    </ul>
                </@fdsCheckAnswers.checkAnswersRow>

            </#list>
        </@fdsCheckAnswers.checkAnswers>

    <#else>
        <@fdsInsetText.insetText>
            No permanent deposit drawings have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
    

</#macro>


