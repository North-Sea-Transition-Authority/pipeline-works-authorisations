<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>
<#import 'pipeline/pipelineCrossingManagement.ftl' as pipelineCrossingManagement>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="blockCrossingDocumentsUrl" type="java.lang.String" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->



<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true fullWidthColumn=true>

    <@blockCrossingManagement.blockCrossingManagement
    blockCrossings=blockCrossings
    blockCrossingFileViews=blockCrossingFiles
    urlFactory=blockCrossingUrlFactory
    isCompleted=crossingAgreementValidationResult.isSectionValid("BLOCK_CROSSINGS") />

    <hr class="govuk-section-break govuk-section-break--l"/>

    <@cableCrossingManagement.cableCrossingManagement
    cableCrossingViews=cableCrossings
    cableCrossingFileViews=cableCrossingFiles
    urlFactory=cableCrossingUrlFactory
    isCompleted=crossingAgreementValidationResult.isSectionValid("CABLE_CROSSINGS") />

    <hr class="govuk-section-break govuk-section-break--l"/>

    <@pipelineCrossingManagement.pipelineCrossingManagement
    urlFactory=pipelineCrossingUrlFactory
    pipelineCrossingFileViews=pipelineCrossingFiles
    pipelineCrossings=pipelineCrossings
    isCompleted=false/>

    <hr class="govuk-section-break govuk-section-break--l"/>

    <@medianLineCrossingManagement.medianLineCrossingManagement
    urlFactory=medianLineUrlFactory
    medianLineAgreementView=medianLineAgreementView!""
    medianLineFileViews=medianLineFiles />


</@defaultPage>