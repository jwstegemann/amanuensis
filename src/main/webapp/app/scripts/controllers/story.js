'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService,slotService,favourService,$rootScope,$location,$window,utilService,growl,queryService,$http,gettextCatalog) {

    function hasSlot(name, slots) {
        var i = slots.length;
        while(i--) {
            if (slots[i].name === name) return true;
        }
        return false;
    }

    function setPristine() {
        $scope.storyForm.$setPristine();
        $scope.storyForm.icon = {
            $dirty: false
        };
    }

    $scope.icons = icons;

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

                $rootScope.editMode = false;
                $rootScope.storyFlags = successData.flags;
                $rootScope.storyFlags.saved = 1;

                //open up slot and story-list by queryParameter
                if (!$window.matchMedia('(max-width: 800px)').matches) {
                    var slotToOpen = $location.search()['openSlot']
                    var inbound = angular.isDefined($location.search()['inbound'])

                    //check, of slot is available in the given direction
                    if (angular.isDefined(slotToOpen)
                        && (
                            (inbound && hasSlot(slotToOpen, successData.inSlots)) 
                            || (!inbound && hasSlot(slotToOpen, successData.outSlots))
                        )
                    ) {
                        $scope.selectSlot(slotToOpen, inbound);
                    }
                }

                setPristine();

                // set window title to loaded story
                $window.document.title = 'Colibri - ' + $scope.context.story.title;

            }, function(errorData) {
                // goto query-page when it is not possible to load the story!
                setPristine();
                $location.url('/query').replace();                
            });
        } 
        else {
        	// init empty StoryContext
        	$scope.context = { 
        		story: {
        			id: undefined,
        			title: undefined,
        			content: undefined,
                    created: gettextCatalog.getString("not yet"),
                    createdBy: gettextCatalog.getString("who knows"),
                    modified: gettextCatalog.getString("not yet"),
                    modifiedBy: gettextCatalog.getString("who knows"),
                    tags: [],
                    icon: "fa-bookmark"
        		},
        		inSlots: [],
        		outSlots: [],
                flags: {
                    canWrite: 1,
                    stars: 0,
                    saved: 0
                }
        	};

            setTimeout(function() {
              $('#story-title-input').focus();
            }, 200);

            $rootScope.appState = 1;

            $rootScope.editMode = true;

            $rootScope.storyFlags = {
                canWrite: 1, 
                stars: 0, 
                saved: 0
            };

            if (angular.isDefined($scope.storyForm)) {
                setPristine();
            }

            // set window title
            $window.document.title = gettextCatalog.getString('Colibri - new Story');            
        }

        $scope.storyFilter = {};
        $scope.slotFilterRight = undefined;
        $scope.slotFilterLeft = undefined;
    }

    $scope.$on('reloadStory', function() {
        $scope.reload();
    });

    $scope.$on('saveStory', function() {
        if (
            angular.isUndefined($scope.context.story.title) ||
            $scope.context.story.title.length < 3 ||
            angular.isUndefined($scope.context.story.content) ||
            $scope.context.story.content.length < 3
        ) {
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Sorry, but your story must have a title and a content. Please enter one...')});
        }
        else {
            // save existing story
        	if (angular.isDefined($scope.context.story.id)) {
        		storyService.update($scope.context.story, function(successData) {
                    setPristine();
                    $scope.context.story.modified = successData.version;
                    growl.addSuccessMessage($scope.context.story.title + gettextCatalog.getString(' has been saved.'));
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
                        setPristine();
                        $scope.context.story.id = successData.id;
                        $location.url("/story/" + $scope.context.story.id).replace();
                        growl.addSuccessMessage($scope.context.story.title + gettextCatalog.getString(' has been created in Slot ') + $routeParams.slotName + gettextCatalog.getString(' at Story ') + $routeParams.fromStoryTitle);
                        $rootScope.storyFlags.saved = 1;
                    });

                    $rootScope.stack = undefined;

                }                    
                // on its own
                else {
            		storyService.create($scope.context.story, function(successData) {
                        setPristine();
            			$scope.context.story.id = successData.id;
                        $location.url("/story/" + $scope.context.story.id).replace();
                        growl.addSuccessMessage($scope.context.story.title + gettextCatalog.getString(' has been created.'));
                        $rootScope.storyFlags.saved = 1;                        
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
            growl.addSuccessMessage(oldTitle + gettextCatalog.getString(' has been deleted.'));    
            $window.history.back();
            utilService.hideModal('#confirm-delete-modal');    
        });
    }

    $scope.cancelConfirmDelete = function() {
        utilService.hideModal('#confirm-delete-modal');    
    }

    /*
     * Choose icon for the story
     */
    $scope.chooseIcon = function() {
        if ($rootScope.editMode) {
            utilService.showModal('#choose-icon-modal'); 
        }
    }

    $scope.selectIcon = function(icon) {
        if (icon !== $scope.context.story.icon) {
            $scope.storyForm.icon = {
                $dirty: true
            }
        }
        $scope.context.story.icon = icon;
        utilService.hideModal('#choose-icon-modal');
    }

    $scope.cancelChooseIcon = function() {
        utilService.hideModal('#choose-icon-modal');    
    }

    /*
     * Star & Unstar Stories
     */
    $scope.$on('starStory', function() {
        if (angular.isUndefined($scope.context.story.id)) {
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Unfortunately you cannot star an unsaved story.')});
        }      
        else {  
        favourService.star({storyId: $scope.context.story.id}, null, function(successData) {
                growl.addSuccessMessage(gettextCatalog.getString('You just starred ') + $scope.context.story.title);
                $rootScope.storyFlags.stars = 1;
            });
        }
    });

    $scope.$on('unstarStory', function() {
        if (angular.isUndefined($scope.context.story.id)) {
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Unfortunately you cannot unstar an unsaved story.')});
        }      
        else {  
        favourService.unstar({storyId: $scope.context.story.id}, null, function(successData) {
                growl.addSuccessMessage(gettextCatalog.getString('You have removed your star from ') + $scope.context.story.title);
                $rootScope.storyFlags.stars = 0;
            });
        }
    });

    /*
     * Update due
     */

    $scope.updateDue = function() {
        if (angular.isUndefined($scope.context.story.due) || $scope.context.story.due === null) {
            favourService.undue({storyId: $scope.context.story.id}, null, function(successData) {
                    growl.addSuccessMessage(gettextCatalog.getString('You have removed ') + $scope.context.story.title + gettextCatalog.getString(' from your ToDo-list.'));
                });
        }
        else {
            favourService.due({storyId: $scope.context.story.id, date: $scope.context.story.due.toJSON()}, null, function(successData) {
                    growl.addSuccessMessage(gettextCatalog.getString('You have added ') + $scope.context.story.title + gettextCatalog.getString(' to your ToDo-list.'));
                });
        }
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
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('You cannot share a story that has not been saved yet.')});            
        }
    });

    /*
     * Add a Story to a Slot
     */
    $scope.$on('addStoryToSlot', function() {
        if (angular.isUndefined($scope.context.story.id)) {
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Unfortunately you cannot add an unsaved story to a slot.')});
        }
        else if ($scope.context.story.id === $rootScope.stack.storyId) {
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Unfortunately it is not possible to link a story to itself.')});                        
        }
        else {
            slotService.add({
                toStoryId: $rootScope.stack.storyId,
                slotName: $rootScope.stack.slotName,
                storyId: $scope.context.story.id
            }, null, function (successData) {
                $rootScope.selectMode = false;
                growl.addSuccessMessage($scope.context.story.title + gettextCatalog.getString(' has been linked to ') + $rootScope.stack.storyTitle + gettextCatalog.getString(' as ') + $rootScope.stack.slotName); 
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
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('I am sorry, but you cannot create a new slot on an unsaved story.')});            
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
            $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Excuse me, but you cannot create a new story in a slot of an unsaved story.')});
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
                inbound: inbound,
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
        console.log("Bin hier");
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


    $scope.$on('hideInStories', $scope.hideInStories);
    $scope.$on('hideInSlots', $scope.hideInSlots);
    $scope.$on('hideOutStories', $scope.hideOutStories);
    $scope.$on('hideOutSlots', $scope.hideOutSlots);

    /*
     * Preview and Editor
     */

//    $scope.showContentEditor = function() {
    $scope.$on('startEditing', function() {
        if ($scope.context.flags.canWrite && !$rootScope.selectMode && !$rootScope.targetMode) {
            $rootScope.editMode = true;

            setTimeout(function() {
                $rootScope.$broadcast('elastic:adjust');
                $('#story-content-editor').focus();
            }, 200);        
        }
    });

    $scope.$on('doneEditing', function() {
        $scope.$broadcast('updateView', {
            markdown: $scope.context.story.content
        });
        $rootScope.editMode = false;
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
     * prohibit navigation when dirty
     */

//    $scope.parser = document.createElement('a');
    $scope.$on('$locationChangeStart', function (event, newUrl, oldUrl) {
        if (newUrl === $scope.newUrl) {
            $scope.newUrl = undefined;
        }
        else if (($scope.storyForm.title.$dirty || $scope.storyForm.tags.$dirty || $scope.storyForm.content.$dirty || $scope.storyForm.icon.$dirty) 
            && newUrl.match(/^[^\?]+/)[0] !== oldUrl.match(/^[^\?]+/)[0]) {
                event.preventDefault(); // This prevents the navigation from happening
                $scope.newUrl = newUrl;
                utilService.showModal('#confirm-leave-modal');
        }
    });

    $scope.leaveStoryConfirmed = function() {
        console.log("going to: " + $scope.newUrl);
        $location.url($location.url($scope.newUrl).hash());
        //$rootScope.$apply();
        utilService.hideModal('#confirm-leave-modal');        
    }

    $scope.cancelConfirmLeave = function() {
        $scope.newUrl = undefined;
        utilService.hideModal('#confirm-leave-modal');    
    }

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
  .controller('SlotNameModalCtrl', function ($scope,$rootScope,$location,utilService,gettextCatalog) {

    $scope.$on('askForSlotName',function(event, param) {
        $scope.title = gettextCatalog.getString('Please give the new slot a name...');

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
            $scope.title = gettextCatalog.getString('Too short. Try a longer name for the slot...');
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
  .controller('ShareModalCtrl', function ($scope,$rootScope,$location,utilService,shareService,$http,gettextCatalog) {

    $scope.userRights = [
        {label: gettextCatalog.getString('read'), value: 'canRead'},
        {label: gettextCatalog.getString('read & write'), value: 'canWrite'},
        {label: gettextCatalog.getString('read, write & grant'), value: 'canGrant'},
    ];

    $scope.modes = [
        {label: gettextCatalog.getString('user '), value: 'user', icon: 'fa-user'},
        {label: gettextCatalog.getString('group'), value: 'group', icon: 'fa-users'},
        {label: gettextCatalog.getString('everybody'), value: 'public', icon: 'fa-globe'},
    ];

    $scope.title = gettextCatalog.getString('Share with somebody else...');

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
            $scope.title = gettextCatalog.getString('Please choose, how to share this story...');            
        }
        else if (angular.isDefined($scope.userToShare) && $scope.userToShare.length > 3 && angular.isDefined($scope.rightToShare)) {
            shareService.share({
                storyId: $scope.storyId,
                rights: $scope.rightToShare,
                user: $scope.userToShare
            }, {}, function(successData) {
                $scope.reloadShares();
                $scope.title = gettextCatalog.getString('Share with somebody else...');
            });
        }
        else {
            if ($scope.mode === 'public') {
                $scope.title = gettextCatalog.getString('Please choose the rights to grant to everybody else...');
            }
            else {
                $scope.title = gettextCatalog.getString('Please enter a valid username and the rights to grant...');
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