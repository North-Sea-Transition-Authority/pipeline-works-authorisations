<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="blockCrossingDocumentsUrl" type="java.lang.String" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->



<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true fullWidthColumn=true>

    <#if overview == "LICENCE_AND_BLOCKS">
        <@blockCrossingManagement.blockCrossingManagement
        blockCrossings=blockCrossings
        blockCrossingFileViews=blockCrossingFiles
        urlFactory=blockCrossingUrlFactory
        isCompleted=crossingAgreementValidationResult.isSectionValid("BLOCK_CROSSINGS") />
    <#elseif overview == "PIPELINE_CROSSINGS">

    <#elseif overview == "CABLE_CROSSINGS">
        <@cableCrossingManagement.cableCrossingManagement
        cableCrossingViews=cableCrossings
        cableCrossingFileViews=cableCrossingFiles
        urlFactory=cableCrossingUrlFactory
        isCompleted=crossingAgreementValidationResult.isSectionValid("CABLE_CROSSINGS") />
    <#elseif overview == "MEDIAN_LINE_CROSSING">
        <@medianLineCrossingManagement.medianLineCrossingManagement
        urlFactory=medianLineUrlFactory
        medianLineAgreementView=medianLineAgreementView!""
        medianLineFileViews=medianLineFiles />
    </#if>

</@defaultPage>