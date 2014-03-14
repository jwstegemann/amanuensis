'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService,slotService,$rootScope,$location,$window,utilService,growl,queryService,$http) {

    function hasSlot(name, slots) {
        var i = slots.length;
        while(i--) {
            if (slots[i].name === name) return true;
        }
        return false;
    }

    // init StoryContext
    $scope.reload = function() {
        $scope.inSlots = false;
        $scope.inStories = false;
        $scope.outSlots = false;
        $scope.outStories = false;

        if (angular.isDefined($routeParams.storyId)) {
        	$scope.context = storyService.get({storyId: $routeParams.storyId}, function(successData) {
                //Todo: Adjust when opening slots and stories from routeParams
                $rootScope.appState = 1;
                $scope.$broadcast('updateView', {
                    markdown: successData.story.content
                });
                updateScrollbar();
                $rootScope.editMode = false;

                //open up slot and story-list by queryParameter
                var slotToOpen = $location.search()['openSlot']
                var inbound = angular.isDefined($location.search()['inbound'])

//                console.log("should open slot: " + slotToOpen);
//                console.log("  in direction: ") + (inbound)?'inbound':'outbound';

                //check, of slot is available in the given direction
                if (angular.isDefined(slotToOpen)
                    && (
                        (inbound && hasSlot(slotToOpen, successData.inSlots)) 
                        || (!inbound && hasSlot(slotToOpen, successData.outSlots))
                    )
                ) {
                    $scope.selectSlot(slotToOpen, inbound);
                }
            });
        } 
        else {
        	// init empty StoryContext
        	$scope.context = { 
        		story: {
        			id: undefined,
        			title: undefined,
        			content: undefined,
                    created: "not yet",
                    createdBy: "who knows",
                    tags: []
        		},
        		inSlots: [],
        		outSlots: []
        	};

            setTimeout(function() {
              $('#story-title-input').focus();
            }, 200);

            $rootScope.appState = 1;

            $rootScope.editMode = true;
        }

        $scope.storyFilter = {};
        $scope.slotFilterRight = undefined;
        $scope.slotFilterLeft = undefined;
    }

    $scope.$on('saveStory', function() {
        if (
            angular.isUndefined($scope.context.story.title) ||
            $scope.context.story.title.length < 3 ||
            angular.isUndefined($scope.context.story.content) ||
            $scope.context.story.content.length < 3
        ) {
            $rootScope.$broadcast('error',{errorMessage: 'Sorry, but you story must have a title and a content. Please enter one...'});
        }
        else {
            // save existing story
        	if (angular.isDefined($scope.context.story.id)) {
        		storyService.update($scope.context.story, function(successData) {
                    growl.addSuccessMessage($scope.context.story.title + ' has been saved.');
                });
        	}
            // create new story
        	else {
                // in a slot
                if (angular.isDefined($routeParams.slotName) && angular.isDefined($routeParams.fromStoryId)) {
                    storyService.createInSlot({
                        fromStoryId : $routeParams.fromStoryId,
                        slotName: $routeParams.slotName
                    }, $scope.context.story, function(successData) {
                        $scope.context.story.id = successData.id;
                        $location.url("/story/" + $scope.context.story.id).replace();
                        growl.addSuccessMessage($scope.context.story.title + ' has been created in Slot ' + $routeParams.slotName + ' at Story ' + $routeParams.fromStoryTitle);
                    });

                    $rootScope.stack = undefined;

                }                    
                // on its own
                else {
            		storyService.create($scope.context.story, function(successData) {
            			$scope.context.story.id = successData.id;
                        $location.url("/story/" + $scope.context.story.id).replace();
                        growl.addSuccessMessage($scope.context.story.title + ' has been created.');
            		});
                }
        	}
        }
    });

    /*
     * Delete a Story after confirmation
     */
    $scope.$on('deleteStory', function() {
        utilService.showModal('#confirm-delete-modal');    
    });

    $scope.deleteStoryConfirmed = function() {
        //keep title for message
        var oldTitle = $scope.context.story.title;
        storyService.delete({storyId: $routeParams.storyId}, function(successData) {
            growl.addSuccessMessage(oldTitle + ' has been deleted.');    
            $window.history.back();
            utilService.hideModal('#confirm-delete-modal');    
        });
    }

    $scope.cancelConfirmDelete = function() {
        utilService.hideModal('#confirm-delete-modal');    
    }

    /*
     * Watch a Story for being changed by others
     */
    $scope.$on('watchStory', function() {
        utilService.showModal('#watch-modal');    
    });

    $scope.cancelWatch = function() {
        utilService.hideModal('#watch-modal');    
    }

    /*
     * Share a Story with other users
     */
    $scope.$on('shareStory', function() {
        if (angular.isDefined($scope.context.story.id)) {
            $scope.$broadcast("openShareStoryModal", {
                storyId: $scope.context.story.id
            });
        }
        else {
            $rootScope.$broadcast('error',{errorMessage: 'You cannot share a story that has not been saved yet.'});            
        }
    });

    /*
     * Add a Story to a Slot
     */
    $scope.$on('addStoryToSlot', function() {
        if (angular.isUndefined($scope.context.story.id)) {
            $rootScope.$broadcast('error',{errorMessage: 'Unfortunately you cannot add an unsaved story to a slot.'});
        }
        else if ($scope.context.story.id === $rootScope.stack.storyId) {
            $rootScope.$broadcast('error',{errorMessage: 'Unfortunately it is not possible to link a story to itself.'});                        
        }
        else {
            slotService.add({
                toStoryId: $rootScope.stack.storyId,
                slotName: $rootScope.stack.slotName,
                storyId: $scope.context.story.id
            }, null, function (successData) {
                $rootScope.selectMode = false;
                growl.addSuccessMessage($scope.context.story.title + ' has been linked to ' + $rootScope.stack.storyTitle + ' as ' + $rootScope.stack.slotName); 
                $rootScope.stack = undefined;
                $scope.reload();
            });
        }
    });    

    $scope.$on('createSlot', function() {
        if (angular.isDefined($scope.context.story.id)) {
            $scope.$broadcast('askForSlotName',{
                    create: false,
                    storyId: $scope.context.story.id,
                    storyTitle: $scope.context.story.title
            }); 
        }
        else {
            $rootScope.$broadcast('error',{errorMessage: 'I am sorry, but you cannot create a new slot on an unsaved story.'});            
        }
    });

    $scope.$on('createStoryInSlot', function() {
        if (angular.isDefined($scope.context.story.id)) {
            if (angular.isDefined($scope.activeSlot) && $rootScope.appState === 4) {
                $rootScope.stack = {
                    storyId: $scope.context.story.id,
                    storyTitle: $scope.context.story.title,
                    slotName: $scope.activeSlot
                };                
                $location.url('/story/' + $scope.activeSlot + '/' + $scope.context.story.id + '/' + $scope.context.story.title)
            }
            else {
                $scope.$broadcast('askForSlotName',{
                    create: true,
                    storyId: $scope.context.story.id,
                    storyTitle: $scope.context.story.title
                });
            }
        }
        else {
            $rootScope.$broadcast('error',{errorMessage: 'Excuse me, but you cannot create a new story in a slot of an unsaved story.'});
        }
    });    

    $scope.selectSlot = function(slotName, inbound) {
        if ($scope.activeSlot === slotName) {
            if (inbound) $scope.hideInStories();
            else $scope.hideOutStories();
            
            $scope.activeSlot = undefined;
            $rootScope.appState = 2;

            //adjust location
            $location.search({});
        }
        else {
            //adjust location
            var query = {
                openSlot: slotName                
            };
            if (inbound) query.inbound=true;
            $location.search(query);

            // emit signal
            $scope.$broadcast('selectSlot',{
                storyId: $scope.context.story.id,
                storyTitle: $scope.context.story.title,
                slotName: slotName
            });
            
            //ToDo: wait for some Confirmation
            if (inbound) $scope.showInStories();
            else $scope.showOutStories();
            
            $scope.activeSlot = slotName;
            if (inbound) {
                $rootScope.appState = 3;    
            }
            else {
                $rootScope.appState = 4;
            }
            
        }

        $scope.storyFilter = {};
    }

    $scope.openStory = function(storyId,slotName,inbound) {
        var query = {};

        if (!inbound) {
            query.openSlot = slotName;
        }

        $location.url('/story/' + storyId).search(query);
    }

    /*
     * listening to toggleLeft and -Right events
     */

    $scope.$on('toggleLeft', function() {
        if ($scope.inSlots) {
            $scope.hideInSlots();
            $rootScope.appState = 1;

            $scope.slotFilterLeft = undefined;
            $scope.storyFilter = {};
        }
        else if (angular.isDefined($scope.context.story.id)) {
            $scope.showInSlots();
            $rootScope.appState = 2;
        }
    });

    $scope.$on('toggleRight', function() {
        if ($scope.outSlots) {
            $scope.hideOutSlots();
            $rootScope.appState = 1;

            $scope.slotFilterRight = undefined;
            $scope.storyFilter = {};
        }
        else if (angular.isDefined($scope.context.story.id)) {
            $scope.showOutSlots();
            $rootScope.appState = 2;
        }
    });

    /* 
     * show and hide slots and strories
     */
    $scope.showInSlots = function() {
        $scope.inSlots = true;
        $scope.outStories = false;
        $scope.activeSlot = undefined;
    }

    $scope.hideInSlots = function() {
        $scope.inSlots = false;
        $scope.inStories = false;
    }

    $scope.showInStories = function() {
        $scope.inStories = true;
        $scope.inSlots = true;
        $scope.outStories = false;
        $scope.outSlots = false;
    }

    $scope.hideInStories = function() {
        $scope.inStories = false;
    }


    $scope.showOutSlots = function() {
        $scope.outSlots = true;
        $scope.inStories = false;
        $scope.activeSlot = undefined;        
    }

    $scope.hideOutSlots = function() {
        $scope.outSlots = false;
        $scope.outStories = false;
    }

    $scope.showOutStories = function() {
        $scope.outStories = true;
        $scope.outSlots = true;
        $scope.inStories = false;
        $scope.inSlots = false;
    }

    $scope.hideOutStories = function() {
        $scope.outStories = false;
    }

    /*
     * Preview and Editor
     */

    $scope.showContentEditor = function() {
        $rootScope.editMode = true;

        setTimeout(function() {
            $('#story-content-editor').focus();
            updateScrollbar();
        }, 200);        

    }

    $scope.$on('doneEditing', function() {
        $scope.$broadcast('updateView', {
            markdown: $scope.context.story.content
        });
        $rootScope.editMode = false;
        updateScrollbar();
    });


    $scope.$on('selectTarget', function() {
        $rootScope.targetMode = true;
        $rootScope.targetStack = {
            source: {
                id: $scope.context.story.id,
                title: $scope.context.story.title
            },
            target: {
            }
        };
        $location.url('/query');
    });

    $scope.$on('findPaths', function() {
        $rootScope.targetStack.target.id = $scope.context.story.id;
        $rootScope.targetStack.target.title = $scope.context.story.title;
        $location.url('/graph/findpaths');
    });

    function updateScrollbar() {
        setTimeout(function() {
          $("#story-content").height($("#story-content").outerHeight());
          $("#story-content").mCustomScrollbar('update');
        }, 100);
    }

    $scope.suggestTags = function(query) {
        return ($http.get('/query/suggest/tags/' + query).then(function(result) {
            result.data = result.data.suggest[0].options.map(function(value) {
                return value.text
            });
            return result;
        }));
    }

    $scope.suggestSlots = function(query) {
        return ($http.get('/query/suggest/slots/' + query).then(function(result) {
            result.data = result.data.suggest[0].options.map(function(value) {
                return value.text
            });
            return result;
        }));
    }    

    /*
     * resize scrollbar for content on window-resize-event
     */
    $(window).resize(updateScrollbar);

    // reset filter
    $scope.storyFilter = {};
    $scope.slotFilterLeft = undefined;
    $scope.slotFilterRight = undefined;

    // init controller
    $scope.reload();

  });


