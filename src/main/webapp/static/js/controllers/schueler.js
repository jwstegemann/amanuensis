'use strict';

/* Controllers */

function SchuelerListCtrl($scope, $http, $filter, dictionary, message) {
  var url = "/schueler"
  /*
   * init dictionaries
   */
  $scope.geschlecht = dictionary.get("Geschlecht");    

  $scope.schuelerList = undefined;
  $scope.columns = [
    {title: "Name", attribute: "name"},
    {title: "Vorname", attribute: "vorname"},
    {title: "Geschlecht", attribute: "geschlecht", dictionary: $scope.geschlecht}
  ];

  $scope.optionen =  {selection: 'many'};

  $scope.reload = function() {
		$http.get(url).success(function(response) {
			$scope.schuelerList = response;
		});
  }

  $scope.init = function() {
    $scope.reload();
  }

  $scope.deleteSchueler = function(schuelerId) {
    $http.delete(url + "/" + schuelerId).success(function(response) {
        console.log("geloescht: " + schuelerId);
        $scope.reload();
        message.success('Löschen erfolgreich.','Der Schüler mit der id ' + schuelerId + ' wurde entfernt.');
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

 function SchuelerDetailCtrl($scope, $routeParams, $http, $location, dictionary, message) {

  $scope.activeSection = "personenDaten";

  var createMode = false;
  if($routeParams.schuelerId == undefined) createMode = true;

  var request = {};

  if (createMode) {
    request.url = "/schueler";
    request.method = "POST";
  }
  else {
    request.url = "/schueler/" + $routeParams.schuelerId;
    request.method = "PUT";
  }

  $scope.schueler = { _id: null, version: null }
  $scope.validation = { errors: [] }
  
  $scope.reload = function() {
    $http.get(request.url).success(function(response) {
      $scope.schueler = response;
    });
  }

  $scope.save = function(close) {
    request.data = $scope.schueler;
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
    $location.path("/schueler");
  }

  $scope.init = function() {
    if ($routeParams.schuelerId != undefined) {
      $scope.reload();
    }

    /*
     * init dictionaries
     */
    $scope.geschlecht = dictionary.get("Geschlecht");
  }

}

function SelectReferenceCtrl($scope, $routeParams, $http, $location, dictionary, message) {

  var url = "/schule"

  var selectionModal = $('#selection-modal');

  console.log("modal: %o", selectionModal);

  selectionModal.modal({
    keyboard: true,
    backdrop: "static",
    show: false
  });

  selectionModal.on('show', function () {
    $scope.reload();
  });

  /*
   * init dictionaries
   */
  $scope.schulform = dictionary.get("Schulform"); 

  $scope.columns = [
    {title: "Name", attribute: "name"},
    {title: "Schulform", attribute: "schulform", dictionary: $scope.schulform}
  ];

  $scope.entitiesList = undefined;
  $scope.selectedElement = undefined;

  $scope.optionen =  {noCreate: true, noDelete: true, noEdit: true, selection: 'single'};

  $scope.reload = function() {
    $http.get(url).success(function(response) {
      $scope.entitiesList = response;
    });
  }  

  $scope.select = function() {
    selectionModal.modal('hide');
  }

  $scope.cancel = function() {
    selectionModal.modal('hide');
  }

  $scope.openSelection = function() {
    selectionModal.modal('show');
  }

  $scope.changeSelection = function(value) {
    console.log("change Value: %o", value)
  }
}