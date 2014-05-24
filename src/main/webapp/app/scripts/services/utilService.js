'use strict';


angular.module('amanuensisApp')
  .factory('utilService', function () {

    var overlay = $('#md-overlay');

    var counter = 0;

    function showOverlay() {
      //console.log('+++ show Overlay, counter before: ' + counter);
      if (counter++ === 0) {
            overlay.addClass('md-show');   
      }
//      console.log('+++ show Overlay, counter after: ' + counter);
    }

    function hideOverlay() {
//      console.log('--- hide Overlay, counter before: ' + counter);

      if (--counter <= 0) {
            overlay.removeClass('md-show');
            counter = 0;
      }

//      console.log('--- hide Overlay, counter after: ' + counter);

    }

    function showModalElement(modal)  {
      if (!modal.hasClass('md-show')) {
        showOverlay();
        modal.addClass('md-show')
        setTimeout(function(){
          modal.find('.start-focus').focus();
        }, 120);
      }
    }

    function hideModalElement(modal) {
      if (modal.hasClass('md-show')) {
        modal.removeClass('md-show');
        hideOverlay();
      }
    }

    return {

      showModalElement: showModalElement,

      hideModalElement: hideModalElement,

      showModal: function(modalId)  {
        showModalElement($(modalId));
      },

      hideModal: function(modalId) {
        hideModalElement($(modalId));
      } 

    };
  });