/*
 * Controller for Slot-Name-Dialog
 */
angular.module('amanuensisApp')
  .controller('SlotNameModalCtrl', function ($scope,$rootScope,$location,utilService) {

    $scope.$on('askForSlotName',function(event, param) {
        $scope.title = 'Please give the new slot a name...';

        $scope.storyId = param.storyId;
        $scope.storyTitle = param.storyTitle;
        $scope.create = param.create;

        utilService.showModal('#slot-name-modal');
    });

    $scope.done = function() {
        if ($scope.newSlotName.length > 3) {
            $rootScope.stack = {
                storyId: $scope.storyId,
                storyTitle: $scope.storyTitle,
                slotName: $scope.newSlotName
            };
            // create a new story in a slot
            if ($scope.create) {
                $location.url('/story/' + $scope.newSlotName + '/' + $scope.storyId + '/' + $scope.storyTitle);
            }
            // link an existing story
            else {
                $rootScope.selectMode = true;
                $location.url('/query');
            }
            utilService.hideModal('#slot-name-modal');
        }
        else {
            $scope.title = 'Too short. Try a longer name for the slot...';
        }
    }

    $scope.cancel = function() {
        utilService.hideModal('#slot-name-modal');
    }

}); 

/*
 * Controller for Share-Dialog
 */
