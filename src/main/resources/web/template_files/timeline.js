var margin = {top: 10, right: 30, bottom: 30, left: 60},
    width = 800 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;

var svg;

var chart;

let lastChartData;
let lastQueryString;

function getTimeline(queryString) {
    var query = JSON.parse(queryString);
    query.result.type = "ngram";
    query.result.context = 0;
    query.result.n = 1;
    query.result.groupby = "date";
    query.result.async = false;
    var timelineDiv = document.getElementById("timeline");
    timelineDiv.style.display = "block";
    var loading = "<div class='spinner-border' role='status' style='width: 10rem; height: 10rem; margin-top: 50px'><span class=''sr-only></span></div>";
    timelineDiv.innerHTML = loading;
    $.ajax({
        type: "POST",
        url: "/" + corpus + "/query",
        data: JSON.stringify(query),
        processData: false,
        contentType: "application/json",
        success: function (data) {
            lastData = data;
            displayTimeline(data, jsonQueryToPrinatableString(query.query.tokens));
        },
        failure: function (errMsg) {
            handleError(errMsg);
        }
    });
}

function blankData(){
    let new_data = [];
    for(let i = 1803; i < 2020; i++){
        let d = new Date();
        d.setYear(i);
        new_data.push({
            date: d,
            freq: 0,
            last: 0
        });
    }
    return new_data;
}

function clone(obj){
    return JSON.parse(JSON.stringify(obj));
}

function updateData(chart_data, new_data){
    for(let i = 0; i < chart_data.length; i++){
        chart_data[i].last = chart_data[i].freq;
        chart_data[i].freq = 0;
    }
    for(let i = 0; i < new_data.ngrams.length; i++){
        let dateString = new_data.ngrams[i].key.replace(/_/g, '-');
        let year = parseInt(dateString.split('-')[0]);
        let index = year - 1803;
        let freq = new_data.ngrams[i].value;
        chart_data[index].freq = chart_data[index].freq + freq;
    }
}

function displayTimeline(data, queryString) {
    am4core.useTheme(am4themes_material);
    am4core.useTheme(am4themes_animated);

    let showLast = document.getElementById("hist-last").checked;

    if(chart)
        chart.dispose();

    chart = am4core.create("timeline", am4charts.XYChart);
    chart.paddingRight = 20;

    if(typeof lastChartData === "undefined")
        lastChartData = blankData();

    chart.data = clone(lastChartData);

    updateData(chart.data, data);

    lastChartData = clone(chart.data);

    var dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    dateAxis.baseInterval = {
        "timeUnit": "year",
        "count": 1
    };
    dateAxis.tooltipDateFormat = "YYYY";

    var valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
    valueAxis.tooltip.disabled = true;
    valueAxis.title.text = "Frequency";

    var series = chart.series.push(new am4charts.LineSeries());
    series.dataFields.dateX = "date";
    series.dataFields.valueY = "freq";
    series.tooltipText = queryString + " [bold]{valueY}[/]";

    if(showLast && (typeof lastQueryString !== "undefined")){
        var series = chart.series.push(new am4charts.LineSeries());
        series.dataFields.dateX = "date";
        series.dataFields.valueY = "last";
        series.tooltipText = lastQueryString + " [bold]{valueY}[/]";
    }

    chart.cursor = new am4charts.XYCursor();
    chart.cursor.lineY.opacity = 0;
    chart.scrollbarX = new am4charts.XYChartScrollbar();
    chart.scrollbarX.series.push(series);

    lastQueryString = queryString;

    showViz();
}
