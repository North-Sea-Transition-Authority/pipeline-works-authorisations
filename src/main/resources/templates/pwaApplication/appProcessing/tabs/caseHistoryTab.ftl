<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseHistoryItems" type="java.util.List<uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView>" -->

<#macro tab caseHistoryItems>

  <@fdsTimeline.timeline timelineClass="fds-timeline--left-padding">

      <@fdsTimeline.timelineSection sectionHeading="">

        <#list caseHistoryItems as item>

          <#if item?counter == caseHistoryItems?size>
            <#local stampClass="fds-timeline__time-stamp--no-border" />
            <#else>
              <#local stampClass = "fds-timeline__time-stamp" />
          </#if>

          <@fdsTimeline.timelineTimeStamp timeStampHeading=item.headerText nodeNumber=" " timeStampClass=stampClass >

              <@fdsTimeline.timelineEvent>

                  <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                      <@fdsDataItems.dataValues key="Date and time" value=item.dateTimeDisplay />
                      <@fdsDataItems.dataValues key=item.personLabelText value=item.personName />
                      <#if item.personEmailLabel?has_content>
                          <@fdsDataItems.dataValues key=item.personEmailLabel value=item.personEmail />
                      </#if>
                </@fdsDataItems.dataItem>

                <#list item.dataItemRows as row>
                    <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                    <#list row.dataItems as key, value>
                        <@fdsDataItems.dataValues key=key value=value />
                    </#list>
                    </@fdsDataItems.dataItem>
                </#list>

              <#if item.uploadedFileViews?has_content>
                <@pwaFiles.uploadedFileList downloadUrl=springUrl(item.fileDownloadUrl) existingFiles=item.uploadedFileViews blockClass="case-history" />
              </#if>

            </@fdsTimeline.timelineEvent>

          </@fdsTimeline.timelineTimeStamp>

        </#list>

      </@fdsTimeline.timelineSection>

  </@fdsTimeline.timeline>

</#macro>