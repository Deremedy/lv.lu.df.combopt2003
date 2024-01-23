var map = L.map('map').setView([56.958141067669146, 24.12462850613549], 14);
const colors = ["#f44336","#e81e63","#9c27b0","#673ab7","#3f51b5","#2196f3","#03a9f4","#00bcd4","#009688",
                                                "#4caf50","#8bc34a","#cddc39","#ffeb3b","#ffc107","#ff9800","#ff5722"];
const vehicleIcon = L.divIcon({
    html: '<i class="fas fa-truck"></i>'
});
const pickupIcon = L.divIcon({
    html: '<i class="fas fa-2x fa-building"></i>'
});
const dotIcon = L.divIcon({
    html: '<i class="far fa-dot-circle"></i>'
});
const oneWayIcon = L.divIcon({
    html: '<i class="fa-solid fa-arrow-right"></i>'
});
const redOneWayIcon = L.divIcon({
    html: '<i class="fa-solid fa-arrow-right" style="color:red"></i>'
});
const twoWayIcon = L.divIcon({
    html: '<i class="fa-solid fa-arrows-left-right"></i>'
});
const redTwoWayIcon = L.divIcon({
    html: '<i class="fa-solid fa-arrows-left-right" style="color:red"></i>'
});
$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);
    const solutionId = urlParams.get('id');
    var coordinates = [];
    var motionObject = null;

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        opacity: 0.6,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    $.getJSON("/routes/score?id=" + solutionId, function(analysis) {
                    var badge = "badge bg-danger";
                    if (getHardScore(analysis.score)==0) { badge = "badge bg-success"; }
                    $("#score_a").attr({"title":"Score Brakedown","data-bs-content":"" + getScorePopoverContent(analysis.constraints) + "","data-bs-html":"true"});
                    $("#score_text").text(analysis.score);
                    $("#score_text").attr({"class":badge});

                    $(function () {
                       $('[data-toggle="popover"]').popover()
                    })
    });

    $.getJSON("/routes/solution?id=" + solutionId, function(solution) {
            $.getJSON("/routes/indictments?id=" + solutionId, function(indictments) {
                            coordinates = renderRoutes(solution, indictments);
                            $(function () {
                              $('[data-toggle="popover"]').popover()
                            })
            })
        });

    let playButton = $("#play");
    playButton.click(function () {
        motionObject = L.motion.polyline(coordinates, {
            color: "indigo"
        }, {
            auto: true,
            removeOnEnd: true,
            duration: $("#duration").val(),
        }, {
            showMarker: true,
            icon: vehicleIcon
        }).addTo(map);
    });

    $("#durationValue").text($("#duration").val()/1000 + ' seconds');
    $("#duration").on("input", function () {
        $("#durationValue").text($("#duration").val()/1000 + ' seconds');
        if (motionObject != null) {
            motionObject.motionDuration($("#duration").val());
        }
    })
});

