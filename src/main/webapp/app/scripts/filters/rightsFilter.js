angular.module('amanuensisApp').filter('userRights', function(gettextCatalog) {
  return function(right) {
    switch (right) {
      case 'canRead': return gettextCatalog.getString('read');
      case 'canWrite': return gettextCatalog.getString('read & write');
      case 'canGrant': return  gettextCatalog.getString('read, write & grant');
    }
  }
})