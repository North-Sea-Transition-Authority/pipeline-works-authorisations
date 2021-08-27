<#include '../../layout.ftl'>
<#import 'consentSearchTopLevelView.ftl' as consentSearchTopLevelView>

<#assign consentApplicationDto = consentFileView.pwaConsentApplicationDto />

<@defaultPage htmlTitle="${consentApplicationDto.consentReference} documents" pageHeading="${consentApplicationDto.consentReference}" caption="Documents" breadcrumbs=true fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <#assign documentIsDownloadable = consentApplicationDto.consentDocumentDownloadable() />
    <#assign documentStatusDisplay = consentApplicationDto.getDocStatusDisplay() />

    <@consentSearchTopLevelView.topLevelData consentSearchResultView/>

    <@grid.gridRow>
        <@grid.twoThirdsColumn>

            <h2 class="govuk-heading-m">Consent document</h2>
            <@fdsCheckAnswers.checkAnswers>
                <#if consentApplicationDto.pwaApplicationId?has_content && documentIsDownloadable>
                    <@fdsAction.link linkText="Download" linkUrl=springUrl(urlFactory.getConsentDocumentUrl(consentApplicationDto.consentId, consentApplicationDto.docgenRunId.get())) />
                </#if>
            </@fdsCheckAnswers.checkAnswers>

            <#if consentFileView.consultationRequestView?has_content>
                <#assign consultationRequestViewData = consentFileView.consultationRequestView />
                <#if consultationRequestViewData.consultationResponseFileViews?has_content>
                    <h2 class="govuk-heading-m">Secretary of State decision files</h2>
                    <@pwaFiles.uploadedFileList downloadUrl=springUrl(consultationRequestViewData.downloadFileUrl) existingFiles=consultationRequestViewData.consultationResponseFileViews />
                </#if>
            </#if>

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>