function renderRoutes(solution, indictments) {
    $("#solutionTitle").text("Version 22/Jan/2024 solutionId: " + solution.solutionId);

    var indictmentMap = {};
    indictments.forEach((indictment) => {
        indictmentMap[indictment.indictedObjectID] = indictment;
    })

    // Render edges to traverse
    solution.edgeList.forEach((edge) => {
        let startVertex = solution.vertexList.find((vertex) => vertex.id === edge.start);
        let endVertex = solution.vertexList.find((vertex) => vertex.id === edge.end);
        let startLocation = [startVertex.lat, startVertex.lon];
        let endLocation = [endVertex.lat, endVertex.lon];
        L.polyline([startLocation, endLocation], {color: 'red'}).addTo(map);
    });

    // Render edge markers
    let visitedEdges = new Set();
    solution.edgeList.forEach((edge) => {
        if (visitedEdges.has(edge.id)) {
            return;
        }
        let startVertex = solution.vertexList.find((vertex) => vertex.id === edge.start);
        let endVertex = solution.vertexList.find((vertex) => vertex.id === edge.end);
        let startLocation = [startVertex.lat, startVertex.lon];
        let endLocation = [endVertex.lat, endVertex.lon];

        const marker = L.marker([(startVertex.lat + endVertex.lat) / 2, (startVertex.lon + endVertex.lon) / 2]).addTo(map);

        let edgePopoverContent = "";
        marker.setIcon(oneWayIcon);
        edgePopoverContent += "<b>#"+edge.id+"</b><br>" +
            "Start: <b>#"+edge.start+"</b><br>" +
            "End: <b>#"+edge.end+"</b><br>";

        let oppositeEdge = solution.edgeList.find((e) => e.start === edge.end && e.end === edge.start);
        if (oppositeEdge) {
            edgePopoverContent += "<hr>" +
                "<b>#"+oppositeEdge.id+"</b><br>" +
                "Start: <b>#"+oppositeEdge.start+"</b><br>" +
                "End: <b>#"+oppositeEdge.end+"</b><br>";
            visitedEdges.add(oppositeEdge.id);
        }

        let step = solution.routeSteps.find((step) => step.startVertex === edge.start && step.endVertex === edge.end);
        let wasStepTravelled = true;
        if (step) {
            let indictmentId = 'RouteStep'+step.id;
            edgePopoverContent += "<hr>" +
                "Route step: <b>#"+step.id+"</b><br>" +
                "Start vertex: <b>#"+step.startVertex+"</b><br>" +
                "End vertex: <b>#"+step.endVertex+"</b><br>" +
                "Next step: <b>#"+step.nextStep+"</b><br>" +
                "<hr>" +
                getEntityPopoverContent(indictmentId, indictmentMap);
        } else {
            wasStepTravelled = false;
        }

        visitedEdges.add(edge.id);
        if (oppositeEdge) {
            let oppositeStep = solution.routeSteps.find((step) => step.startVertex === oppositeEdge.start && step.endVertex === oppositeEdge.end);
            let indictmentId = 'RouteStep'+oppositeStep.id;
            if (oppositeStep) {
                edgePopoverContent += "<hr>" +
                    "Route step: <b>#"+oppositeStep.id+"</b><br>" +
                    "Start vertex: <b>#"+oppositeStep.startVertex+"</b><br>" +
                    "End vertex: <b>#"+oppositeStep.endVertex+"</b><br>" +
                    "Next step: <b>#"+oppositeStep.nextStep+"</b><br>" +
                    "<hr>" +
                    getEntityPopoverContent(indictmentId, indictmentMap);
                marker.setIcon(twoWayIcon);
            } else {
                marker.setIcon(redTwoWayIcon);
            }
        } else {
            if (wasStepTravelled) {
                marker.setIcon(oneWayIcon);
            } else {
                marker.setIcon(redOneWayIcon);
            }
        }
        marker.bindPopup(edgePopoverContent);
    });

    // Render vertices markers
    solution.vertexList.forEach((vertex) => {
        const marker = L.marker([vertex.lat, vertex.lon]).addTo(map);
        marker.setIcon(dotIcon);
        marker.bindPopup("<b>#"+vertex.id+"</b><br>");
    });

    // Render step markers


    var step_counter = 1;
    var routeStartStep = solution.routeSteps.find((step) => step.start);
    let next_step = routeStartStep;
    let coordinates = [];

    let startVertex = solution.vertexList.find((vertex) => vertex.id === solution.startVertex);
    const vmarker = L.marker([startVertex.lat, startVertex.lon]).addTo(map);
    vmarker.setIcon(pickupIcon);

    while (next_step) {
        if (next_step.isActive) {
            let startVertex = solution.vertexList.find((vertex) => vertex.id === next_step.startVertex);
            let endVertex = solution.vertexList.find((vertex) => vertex.id === next_step.endVertex);
            let startLocation = [startVertex.lat, startVertex.lon];
            let endLocation = [endVertex.lat, endVertex.lon];
            coordinates.push(startLocation);
        }

        // const vmarker = L.marker(startLocation).addTo(map);
        // vmarker.setIcon(vehicleIcon);
        // const vcolor = getColor();
        // L.polyline([startLocation, endLocation], {color: vcolor}).addTo(map);

        next_step = solution.routeSteps.find((step) => step.id === next_step.nextStep);
        if (next_step.start) {
            next_step = null;
        }
        step_counter++;
    }

    return coordinates;
}

function getEntityPopoverContent(entityId, indictmentMap) {
    var popover_content = "";
    const indictment = indictmentMap[entityId];
    if (indictment != null) {
        popover_content = popover_content + "Total score: <b>" + indictment.score + "</b> (" + indictment.matchCount + ")<br>";
        indictment.constraintMatches.forEach((match) => {
                  if (getHardScore(match.score) == 0) {
                     popover_content = popover_content + match.constraintName + " : " + match.score + "<br>";
                  } else {
                     popover_content = popover_content + "<b>" + match.constraintName + " : " + match.score + "</b><br>";
                  }
            })
    }
    return popover_content;
}

function getScorePopoverContent(constraint_list) {
    var popover_content = "";
    constraint_list.forEach((constraint) => {
          if (getHardScore(constraint.score) == 0) {
             popover_content = popover_content + constraint.name + " : " + constraint.score + "<br>";
          } else {
             popover_content = popover_content + "<b>" + constraint.name + " : " + constraint.score + "</b><br>";
          }
    })
    return popover_content;
}

function getHardScore(score) {
   return score.slice(0,score.indexOf("hard"))
}

function getSoftScore(score) {
   return score.slice(score.indexOf("hard/"),score.indexOf("soft"))
}