'use strict';

function addFlowListener(element, type, fn){
    var flow = type == 'over';
    element.addEventListener('OverflowEvent' in window ? 'overflowchanged' : type + 'flow', function(e){
        if (e.type == (type + 'flow') ||
        ((e.orient == 0 && e.horizontalOverflow == flow) ||
        (e.orient == 1 && e.verticalOverflow == flow) ||
        (e.orient == 2 && e.horizontalOverflow == flow && e.verticalOverflow == flow))) {
            e.flow = type;
            return fn.call(this, e);
        }
    }, false);
};
    
function fireEvent(element, type, data, options){
    var options = options || {},
        event = document.createEvent('Event');
    event.initEvent(type, 'bubbles' in options ? options.bubbles : true, 'cancelable' in options ? options.cancelable : true);
    for (var z in data) event[z] = data[z];
    element.dispatchEvent(event);
};
    
function addResizeListener(element, fn){
    var resize = 'onresize' in element && element.onresize !== null;
    if ((!resize || resize === null) && !element._resizeSensor) {
        var sensor = element._resizeSensor = document.createElement('div');
            sensor.className = 'resize-sensor';
            sensor.innerHTML = '<div class="resize-overflow"><div></div></div><div class="resize-underflow"><div></div></div>';
                
        var x = 0, y = 0,
            first = sensor.firstElementChild.firstChild,
            last = sensor.lastElementChild.firstChild,
            matchFlow = function(event){
                var change = false,
                width = element.offsetWidth;
                if (x != width) {
                    first.style.width = width - 1 + 'px'; 
                    last.style.width = width + 1 + 'px';
                    change = true;
                    x = width;
                }
                var height = element.offsetHeight;
                if (y != height) {
                    first.style.height = height - 1 + 'px';
                    last.style.height = height + 1 + 'px';  
                    change = true;
                    y = height;
                }
                if (change && event.currentTarget != element) fireEvent(element, 'resize');
            };
            
        if (getComputedStyle(element).position == 'static'){
            element.style.position = 'relative';
            element._resizeSensor._resetPosition = true;
        }
        addFlowListener(sensor, 'over', matchFlow);
        addFlowListener(sensor, 'under', matchFlow);
        addFlowListener(sensor.firstElementChild, 'over', matchFlow);
        addFlowListener(sensor.lastElementChild, 'under', matchFlow); 
        element.appendChild(sensor);
        matchFlow({});
    }
        var events = element._flowEvents || (element._flowEvents = []);
        if (events.indexOf(fn) == -1) events.push(fn);
        if (!resize) element.addEventListener('resize', fn, false);
        element.onresize = function(e){
            events.forEach(function(fn){
                fn.call(element, e);
            });
        };
};
    
function removeResizeListener(element, fn){
    var index = element._flowEvents.indexOf(fn);
    if (index > -1) element._flowEvents.splice(index, 1);
    if (!element._flowEvents.length) {
        var sensor = element._resizeSensor;
        if (sensor) {
            element.removeChild(sensor);
            if (sensor._resetPosition) element.style.position = 'static';
            delete element._resizeSensor;
        }
        if ('onresize' in element) element.onresize = null;
        delete element._flowEvents;
    }
    element.removeEventListener('resize', fn);
};

angular.module('amanuensisApp', [
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngAnimate',
  'angular-growl',
  'http-auth-interceptor',
  'chieffancypants.loadingBar',
  'ngTagsInput',
  'typeahead',
  'selectbox',
  'ngQuickDate'
]).config(function ($routeProvider) {
    $routeProvider
      .when('/story/:storyId?', {
        templateUrl: 'views/story.html',
        controller: 'StoryCtrl',
        reloadOnSearch: false
      })
      .when('/story/:slotName/:fromStoryId/:fromStoryTitle', {
        templateUrl: 'views/story.html',
        controller: 'StoryCtrl',
        reloadOnSearch: false
      })
      .when('/query/:queryString?', {
        templateUrl: 'views/query.html',
        controller: 'QueryCtrl'
      })
      .when('/login', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl'
      })
      .when('/graph/findpaths', {
        templateUrl: 'views/findpaths.html',
        controller: 'FindPathsCtrl'
      })
      .otherwise({
        redirectTo: '/query'
      });

  }).config(function(growlProvider) {
    growlProvider.onlyUniqueMessages(false);
    growlProvider.globalTimeToLive(3000);

  }).config(function($httpProvider) {
    // interecpt error when communication with the backend
    $httpProvider.interceptors.push(['$q','$rootScope', function($q,$rootScope) {
      return {
        'responseError': function(rejection) {
          if (rejection.status !== 401) {
            if (rejection.status === 404) {
              console.log("NotFound-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: 'I am sorry, but you are not allowed to do this.'});
            }
            else if (rejection.status === 423) {
              console.log("OptimisticLock-Info communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: 'I am sorry, but somebody else has edited this object since your last update. Please reload the story.'});
            }
            else if (rejection.status === 412) {
              console.log("Validation-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: rejection.data[0].text});
            }
            else if (rejection.status === 400) {
              console.log("Bad-Request-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: rejection.data});
            }            
            else {
              console.log("Fatal-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: 'Error communicating with Amanuensis-backend. Please try again or contact your system-administrator.'});
            }
          }
          return $q.reject(rejection);
        }
      };
    }]);

  }).config(function(ngQuickDateDefaultsProvider) {
    // Configure with icons from font-awesome
    return ngQuickDateDefaultsProvider.set({
      closeButtonHtml: "<i class='fa fa-times'></i>",
      buttonIconHtml: "<i class='fa fa-calendar'></i>",
      nextLinkHtml: "<i class='fa fa-chevron-right'></i>",
      prevLinkHtml: "<i class='fa fa-chevron-left'></i>"
    })
  }).run(function ($rootScope, $location) {
    //init mode and stack
    $rootScope.selectMode = false;
    $rootScope.stack = undefined;
    $rootScope.appState = undefined;
    $rootScope.editMode = false;
    $rootScope.targetMode = false;    
    $rootScope.targetStack = undefined;    

    if(!(($location.host() === 'localhost' || $location.host() === '0.0.0.0') && $location.port() === 9000)) {
      if ($location.protocol() !== 'https') {
        //FixMe: Is there a way to switch the protocol?
        $rootScope.$broadcast('error',{errorMessage: 'Please use https in your URL to make sure, that nobody gets to know your credentials.'});
      }
    }

  });


