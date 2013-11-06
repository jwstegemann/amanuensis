directive('klaraTable', function() {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: { tableControl: { filter: undefined, predicate: '@sort', reverse: false },
                tableModel: '=model',
                sort: function(attribute) {
                  if (attribute == $scope.schuelerTable.predicate) {
                    $scope.schuelerTable.reverse = !$scope.schuelerTable.reverse;
                  }
                  else {
                    $scope.schuelerTable.predicate = attribute;
                    $scope.schuelerTable.reverse = false;
                  }
                },
                selectAll: function() {
                    angular.forEach($scope.schuelerList, function(item, index) {
                        item.selected = $scope.schuelerTable.selectAll;
                    });
                }
        },
        templateUrl: "templates/klaraTable.html",
        link: function(scope, element, attrs) {
            /*
             * handling col-resizing
             

            var pressed = false;
            var start = undefined;
            var startX, startWidth;

            // prevent sorting when resizing cols
            $("#" + attrs.id + " thead tr th .sizehandler").click(function(e) {
              e.stopImmediatePropagation()
            });
            
            $("#" + attrs.id + " thead tr th .sizehandler").mousedown(function(e) {
                start = $(this).parent();
                pressed = true;
                startX = e.pageX;
                startWidth = start.width();

                e.preventDefault();

                $(start.parent().mousemove(function(e) {
                  if(pressed) {
                      $(start).width(startWidth+(e.pageX-startX));
                      e.preventDefault();
                  }
                }));
            });

            $(document).mouseup(function(e) {
                e.preventDefault();

                if(pressed) {
                    pressed = false;
                    start.parent().off('mousemove');
                }
            });
*/
        }
    }
})




















<table class="table table-condensed table-bordered table-hover table-striped">
    <thead>
        <tr>
            <th colspan="4" class="toolbar">
                <form class="form-search pull-left">
                    <div class="input-append">
                        <input type="text" class="search-query input-large" placeholder="Filter" ng-model="tableControl.filter">
                        <button class="btn"><i class="icon-search icon-large" /></button>
                    </div>
                </form>

                <div class="pull-right">
                    <div class="btn-group">
                        <a class="btn" href="#/schueler/create"><i class="icon-plus-sign icon-large" /></a>
<!--                            <button class="btn" ng-click="deleteSelected()"><i class="icon-trash icon-large" /></button>
                        <button class="btn" ng-click="reload()"><i class="icon-refresh icon-large" /></button>
                        <button class="btn"><i class="icon-cog icon-large" /></button> -->
                    </div>
                </div>
        </th>
        <tr>
            <!--<th class="select"><input type="checkbox" id="select_all" ng-model="tableControl.selectAll" ng-change="selectAll()" /></td></th>
            <th ng-click="sort('name')">Name <i ng-show="tableControl.predicate == 'name'" ng-class="tableControl.reverse?'icon-angle-up':'icon-angle-down'" /><div class="sizehandler">&nbsp;</div></th>
            <th ng-click="sort('vorname')">Vorname  <i ng-show="tableControl.predicate == 'vorname'" ng-class="tableControl.reverse?'icon-angle-up':'icon-angle-down'" /><div class="sizehandler">&nbsp;</div></th>
            <th ng-click="sort('geschlecht')">Geschlecht  <i ng-show="tableControl.predicate == 'geschlecht'" ng-class="tableControl.reverse?'icon-angle-up':'icon-angle-down'" /></th> 
        -->

            <th>Name</th>
            <th>Vorname</th>
            <th>Geschlecht</th>

        </tr>
    </thead>
    <tbody>
        <tr ng-repeat="schueler in tableModel | filter: tableControl.filter | orderBy:tableControl.predicate:tableControl.reverse" ng-class="{selected: schueler.selected}">
<!--                <td class="select"><input type="checkbox" id="{{schueler._id}}" ng-model="schueler.selected" /></td> -->
            <td><a href="#schueler/detail/{{schueler._id}}">{{schueler.name}}</a></td>
            <td>{{schueler.vorname}}</td>
            <td>{{schueler.geschlecht}}</td>
        </tr>
    </tbody>
</table>