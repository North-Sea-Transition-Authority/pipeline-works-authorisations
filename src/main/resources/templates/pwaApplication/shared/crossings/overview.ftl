<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="blockCrossingDocumentsUrl" type="java.lang.String" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->



<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true>

    <@blockCrossingManagement.blockCrossingManagement
    blockCrossings=blockCrossings
    blockCrossingFileViews=blockCrossingFiles
    urlFactory=blockCrossingUrlFactory
    isCompleted=crossingAgreementValidationResult.isSectionValid("BLOCK_CROSSINGS") />

    <@medianLineCrossingManagement.medianLineCrossingManagement
    urlFactory=medianLineUrlFactory
    medianLineAgreementView=medianLineAgreementView!""
    medianLineFileViews=medianLineFiles />

</@defaultPage>