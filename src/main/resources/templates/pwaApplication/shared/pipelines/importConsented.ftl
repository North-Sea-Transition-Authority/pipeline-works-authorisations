<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType" -->

<@defaultPage htmlTitle="Modify consented pipeline" pageHeading="Modify consented pipeline" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.pipelineId" options=consentedPipelines labelText="Which pipeline is being modified?"/>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Modify consented pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>
