<#include '../layout.ftl'>

<#macro upload id uploadUrl downloadUrl maxAllowedSize allowedExtensions dropzoneText="Drag and drop the .json file here" existingFiles=[] validationErrors=[]>

  <div id="${id}-dropzone" class="fileupload-dropzone">
    <div class="fileupload-dropzone__text">
      ${dropzoneText}, or
      <input id="${id}"
         class="fileupload-dropzone__hidden-input"
         type="file"
         name="file"
         tabindex="-1"
         data-form-data='{"${_csrf.parameterName}": "${_csrf.token}"}'
         data-url="<@spring.url uploadUrl/>"
         data-download-url="<@spring.url downloadUrl/>"
         upload-file-max-size="${maxAllowedSize}"
         accept="${allowedExtensions}"
      >
      <label for="${id}" class="fileupload-dropzone__link" tabindex="0">choose a file</label>
    </div>
  </div>

  <div class="fileupload">
    <#list existingFiles as file>
      <@uploadedFile
      index=file?index
      htmlId=file.getFileId()
      fileName=file.getFileName()
      fileSize=file.getFileSize()
      fileDescription=file.getFileDescription()
      fileId=file.getFileId()
      fileUploadedInstant=file.getFileUploadedTime()
      url=downloadUrl+file.getFileId()
      deleteUrl=deleteUrl+file.getFileId()
      sourceUploadId=id/>
    </#list>
  </div>

  <#list validationErrors as validationError>
    <div class="error-message">${validationError}</div>
  </#list>
  <script src="<@spring.url '/assets/javascript/vendor/jquery/jquery-3.3.1.min.js'/>"></script>
  <script src="<@spring.url '/assets/javascript/vendor/jquery/jquery.iframe-transport.min.js'/>"></script>
  <script src="<@spring.url '/assets/javascript/vendor/jquery/jquery-ui.min.js'/>"></script>
  <script src="<@spring.url '/assets/javascript/vendor/jquery/jquery.fileupload.min.js'/>"></script>
  <script src="<@spring.url '/assets/static/js/self-service/fileUpload.js'/>"></script>
</#macro>


<#macro uploadedFile index htmlId fileName fileSize fileDescription fileId fileUploadedInstant url deleteUrl sourceUploadId>
  <div id="${htmlId}-uploaded-file" data-fileId="${fileId}" data-fileName="${fileName}" data-deleteUrl="<@spring.url deleteUrl/>" class="uploaded-file" data-source-upload-id="${sourceUploadId}">
    <div class="uploaded-file__info">
      <div class="uploaded-file__file-info-wrapper uploaded-file__file-info-wrapper--saved">
        <span class="uploaded-file__filename">
          <#if url?has_content>
            <a href="<@spring.url url/>" class="govuk-link">${fileName}</a>
          </#if>
        </span>
        <span class="uploaded-file__extra-info"> -
          <#if fileSize?has_content>
            ${fileSize}
          </#if>
        </span>
        <a href="#" class="govuk-link uploaded-file__delete-link uploaded-file__delete-link--saved">Remove file <span class="govuk-visually-hidden">${fileName}</span></a>
      </div>
    </div>
    <input type="hidden" name="uploadedFileId" value="${fileId}">
  </div>
</#macro>
