'use strict';

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var FileUpload =
/*#__PURE__*/
function () {
  function FileUpload($fileInput) {
    var _this = this;

    _classCallCheck(this, FileUpload);

    this.$fileInput = $fileInput;
    this.fileInputId = this.$fileInput.attr('id');
    this.$dropzone = $("#".concat(this.fileInputId, "-dropzone"));

    this._bindEventHandlers();

    $fileInput.fileupload({
      dataType: 'json',
      dropZone: this.$dropzone,
      limitConcurrentUploads: 2,
      add: function add(e, data) {
        return _this._addHandler(data);
      },
      progress: function progress(e, data) {
        return _this._progressHandler(data);
      },
      done: function done(e, data) {
        return _this._doneHandler(data);
      },
      fail: function fail(e, data) {
        return _this._failHandler(data);
      }
    });
  }

  _createClass(FileUpload, [{
    key: "_bindEventHandlers",
    value: function _bindEventHandlers() {
      var _this2 = this;

      this.$dropzone.bind('dragover', function () {
        return $(_this2).addClass('fileupload-dropzone--hover');
      });
      this.$dropzone.bind('dragleave', function () {
        return $(_this2).removeClass('fileupload-dropzone--hover');
      });
      this.$dropzone.bind('drop', function () {
        return $(_this2).removeClass('fileupload-dropzone--hover');
      });
      this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('focus', function () {
        return $(_this2).addClass('fileupload-dropzone__hidden-input--has-focus');
      });
      this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('blur', function () {
        return $(_this2).removeClass('fileupload-dropzone__hidden-input--has-focus');
      });
      /* Keyboard accessibility - we make the 'choose a file' link-styled label appear focused when the file input
         actually has focus. In IE, the spacebar makes the file picker appear, but the enter key doesn't. So we intercept
         the keypress and call click() on the label instead.*/

      this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('keydown', function (e) {
        if (e.keyCode === 13) {
          $(_this2).siblings('.fileupload-dropzone__button').click();
          e.preventDefault();
          return false;
        }
      });
    }
  }, {
    key: "_addHandler",
    value: function _addHandler(data) {
      var _this3 = this;

      var filename = data.files[0].name;
      var size = data.files[0].size;
      var maxSize = parseInt(this.$fileInput.attr('upload-file-max-size'));
      var allowedExtensions = this.$fileInput.attr('accept').split(',');
      var indexCard = $(".uploaded-file").last().index() + 1;
      var uploadedFileInfoHtml = "<div class=\"uploaded-file\"><div class=\"uploaded-file__info\">\n        <div class=\"uploaded-file__file-info-wrapper\">\n          <span class=\"uploaded-file__filename\"> </span> \n          <span class=\"uploaded-file__extra-info\"> </span>\n        </div>\n        <div class=\"uploaded-file__error\"></div>\n      </div></div>";
      var fileDescriptionHtml = "<div class=\"govuk-form-group govuk-form-group--file-upload\"><label class=\"govuk-label\" for=\"file-upload-description\">File description</label>\n                                  <textarea class=\"govuk-textarea govuk-textarea--file-upload\" id=\"file-upload-description\" name=\"documentListDetails[".concat(indexCard, "].uploadedFileDescription\" rows=\"2\"></textarea>\n                                 </div>");
      var progressText = "<div class=\"uploaded-file__progress\">- <span class=\"uploaded-file__progress-value\" role=\"progressbar\" aria-valuemin=\"0\" aria-valuemax=\"100\" aria-valuenow=\"0\"></span><span class=\"uploaded-file__progress-unit\">%</span></div>";
      data.context = $(uploadedFileInfoHtml);
      $('.fileupload').prepend(data.context);
      data.context.find('.uploaded-file__filename').text(filename);
      data.context.find('.uploaded-file__extra-info').html("- ".concat(this._getReadableFileSizeString(size)));

      if (size > maxSize) {
        data.context.addClass('uploaded-file--error');

        this._showError(data.context, "Sorry, this file is too large. The maximum size allowed is ".concat(this._getReadableFileSizeString(maxSize)));
      } else if (!allowedExtensions.some(function (extension) {
        return _this3._endsWith(filename, extension.trim());
      })) {
        data.context.addClass('uploaded-file--error');

        this._showError(data.context, "Sorry, this type of file is not allowed. File types accepted are ".concat(allowedExtensions.join(', ')));
      } else {
        data.context.attr('id', indexCard);
        data.context.find('.uploaded-file__info').append(fileDescriptionHtml);
        data.context.find('.uploaded-file__file-info-wrapper').append(progressText);
        data.submitTimestamp = new Date().toISOString();
        data.submit();
      }
    }
  }, {
    key: "_progressHandler",
    value: function _progressHandler(data) {
      var progress = parseInt(data.loaded / data.total * 100, 10);
      data.context.find('.uploaded-file__progress-value').attr('aria-valuenow', progress).text(progress);
    }
  }, {
    key: "_doneHandler",
    value: function _doneHandler(data) {
      if (data.result.valid) {
        this.uploadedFileId = data.result.fileId;
        var downloadUrl = this.$fileInput.attr('data-download-url');
        var deleteUrl = this.$fileInput.attr('data-delete-url');
        var removeLink = " <a href=# class=\"govuk-link uploaded-file__delete-link\">Remove file <span class=\"govuk-visually-hidden\">".concat(data.files[0].name, "</span></a>");
        var filenameDownloadLink = "<a href=\"".concat(downloadUrl + data.result.fileId, "\" class=\"govuk-link\">").concat(data.files[0].name, " </a>");
        var indexCard = data.context.attr("id");
        data.context.attr('data-fileId', data.result.fileId);
        data.context.attr('data-deleteUrl', "".concat(deleteUrl).concat(data.result.fileId));
        data.context.find('.uploaded-file__filename').html(filenameDownloadLink);
        data.context.find('.uploaded-file__file-info-wrapper').append(removeLink);
        data.context.find('.uploaded-file__delete-link').on('click', function (e) {
          e.preventDefault();
          IRS.Modal.displayModal(FileUploadUtils.generateModal(data.files[0].name), "Remove uploaded file ".concat(data.files[0].name));
          FileUploadUtils.beforeRemove(data.result.fileId);
        });
        data.context.find('input[name="uploadedFileId"]').attr('value', data.result.fileId);
        data.context.find('.uploaded-file__info').append($("<input type=\"hidden\" name=\"documentListDetails[".concat(indexCard, "].uploadedFileId\" value=\"").concat(this.uploadedFileId, "\"/>")));
        data.context.find('.uploaded-file__info').append($("<input type=\"hidden\" name=\"documentListDetails[".concat(indexCard, "].uploadedFileInstant\" value=\"").concat(data.submitTimestamp, "\"/>")));
      } else if (data.result.errorType === "VIRUS_FOUND_IN_FILE") {
        data.context.addClass('uploaded-file--error');
        data.context.find('.uploaded-file__extra-info').remove();
        data.context.find('.govuk-form-group--file-upload').remove();

        this._showError(data.context, "The file provided appears to contain a virus and it will not be uploaded.");
      } else {
        this._showError(data.context, "Sorry, there was a problem uploading the file. ".concat(data.result.errorMessage));
      }

      data.context.find('.uploaded-file__progress').remove();
      data.context.focus();
    }
  }, {
    key: "_failHandler",
    value: function _failHandler(data) {
      this._showError(data.context, 'Sorry, there was a problem uploading the file.');
    }
  }, {
    key: "_showError",
    value: function _showError(context, msg) {
      context.find('.uploaded-file__error').text(msg);
    }
  }, {
    key: "_endsWith",
    value: function _endsWith(filename, extension) {
      return filename.toLowerCase().lastIndexOf(extension.toLowerCase()) === filename.length - extension.length;
    }
  }, {
    key: "_getReadableFileSizeString",
    value: function _getReadableFileSizeString(fileSizeInBytes) {
      var i = -1;
      var byteUnits = [' kB', ' MB', ' GB', ' TB', ' PB', ' EB', ' ZB', ' YB'];

      do {
        fileSizeInBytes = fileSizeInBytes / 1024;
        i++;
      } while (fileSizeInBytes > 1024);

      return Math.max(fileSizeInBytes, 1).toFixed() + byteUnits[i];
    }
  }]);

  return FileUpload;
}(); // Scope the File Upload utility functions


