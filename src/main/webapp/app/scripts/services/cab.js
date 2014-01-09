'use strict';

angular.module('amanuensisApp')
  .factory('cab', function () {

    var appStatus = 0;

    return {
      /*
       * define status constants
       */
      //ToDo: make to constants if possible

      status: {
        editStory: 1,
        editSlot: 2,
        editStoriesinSlot: 4,
        query: 8,
        selectStory: 16
      },

      setAppStatus: function (newStatus) {
        appStatus = newStatus;
      }

    };
  });
