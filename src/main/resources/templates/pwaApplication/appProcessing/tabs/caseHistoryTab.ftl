<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseHistoryItems" type="java.util.List<uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView>" -->

<#macro tab caseHistoryItems>

  <@fdsTimeline.timeline>

      <@fdsTimeline.timelineSection sectionHeading="">

        <#list caseHistoryItems as item>

          <@fdsTimeline.timelineTimeStamp timeStampHeading=item.headerText nodeNumber=" " timeStampClass="fds-timeline__time-stamp" >

              <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                  <@fdsDataItems.dataValues key="Date and time" value=item.dateTimeDisplay />
                  <@fdsDataItems.dataValues key=item.personLabelText value=item.personName />
              </@fdsDataItems.dataItem>

              <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                  <#list item.dataItems as key, value>
                    <@fdsDataItems.dataValues key=key value=value />
                  </#list>
              </@fdsDataItems.dataItem>

          </@fdsTimeline.timelineTimeStamp>

        </#list>

      </@fdsTimeline.timelineSection>

  </@fdsTimeline.timeline>

</#macro>