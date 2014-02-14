'use strict';


angular.module('amanuensisApp')
  .factory('utilService', function () {

    var overlay = $('#md-overlay');

    var counter = 0;

    function showOverlay() {
      console.log('+++ show Overlay, counter before: ' + counter);
      if (counter++ === 0) {
            overlay.addClass('md-show');   
      }
      console.log('+++ show Overlay, counter after: ' + counter);
    }

    function hideOverlay() {
      console.log('--- hide Overlay, counter before: ' + counter);

      if (--counter <= 0) {
            overlay.removeClass('md-show');
            counter = 0;
      }

      console.log('--- hide Overlay, counter after: ' + counter);

    }

    return {

      showModalElement: function(modal)  {
        showOverlay();
        modal.addClass('md-show')
        setTimeout(function(){
          modal.find('.start-focus').focus();
        }, 120);
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
            }, 120);
      },

      hideModal: function(modalId) {
            $(modalId).removeClass('md-show');
            hideOverlay();
      } 

    };
  });
