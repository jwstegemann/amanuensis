'use strict';

/* Filters */

angular.module('klaraFilters', []).filter('checkmark', function() {
  return function(input) {
    return input ? '\u2713' : '\u2718';
  };
}).filter('dictionary', function() {
  return function(key, dictionary) {
    console.log("called filter!");

    if (angular.isDefined(dictionary)) return dictionary[key] || "unknown key " + key;
    else return key;
  };
});