var FileUploadUtils = {
  // return the modal content
  generateModal: function generateModal(filename) {
    return $("<div class=\"modal\" id=\"uploaded-file__display-modal\">\n          <p class=\"govuk-body modal__filename\">Are you sure you want to remove ".concat(filename, "?</p>\n          <div class=\"modal__actions\">\n            <button data-module=\"govuk-button\" class=\"modal__confirm-button govuk-button\">Remove</button>\n            <button data-module=\"govuk-button\" class=\"modal__cancel-button close-modal govuk-button govuk-button--link-exit\">Cancel</button>\n          </div>\n        </div>"));
  },
  // add a click handler to the modal remove action and do an ajax post to remove the file
  beforeRemove: function beforeRemove(fileId) {
    $('#modal-overlay .modal__confirm-button').on('click', function () {
      $('span.govuk-error-message').remove();
      var csrf = $('input[name="_csrf"]').attr('value');
      var fileIdElement = $(".uploaded-file[data-fileId=\"".concat(fileId, "\"]"));
      var url = fileIdElement.attr('data-deleteUrl');
      var removeErrorMessage = $("<span class=\"govuk-error-message\">Sorry, there was a problem removing this file.</span>");
      $.post(url, {
        _csrf: csrf
      }).done(function () {
        fileIdElement.remove();
        IRS.Modal.closeModal();
      }).fail(function () {
        return $('.modal__filename').append(removeErrorMessage);
      });
    });
  }
};
$(document).ready(function () {
  $('.fileupload-dropzone__hidden-input').each(function (index, element) {
    return new FileUpload($(element));
  });
  $('.uploaded-file').each(function (index, element) {
    return $(element).find('.uploaded-file__delete-link').click(function (event) {
      event.preventDefault();
      var fileId = $(element).attr('data-fileId');
      var fileName = $(element).attr('data-fileName');
      IRS.Modal.displayModal(FileUploadUtils.generateModal(fileName), "Remove uploaded file ".concat(fileName));
      FileUploadUtils.beforeRemove(fileId);
    });
  });
});