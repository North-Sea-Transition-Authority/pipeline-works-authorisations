<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="identView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentDiffableView" -->
<#-- @ftlvariable name="unitMeasurements" type="java.util.Map<java.lang.String, uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement>" -->

<#macro identViewTimelinePoint identView unitMeasurements>

    <#assign connectedToNext = identView.connectedToNext/>

    <@fdsTimeline.timelineTimeStamp timeStampHeading=identView.fromLocation!"" nodeNumber=" " timeStampClass="fds-timeline__time-stamp" >

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValuesNumber smallNumber=true key="${identView.identNumber!}" value="Ident number"/>
            <@fdsDataItems.dataValues key="Length (${unitMeasurements.METRE.suffixDisplay})" value="${identView.length!}"/>
            <#assign from>
                <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
            </#assign>
            <#assign to>
                <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
            </#assign>
            <@fdsDataItems.dataValues key="From (WGS 84)" value="${from!}"/>
            <@fdsDataItems.dataValues key="To (WGS 84)" value="${to!}"/>
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="External diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value=identView.externalDiameter!"" />
            <@fdsDataItems.dataValues key="Internal diameter (${unitMeasurements.MILLIMETRE.suffixDisplay})" value=identView.internalDiameter!""/>
            <@fdsDataItems.dataValues key="Wall thickness (${unitMeasurements.MILLIMETRE.suffixDisplay})" value=identView.wallThickness!"" />
            <@fdsDataItems.dataValues key="MAOP (${unitMeasurements.BAR_G.suffixDisplay})" value=identView.maop!"" />
        </@fdsDataItems.dataItem>

        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Insulation / coating type" value="${identView.insulationCoatingType!}" />
            <@fdsDataItems.dataValues key="Products to be conveyed" value="${identView.productsToBeConveyed!}" />
        </@fdsDataItems.dataItem>
        <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Description of component parts" value="${identView.componentPartsDescription!}"/>
        </@fdsDataItems.dataItem>

    </@fdsTimeline.timelineTimeStamp>

    <#if !connectedToNext>
        <@fdsTimeline.timelineTimeStamp timeStampHeading=identView.toLocation!"" nodeNumber=" " timeStampClass="fds-timeline__time-stamp--no-border"/>
    </#if>

</#macro>

