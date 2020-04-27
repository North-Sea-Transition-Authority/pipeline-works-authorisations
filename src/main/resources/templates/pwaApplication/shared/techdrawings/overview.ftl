<#include '../../../layout.ftl'>
<#import 'admiraltyChartManagement.ftl' as admiraltyChartManagement>

<#-- @ftlvariable name="admiraltyChartUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory" -->
<#-- @ftlvariable name="admiraltyChartFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->

<@defaultPage htmlTitle="Technical drawings" pageHeading="Technical drawings" breadcrumbs=true fullWidthColumn=true>

    <@admiraltyChartManagement.admiraltyChartManagement
    urlFactory=admiraltyChartUrlFactory
    optionalSection=admiraltyOptional
    admiraltyChartFileViews=admiraltyChartFileViews />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons errorMessage=errorMessage!"" primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>