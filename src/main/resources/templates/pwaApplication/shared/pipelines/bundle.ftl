<#include '../../../layout.ftl'>
<#import '../../../components/widgets/pipelineTableSelection.ftl' as pipelineTableSelection />

<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${screenActionType.actionText} pipeline bundle" pageHeading="${screenActionType.actionText} pipeline bundle" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.bundleName" labelText="What is the name of the bundle?" />

        <@fdsFieldset.fieldset legendHeading="Which pipelines are in this bundle?">
            <@pipelineTableSelection.pipelineTableSelection path="form.padPipelineIds" pipelineOverviews=pipelineOverviews />
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline bundle" secondaryLinkText="Back to pipelines" linkSecondaryActionUrl=springUrl(backUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>