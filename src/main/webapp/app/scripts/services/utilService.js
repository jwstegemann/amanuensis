'use strict';


angular.module('amanuensisApp')
  .factory('utilService', function () {

    var overlay = $('#md-overlay');

    var counter = 0;

    function showOverlay() {
      if (counter++ === 0) {
            overlay.addClass('md-show');   
      }
    }

    function hideOverlay() {
      if (--counter <= 0) {
            overlay.removeClass('md-show');
            counter = 0;
      }
    }

    return {

      showModalElement: function(modal)  {
            showOverlay();
            modal.addClass('md-show')
            setTimeout(function(){
              modal.find('.start-focus').focus();
            }, 50);
      },

      hideModalElement: function(modal) {
            modal.removeClass('md-show');
            hideOverlay();
      },

      showModal: function(modalId)  {
            showOverlay();
            var modal =  $(modalId);        
            modal.addClass('md-show')
            setTimeout(function(){
              modal.find('.start-focus').focus();
            }, 50);
      },

      hideModal: function(modalId) {
            $(modalId).removeClass('md-show');
            hideOverlay();
      } 

    };
  });
