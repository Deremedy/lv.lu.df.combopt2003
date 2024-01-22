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

function getHardScore(score) {
   return score.slice(0,score.indexOf("hard"))
}

function getSoftScore(score) {
   return score.slice(score.indexOf("hard/"),score.indexOf("soft"))
}

$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);
    const solutionId = urlParams.get('id');

    $.getJSON("/routes/score?id=" + solutionId, function(analysis) {
            var badge = "badge bg-danger";
            if (getHardScore(analysis.score)==0) { badge = "badge bg-success"; }
            $("#score_a").attr({"title":"Score Brakedown","data-bs-content":"" + getScorePopoverContent(analysis.constraints) + "","data-bs-html":"true"});
            $("#score_text").text(analysis.score);
            $("#score_text").attr({"class":badge});
    });

    $.getJSON("/routes/solution?id=" + solutionId, function(solution) {
        $.getJSON("/routes/indictments?id=" + solutionId, function(indictments) {
                        renderRoutes(solution, indictments);
                        $(function () {
                          $('[data-toggle="popover"]').popover()
                        })
        })
    });

});

function renderRoutes(solution, indictments) {
    var indictmentMap = {};
    indictments.forEach((indictment) => {
         indictmentMap[indictment.indictedObjectID] = indictment;
    })

    const steps_div = $("#route_steps_container");
    var step_counter = 1;
    var is_route_step = true;
    // Get first step in the route
    var routeStartStep = solution.routeSteps.find((step) => step.start);
    let next_step = routeStartStep;

    while (next_step) {
        var step_badge = "badge bg-danger";
        var routeIndictmentId = 'RouteStep'+next_step.id;
        if (indictmentMap[routeIndictmentId]==null || getHardScore(indictmentMap[routeIndictmentId].score)==0) { step_badge = "badge bg-success"; }
        if (!next_step.isActive) { step_badge = "badge bg-secondary"; }
        steps_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
        'nextStep=' + next_step.nextStep + '</br>' +
        'startVertex=' + next_step.startVertex + '</br>' +
        'endVertex=' + next_step.endVertex + '</br>' +
        '<hr>' +
        getEntityPopoverContent(routeIndictmentId, indictmentMap) +
        '" data-bs-original-title="' + '#' + step_counter + " " + routeIndictmentId + ' (' + next_step.isActive + ')' +'"><span class="'+ step_badge +'">'+
            routeIndictmentId + ' (' + next_step.isActive + ')' +'</span></a>'));
        next_step = solution.routeSteps.find((step) => step.id === next_step.nextStep);

        if (next_step.start) {
            next_step = null;
        }
        step_counter++;
    }
    // solution.routeSteps.forEach((routeStep) => {
    //     var next_step = routeSteps.find((step) => step.id === routeStep.nextStep);
    //     var step_badge = "badge bg-danger";
    //     var routeIndictmentId = 'RouteStep'+routeStep.id;
    //     if (indictmentMap[routeIndictmentId]==null || getHardScore(indictmentMap[routeIndictmentId].score)==0) { step_badge = "badge bg-success"; }
    //     if (!routeStep.isActive) { step_badge = "badge bg-secondary"; }
    //     steps_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
    //     'startVertex=' + routeStep.startVertex + '</br>' +
    //     'endVertex=' + routeStep.endVertex + '</br>' +
    //     '<hr>' +
    //     getEntityPopoverContent(routeIndictmentId, indictmentMap) +
    //     '" data-bs-original-title="' + '#' + step_counter + " " + routeIndictmentId + ' (' + routeStep.isActive + ')' +'"><span class="'+ step_badge +'">'+
    //         routeIndictmentId + ' (' + routeStep.isActive + ')' +'</span></a>'));
    // })

    const edges_div = $("#edges_container");
    var edge_counter = 1;
    solution.edgeList.forEach((edge) => {
        var step_badge = "badge bg-danger";
        var edgeIndictmentId = 'Edge'+edge.id;
        if (indictmentMap[edgeIndictmentId]==null || getHardScore(indictmentMap[edgeIndictmentId].score)==0) { step_badge = "badge bg-success"; }
        steps_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
            'startVertex=' + edge.start + '</br>' +
            'endVertex=' + edge.edge + '</br>' +
            'weight=' + edge.weight + '</br>' +
            '<hr>' +
            getEntityPopoverContent(edgeIndictmentId, indictmentMap) +
            '" data-bs-original-title="' + '#' + edge_counter + " " + edgeIndictmentId +'"><span class="'+ step_badge +'">'+
            edgeIndictmentId +'</span></a>'));
    })
}



