function displayWordcloud(data){
    am4core.useTheme(am4themes_animated);

    if(chart)
        chart.dispose();
    document.getElementById("wordcloud").style.display = "block";
    chart = am4core.create("wordcloud", am4plugins_wordCloud.WordCloud);
    var series = chart.series.push(new am4plugins_wordCloud.WordCloudSeries());

    series.accuracy = 1;
    series.step = 30;
    series.rotationThreshold = 1;
    series.maxCount = 300;
    series.minWordLength = 2;
    series.labels.template.margin(4,4,4,4);
    series.maxFontSize = am4core.percent(30);
    series.randomness = 0.4

    let scalar = data.collocations[0].value.ll / 100;

    let text = "";
    for(let i = 0; i < data.collocations.length; i++){
        for(let j = 0; j < data.collocations[i].value.ll/scalar; j++){
            text += data.collocations[i].key;
            text += " ";
        }
    }
    series.text = text;

    series.colors = new am4core.ColorSet();
    series.colors.passOptions = {}; // makes it loop

    series.angles = [0];
    series.fontWeight = "600";

    showViz();

}