angular.module('amanuensisApp')
  .controller('ShareModalCtrl', function ($scope,$rootScope,$location,utilService,shareService,$http) {

    $scope.userRights = [
        {label: 'read', value: 'canRead'},
        {label: 'read & write', value: 'canWrite'},
        {label: 'read, write & grant', value: 'canGrant'},
    ];

    $scope.modes = [
        {label: 'user ', value: 'user', icon: 'fa-user'},
        {label: 'group', value: 'group', icon: 'fa-users'},
        {label: 'everybody', value: 'public', icon: 'fa-globe'},
    ];

    $scope.title = 'Share with somebody else...';

    $scope.$on('openShareStoryModal',function(event, param) {

        $scope.storyId = param.storyId;
    
        $scope.mode = 'user';

        $scope.userToShare = undefined;
        $scope.rightToShare = undefined;

        $scope.reloadShares();
    });

    $scope.reloadShares = function() {
        $scope.shares = shareService.listShares({storyId: $scope.storyId}, function(successData) {
            utilService.showModal('#share-modal');
        });
    }

    $scope.share = function() {
        if (angular.isUndefined($scope.mode)) {
            $scope.title = 'Please choose, how to share this story...';            
        }
        else if (angular.isDefined($scope.userToShare) && $scope.userToShare.length > 3 && angular.isDefined($scope.rightToShare)) {
            shareService.share({
                storyId: $scope.storyId,
                rights: $scope.rightToShare,
                user: $scope.userToShare
            }, {}, function(successData) {
                $scope.reloadShares();
                $scope.title = 'Share with somebody else...';
            });
        }
        else {
            if ($scope.mode === 'public') {
                $scope.title = 'Please choose the rights to grant to everybody else...';
            }
            else {
                $scope.title = 'Please enter a valid username and the rights to grant...';
            }
        }
    }

    $scope.unshare = function(userToShare) {
        shareService.unshare({
            storyId: $scope.storyId,
            user: userToShare
        }, {}, function(successData) {
            console.log("done unsharing");
            $scope.reloadShares();
        });
    }

    $scope.cancel = function() {
        utilService.hideModal('#share-modal');
    }

    $scope.editUser = function(login, access) {
        $scope.userToShare = login;
        $scope.rightToShare = access;        
        $scope.mode = 'user';
    }

    $scope.changeMode = function(value) {
        if (value === 'public' ) {
            $scope.userToShare = 'public';
        }
        else {
            $scope.userToShare = undefined;
        }
        $scope.rightsToShare = undefined;
    }

    //TODO: create a service for all suggests
    $scope.suggestUsersOrGroups = function(query) {
        return ($http.get('/query/suggest/' + $scope.mode + 's/' + query).then(function(result) {
            result.data = result.data.suggest[0].options.map(function(value) {
                return value.text
            });
            return result;
        }));
    }


}); 