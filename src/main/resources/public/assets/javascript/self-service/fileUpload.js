'use strict';

class FileUpload {
  constructor($fileInput) {
    this.$fileInput = $fileInput;
    this.fileInputId = this.$fileInput.attr('id');
    this.$dropzone = $(`#${this.fileInputId}-dropzone`);

    this._bindEventHandlers();

    $fileInput.fileupload({
      dataType: 'json',
      dropZone: this.$dropzone,
      limitConcurrentUploads: 1,
      add: (e, data) => this._addHandler(data),
      progress: (e, data) => this._progressHandler(data),
      done: (e, data) => this._doneHandler(data),
      fail: (e, data) => this._failHandler(data)
    });
  }

  _bindEventHandlers() {
    this.$dropzone.bind('dragover', () => $(this).addClass('fileupload-dropzone--hover'));
    this.$dropzone.bind('dragleave', () => $(this).removeClass('fileupload-dropzone--hover'));
    this.$dropzone.bind('drop', () => $(this).removeClass('fileupload-dropzone--hover'));
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('focus', () => $(this).addClass('fileupload-dropzone__hidden-input--has-focus'));
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('blur', () => $(this).removeClass('fileupload-dropzone__hidden-input--has-focus'));

    /* Keyboard accessibility - we make the 'choose a file' link-styled label appear focused when the file input
       actually has focus. In IE, the spacebar makes the file picker appear, but the enter key doesn't. So we intercept
       the keypress and call click() on the label instead.*/
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('keydown', (e) => {
      if(e.keyCode===13) {
        $(this).siblings('.fileupload-dropzone__button').click();
        e.preventDefault();
        return false;
      }
    });
  }

  _addHandler(data) {
    const filename = data.files[0].name;
    const size = data.files[0].size;

    const maxSize = parseInt(this.$fileInput.attr('upload-file-max-size'));
    const allowedExtensions = this.$fileInput.attr('accept').split(',');

    const indexCard = $(`.uploaded-file`).last().index() + 1;
    const uploadedFileInfoHtml = `<div class="uploaded-file"><div class="uploaded-file__info">
        <div class="uploaded-file__file-info-wrapper">
          <span class="uploaded-file__filename"> </span> 
          <span class="uploaded-file__extra-info"> </span>
        </div>
        <div class="uploaded-file__error"></div>
      </div></div>`;
    const progressText = `<div class="uploaded-file__progress">- <span class="uploaded-file__progress-value" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"></span><span class="uploaded-file__progress-unit">%</span></div>`;

    data.context = $(uploadedFileInfoHtml);

    $('.fileupload').prepend(data.context);

    this._hideDropzone();

    data.context.find('.uploaded-file__filename').text(filename);
    data.context.find('.uploaded-file__extra-info').html(`- ${this._getReadableFileSizeString(size)}`);

    if(size > maxSize) {
      data.context.addClass('uploaded-file--error');
      this._showError(data.context, `Sorry, this file is too large. The maximum size allowed is ${this._getReadableFileSizeString(maxSize)}`);
      this._addRemoveLink(data);
    }
    else if(!allowedExtensions.some((extension) => { return this._endsWith(filename, extension.trim()); })) {
      data.context.addClass('uploaded-file--error');
      this._showError(data.context, `Sorry, this type of file is not allowed. File types accepted are ${allowedExtensions.join(', ')}`);
      this._addRemoveLink(data);
    }
    else {
      data.context.attr('id', indexCard);
      data.context.find('.uploaded-file__file-info-wrapper').append(progressText);
      data.submitTimestamp = new Date().getTime();
      data.submit();
    }
  }

  _progressHandler(data) {
    const progress = parseInt(data.loaded / data.total * 100, 10);
    if (progress === 100) {
      data.context.find('.uploaded-file__progress').text("Validating JSON...");
    } else {
      data.context.find('.uploaded-file__progress-value').attr('aria-valuenow', progress).text(progress);
    }
  };

  _addRemoveLink(data) {
    const removeLink = ` <a href="#" class="govuk-link uploaded-file__delete-link">Remove file <span class="govuk-visually-hidden">${data.files[0].name}</span></a>`;
    data.context.find('.uploaded-file__file-info-wrapper').append(removeLink);
    data.context.find('.uploaded-file__delete-link').on('click', (e) => {
      e.preventDefault();
      data.context.remove();
      this._showDropzone();
      this._disableUpdateButton();
    });
  }

  _doneHandler(data) {
    if(data.result.valid) {
      this.uploadedFileId = data.result.fileId;

      const downloadUrl = this.$fileInput.attr('data-download-url');
      const filenameDownloadLink = `<a href="${downloadUrl + data.result.fileId}" class="govuk-link">${data.files[0].name} </a>`;

      data.context.attr('data-fileId', data.result.fileId);
      data.context.find('.uploaded-file__filename').html(filenameDownloadLink);
      data.context.find('.uploaded-file__info').append($(`<input type="hidden" name="uploadedFileId" value="${this.uploadedFileId}"/>`));
      this._enableUpdateButton();
    }
    else if (data.result.validationErrorMessages) {
      this._showValidationErrors(data.context, data.result.validationErrorMessages);
    }
    else {
      this._showError(data.context, `Sorry, there was a problem uploading the file. ${data.result.uploadErrorMessage}`);
    }

    this._addRemoveLink(data);
    data.context.find('.uploaded-file__progress').remove();
    data.context.focus();
  }

  _failHandler(data) {
    this._showError(data.context,'Sorry, there was a problem uploading the file.');
    this._addRemoveLink(data);
  }

  _showError(context, msg) {
    context.find('.uploaded-file__error').text(msg);
  }

  _showValidationErrors(context, errorMessages) {
    const errorList = $('<ul class="uploaded-file__error"></ul>');
    errorMessages.forEach(e => {
      errorList.append(`<li>${e}</li>`)
    });
    context.find('.uploaded-file__error').text('The journey definition has the following errors:');
    context.find('.uploaded-file__error').append(errorList);
  }

  _endsWith(filename, extension) {
    return filename.toLowerCase().lastIndexOf(extension.toLowerCase()) === filename.length - extension.length;
  }

  _getReadableFileSizeString(fileSizeInBytes) {
    let i = -1;
    const byteUnits = [' kB', ' MB', ' GB'];
    do {
      fileSizeInBytes = fileSizeInBytes / 1024;
      i++;
    } while (fileSizeInBytes > 1024);

    return Math.max(fileSizeInBytes, 1).toFixed() + byteUnits[i];
  }

  _hideDropzone() {
    this.$dropzone.hide();
  }

  _showDropzone() {
    this.$dropzone.show();
  }

  _enableUpdateButton() {
    $('#update-journey-button')
      .removeClass("govuk-button--disabled")
      .removeAttr("disabled")
      .removeAttr("aria-disabled")
  }

  _disableUpdateButton() {
    $('#update-journey-button')
      .addClass("govuk-button--disabled")
      .attr("disabled", "true")
      .attr("aria-disabled", "true")
  }

}

$(document).ready(() => {
  $('.fileupload-dropzone__hidden-input').each((index, element) => new FileUpload($(element)));
});
