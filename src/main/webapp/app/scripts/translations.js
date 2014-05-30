angular.module('gettext').run(['gettextCatalog', function (gettextCatalog) {
/* jshint -W100 */
    gettextCatalog.setStrings('de', {"My ToDos":"Meine ToDos","found {{result.hits.total}} stories in {{result.took}} ms.":"{{result.hits.total}} Stories in {{result.took}} ms gefunden."});
/* jshint +W100 */
}]);