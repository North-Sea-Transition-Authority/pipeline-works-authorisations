<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="huooRolePipelineGroupsPadView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="huooDetails">${sectionDisplayText}</h2>

    <@huooDetails orgRolePipelineGroups=huooRolePipelineGroupsPadView.holderOrgRolePipelineGroups  role="Holders" />
    <@huooDetails orgRolePipelineGroups=huooRolePipelineGroupsPadView.userOrgRolePipelineGroups  role="Users" />
    <@huooDetails orgRolePipelineGroups=huooRolePipelineGroupsPadView.operatorOrgRolePipelineGroups  role="Operators" />
    <@huooDetails orgRolePipelineGroups=huooRolePipelineGroupsPadView.ownerOrgRolePipelineGroups  role="Owners" />
</div>


<#macro huooDetails orgRolePipelineGroups role>

    <h3 class="govuk-heading-m"> ${role} </h3>
    <#list orgRolePipelineGroups as orgRolePipelineGroup>
        
        <#if orgRolePipelineGroup.huooType == "PORTAL_ORG">
            <#if orgRolePipelineGroup.isManuallyEnteredName>
                <h4 class="govuk-heading-s">  ${orgRolePipelineGroup.manuallyEnteredName!}  </h4>
            <#else>
                <h4 class="govuk-heading-s">  ${orgRolePipelineGroup.getCompanyName()!}  </h4>
            </#if>
        <#else>
            <h4 class="govuk-heading-s">  ${orgRolePipelineGroup.treatyAgreement.getCountry()} </h4>
        </#if>
        
        <@fdsCheckAnswers.checkAnswers>

            <#if orgRolePipelineGroup.huooType == "PORTAL_ORG">
                <@fdsCheckAnswers.checkAnswersRow keyText="Company number" actionUrl="" screenReaderActionText="" actionText="">
                    ${orgRolePipelineGroup.getRegisteredNumber()!}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="Legal entity address" actionUrl="" screenReaderActionText="" actionText="">
                    ${orgRolePipelineGroup.getCompanyAddress()!}
                </@fdsCheckAnswers.checkAnswersRow>
            <#else>
                <@fdsCheckAnswers.checkAnswersRow keyText="Treaty agreement text" actionUrl="" screenReaderActionText="" actionText="">
                    ${orgRolePipelineGroup.treatyAgreement.getAgreementText()!}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

            <@fdsCheckAnswers.checkAnswersRow keyText="Pipelines" actionUrl="" screenReaderActionText="" actionText="">
                <#list orgRolePipelineGroup.pipelineNumbersAndSplits as pipeline>
                    <#assign splitInfo = "" />
                    <#if pipeline.splitInfo?has_content> 
                        <#assign splitInfo = '[' + pipeline.splitInfo + ']' />
                    </#if>
                    <ul class="govuk-list">
                        <li> ${pipeline.pipelineNumber!} ${splitInfo} </li>
                    </ul>
                </#list>
            </@fdsCheckAnswers.checkAnswersRow>

            


        </@fdsCheckAnswers.checkAnswers>
    </#list>

    <#if orgRolePipelineGroups?has_content == false>
        <@fdsInsetText.insetText>
            No ${role?lower_case} have been added to this application.
        </@fdsInsetText.insetText>
    </#if>




</#macro>


