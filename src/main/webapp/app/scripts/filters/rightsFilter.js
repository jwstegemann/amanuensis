angular.module('amanuensisApp').filter('userRights', function() {
  return function(right) {
    switch (right) {
      case 'canRead': return 'read';
      case 'canWrite': return 'read & write';
      case 'canGrant': return  'read, write & grant';
    }
  }
})