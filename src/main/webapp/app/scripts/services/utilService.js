'use strict';


angular.module('amanuensisApp')
  .factory('utilService', function () {

    var overlay = $('#md-overlay');

    return {

      showModal: function(modalId)  {
            overlay.addClass('md-show');            
            $(modalId).addClass('md-show');
      },

      hideModal: function(modalId) {
            $(modalId).removeClass('md-show');
            overlay.removeClass('md-show');            
      } 

    };
  });
