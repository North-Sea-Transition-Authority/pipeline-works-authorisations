<#include '../pwaLayoutImports.ftl'>



<#macro huooDetails diffedHuoos role showNoRolesWarning=true pipelinesDataRowKey="Pipelines">

    <h3 class="govuk-heading-m"> ${role} </h3>
    
    <#list diffedHuoos as diffedHuoo>

        <#assign diffHideGroup = "hide-when-diff-disabled"/>
        <#assign isRemovedOrg = diffedHuoo.DiffableOrgRolePipelineGroup_roleOwnerName.diffType == "DELETED"/>

        <h4 class="govuk-heading-s ${isRemovedOrg?then(diffHideGroup, '') }">
            <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_roleOwnerName/>
        </h4>
        
        <@fdsCheckAnswers.checkAnswers summaryListClass=isRemovedOrg?then(diffHideGroup, "")>
            <#assign hasCompanyData = diffedHuoo.DiffableOrgRolePipelineGroup_hasCompanyData.currentValue?upper_case == "YES"/>

            <#if hasCompanyData>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Company number" rowClass=(hasCompanyData == false)?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_companyNumber/>
                </@fdsCheckAnswers.checkAnswersRowNoAction>

                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Legal entity address" rowClass=(hasCompanyData == false)?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_companyAddress/>
                </@fdsCheckAnswers.checkAnswersRowNoAction>

            <#elseif diffedHuoo.DiffableOrgRolePipelineGroup_isManuallyEnteredName.currentValue?upper_case == "NO">
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Treaty agreement text" rowClass=hasCompanyData?then(diffHideGroup, "")>
                    <@diffChanges.renderDiff diffedField=diffedHuoo.DiffableOrgRolePipelineGroup_treatyAgreementText/>
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>


            <@fdsCheckAnswers.checkAnswersRowNoAction keyText=pipelinesDataRowKey rowClass=isRemovedOrg?then(diffHideGroup, "")>
                <ul class="govuk-list">
                    <#list diffedHuoo.DiffableOrgRolePipelineGroup_pipelineAndSplitsList as diffedPipelineNumber>
                        <li><@diffChanges.renderDiff diffedField=diffedPipelineNumber /></li>
                    </#list>
                </ul>
            </@fdsCheckAnswers.checkAnswersRowNoAction>

        </@fdsCheckAnswers.checkAnswers>

    </#list>



    <#if showNoRolesWarning && diffedHuoos?has_content == false>
        <@fdsInsetText.insetText>
            No ${role?lower_case} have been added to this application.
        </@fdsInsetText.insetText>
    </#if>




</#macro>