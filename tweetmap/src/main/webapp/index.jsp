<!DOCTYPE html>
<html>
<head>
    <style type="text/css">
    </style>
    <script src="http://maps.googleapis.com/maps/api/js?key=AIzaSyBTFest0kbG-xe7_EFh-ZHZviRZ3TeMh1A&libraries=visualization"></script>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
    <!-- Latest compiled and minified JavaScript -->
    <script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    <script>
        //america center
        var center = new google.maps.LatLng(38.572643,-96.5236265);
        var heat_map_data = new google.maps.MVCArray([]);
        var map;
        var heatmap;
        var tweetData;
        var ws;
        var realTimeInfo = [];
        var markers = [];
        //realTimeInfo[idx][0] is context
        //realTimeInfo[idx][1] is string of author place time
        function generate() {
            //clean first
            $("#realtime").html("");
            for (idx in realTimeInfo) {
                var blockquote = $("<blockquote/>");
                $("<p/>").html(realTimeInfo[idx][0]).appendTo(blockquote);
                $("<footer/>").html(realTimeInfo[idx][1]).appendTo(blockquote);
                blockquote.appendTo($("#realtime"));
            }
        }
        function removeMarker() {
            for (idx in markers) {
                markers[idx].setMap(null);
            }
            markers = [];
        }
        function overview() {
            //init overview tab
            if (ws != undefined) {
                ws.close();
            }
            removeMarker();
            heatmap.setMap(null);
            $("#category").show();
            $("#realtime").hide();
            $("#trend").show();
        }
        function getdata(category) {
            heat_map_data = new google.maps.MVCArray([]);
            heatmap.setMap(null);
            alert("begin to get data from RDS");
            $.get("getdata?type=" + category, function (data,status) {
                tweetData = data;
                alert("begin to rendering the map");
                for (idx in tweetData) {
                    var item = tweetData[idx];
                    heat_map_data.push({location: new google.maps.LatLng(parseFloat(item.lat), parseFloat(item.lon)), weight: 3});
                }
                heatmap = new google.maps.visualization.HeatmapLayer({
                    data: heat_map_data
                });
                heatmap.setMap(map);
                alert("rendering done");
            } );
        }
        function wssend() {
            if (ws.readyState == ws.OPEN) {
                ws.send("get");
            }
        }
        function realtime() {
            $("#category").hide();
            $("#realtime").show();
            $("#trend").hide();
            //init realtime
            heatmap.setMap(null);
            heat_map_data = new google.maps.MVCArray([]);
            heatmap = new google.maps.visualization.HeatmapLayer({
                data: heat_map_data
            });
            heatmap.setMap(map);
            realTimeInfo = [];
            generate();
            //websocket server
            ws = new WebSocket("ws://54.209.200.143:8080/tweetmap/push");
            //window.setInterval(wssend, 4000);
            ws.onopen = function()
            {
                // Web Socket is connected, send data using send()
                wssend();
            };

            ws.onmessage = function (evt)
            {
                var received_msg = JSON.parse(evt.data);
                var lat = parseFloat(received_msg.lat);
                var lon = parseFloat(received_msg.lon);
                var string = received_msg["username"] + " " + received_msg["lat"] + " " +
                                received_msg["lon"];
                realTimeInfo.push([received_msg["text"], string]);
                if (realTimeInfo.length > 4) {
                    realTimeInfo.shift();
                }
                heat_map_data.push({
                    location: new google.maps.LatLng(parseFloat(received_msg["lat"]),
                            parseFloat(received_msg["lon"])),
                    weight: 3
                });
                generate();
            };

            ws.onclose = function()
            {
                // websocket is closed.
            };
        }
        function initialize() {
            //init raw map
            map = new google.maps.Map(document.getElementById('googleMap'), {
                center: center,
                zoom: 4
            });
            heatmap = new google.maps.visualization.HeatmapLayer({
                data: heat_map_data
            });
            heatmap.setMap(map);
        }
        google.maps.event.addDomListener(window, 'load', initialize);
        function init() {
            //click function
            $("#food").click(function () {
                getdata("food");
            });
            $("#music").click(function () {
                getdata("music");
            });
            $("#sport").click(function () {
                getdata("sport");
            });
        }
        function showtrend() {
            alert("begin get trend");
            $("#trendtxt").html("");
            $.get("showtrend", function (data,status) {
                console.log(data);
                var keywordList = JSON.parse(data);
                var list = $("<ul/>");
                for (idx in keywordList) {
                    $("<li/>").text(keywordList[idx]).appendTo(list);
                }
                list.appendTo($("#trendtxt"));
            } );
        }
    </script>
</head>

<body onload="init()">
<div class="row">
    <div class="col-md-3" id="view_choice">
        <div class="btn-group" role="group" aria-label="...">
            <button type="button" class="btn btn-default" onclick="overview()">Overview</button>
            <button type="button" class="btn btn-default" onclick="realtime()">Real-Time</button>
        </div>
        <div id="category">
            <div><h3>Choose Category to be shown in Map</h3></div>
            <div class="dropdown">
                <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    Category
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
                    <li><a id="food" href="#">Food</a></li>
                    <li><a id="music" href="#">Music</a></li>
                    <li><a id="sport" href="#">Sport</a></li>
                </ul>
            </div>
        </div>
        <div id="trend">
            <button class="btn btn-default" onclick="showtrend()">showTrend</button>
            <div id="trendtxt">

            </div>
        </div>
        <div id="realtime" style="display: none">
        </div>
    </div>
    <div class="col-md-9">
        <div id="googleMap" style="width:1000px;height:770px;"></div>
    </div>
</div>
</body>
</html>