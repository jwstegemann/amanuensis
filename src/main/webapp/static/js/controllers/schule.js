'use strict';

/* Controllers */

function SchuleListCtrl($scope, $http, $filter, dictionary, message) {
  var url = "/schule"
  /*
   * init dictionaries
   */
  $scope.schulform = dictionary.get("Schulform");    

  $scope.schuleList = undefined;
  $scope.columns = [
    {title: "Name", attribute: "name"},
    {title: "Schulform", attribute: "schulform", dictionary: $scope.schulform}
  ];

  $scope.optionen =  {selection: 'many'};

  $scope.reload = function() {
		$http.get(url).success(function(response) {
			$scope.schuleList = response;
		});
  }

  $scope.init = function() {
    $scope.reload();
  }

  $scope.deleteSchule = function(schuleId) {
    $http.delete(url + "/" + schuleId).success(function(response) {
        console.log("geloescht: " + schuleId);
        $scope.reload();
        message.success('Löschen erfolgreich.','Die Schule mit der id ' + schuleId + ' wurde entfernt.');
    });
  }

  $scope.test = function(value) {
    console.log("called function!");
    return value;
  }
}


/*
 *
 */

 function SchuleDetailCtrl($scope, $routeParams, $http, $location, dictionary, message) {

  $scope.activeSection = "schulDaten";

  var createMode = false;
  if($routeParams.schuleId == undefined) createMode = true;

  var request = {};

  if (createMode) {
    request.url = "/schule";
    request.method = "POST";
  }
  else {
    request.url = "/schule/" + $routeParams.schuleId;
    request.method = "PUT";
  }

  $scope.schule = { _id: null, version: null }
  $scope.validation = { errors: [] }
  
  $scope.reload = function() {
    $http.get(request.url).success(function(response) {
      $scope.schule = response;
    });
  }

  $scope.save = function(close) {
    request.data = $scope.schule;
    $http(request).success(function(response) {
      if(close) $scope.goToList();
      else $scope.reload();
      message.success('Herzlichen Glückwunch!','Ihre Änderungen wurden erfolgreich gespeichert.');
    }).error(function(data, status, headers, config) {
      if(status == 412) {
        //clear header messages
        $scope.validation.errors = [];

        //clear all tips
        $(".validationResult").html("");

        //add tips to fields and header message
        angular.forEach(data, function(msg, key) {
          var controlGroup = $("#" + msg.field);
          controlGroup.addClass("error");
          controlGroup.find(".validationResult").html(msg.text);
          $scope.validation.errors.push(msg);
        });
      }
    });
  }

  $scope.goToList = function() {
    $location.path("/schule");
  }

  $scope.init = function() {
    if ($routeParams.schuleId != undefined) {
      $scope.reload();
    }

    /*
     * init dictionaries
     */
    $scope.schulform = dictionary.get("Schulform");
  }

}

