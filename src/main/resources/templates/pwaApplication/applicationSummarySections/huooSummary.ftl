<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="diffedAllOrgRolePipelineGroups" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="huooDetails">${sectionDisplayText}</h2>

    <@huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.holderOrgRolePipelineGroups  role="Holders" />
    <@huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.userOrgRolePipelineGroups  role="Users" />
    <@huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.operatorOrgRolePipelineGroups  role="Operators" />
    <@huooDetails diffedHuoos=diffedAllOrgRolePipelineGroups.ownerOrgRolePipelineGroups  role="Owners" />

</div>


<#macro huooDetails diffedHuoos role>

    <h3 class="govuk-heading-m"> ${role} </h3>
    
    <#list diffedHuoos as diffedHuoo>

        <#assign diffHideGroup = "hide-when-diff-disabled"/>
        <#assign isRemovedOrg = diffedHuoo.DiffableOrgRolePipelineGroup_roleOwnerName.diffType == "DELETED"/>

        <h4 class="govuk-heading-s ${isRemovedOrg?then(diffHideGroup, '') }">
            <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_roleOwnerName/>
        </h4>
        
        <@fdsCheckAnswers.checkAnswers>
            <#assign hasCompanyData = diffedHuoo.DiffableOrgRolePipelineGroup_hasCompanyData.currentValue?upper_case == "YES"/>

            <#if hasCompanyData>
                <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow keyText="Company number" actionUrl="" screenReaderActionText="" actionText="" rowClass=(hasCompanyData == false)?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_companyNumber/>
                </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>

                <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow keyText="Legal entity address" actionUrl="" screenReaderActionText="" actionText="" rowClass=(hasCompanyData == false)?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_companyAddress/>
                </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>

            <#elseif diffedHuoo.DiffableOrgRolePipelineGroup_isManuallyEnteredName.currentValue?upper_case == "NO">
                <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow keyText="Treaty agreement text" actionUrl="" screenReaderActionText="" actionText="" rowClass=hasCompanyData?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_treatyAgreementText/>
                </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>
            </#if>


            <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow keyText="Pipelines" actionUrl="" screenReaderActionText="" actionText="" rowClass=isRemovedOrg?then(diffHideGroup, "")>
                <ul class="govuk-list">
                    <#list diffedHuoo.DiffableOrgRolePipelineGroup_pipelineAndSplitsList as diffedPipelineNumber>
                        <li><@diffChanges.renderDiff diffedField=diffedPipelineNumber /></li>
                    </#list>
                </ul>
            </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

    </#list>



    <#if diffedHuoos?has_content == false>
        <@fdsInsetText.insetText>
            No ${role?lower_case} have been added to this application.
        </@fdsInsetText.insetText>
    </#if>




</#macro>






