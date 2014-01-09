'use strict';


angular.module('amanuensisApp')
  .factory('utilService', function () {

    var overlay = $('#md-overlay');

    return {

      showModal: function(modalId)  {
            overlay.addClass('md-show');   
            var modal =  $(modalId);        
            modal.addClass('md-show')
            setTimeout(function(){
              modal.find('.start-focus').focus();
            }, 50);
      },

      hideModal: function(modalId) {
            $(modalId).removeClass('md-show');
            overlay.removeClass('md-show');            
      } 

    };
  });
