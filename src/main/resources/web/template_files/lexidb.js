query = {
    "query": {
        "tokens": "{\"token\": \"of\"}"
    },
    "result": {
        "page": "0"
    }
}

var corpus = "";

var lastJsonQuery = "";

var concs = null;

var lastQuery = "";
var lastText = "";

var lastData;

var coloring = "pos";

var context = 5;

let newQuery = true;

let allSortOptions = [];

function formatNumber(num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,')
}

function updateHighlighting(column) {
    if (column != coloring) {
        $('.pos-check').toggle();
        $('.sem-check').toggle();
    }
    coloring = column;
}

function showHide(div, link) {
    var divTohide = document.getElementById(div);
    if (divTohide.style.maxHeight) {
        divTohide.style.maxHeight = null;
    } else {
        divTohide.style.maxHeight = divTohide.scrollHeight + "px";
    }
    link.firstChild.classList.toggle('fa-minus');
    link.firstChild.classList.toggle('fa-plus');
}

function showViz(){
    var divTohide = document.getElementById('viz-body');
    var link = document.getElementById('vizToggle');
    divTohide.style.maxHeight = divTohide.scrollHeight + "px";
    link.firstChild.classList.add('fa-minus');
    link.firstChild.classList.remove('fa-plus');
}

function jsonQueryToPrinatableString(queryString){
    let newString = queryString.replace(/}/g, " ");
    newString = newString.replace(/"\w+":/g, "");
    newString = newString.replace(/["{]/g, "");
    newString = newString.replace(/,/g, ":");
    return newString;
}

function hideViz(){
    var divTohide = document.getElementById('viz-body');
    var link = document.getElementById('vizToggle');
    divTohide.style.maxHeight = null;
    link.firstChild.classList.remove('fa-minus');
    link.firstChild.classList.add('fa-plus');
}

function updateCorpusInfo() {
    $.ajax({
        type: "GET",
        url: "/" + corpus + "/size",
        data: JSON.stringify(query),
        processData: false,
        contentType: "application/json",
        success: function (data) {
            displayCorpusInfo(data);
        },
        failure: function (errMsg) {
            handleError(errMsg);
        }
    });
}

function displayCorpusInfo(data) {
    //document.getElementById("corpusNameInfo").innerHTML = corpus;
    document.getElementById("tokenCountInfo").innerHTML = formatNumber(data.count);
    document.getElementById("fileCountInfo").innerHTML = formatNumber(data.types.file);
}

function runQuery(page, updateViz, timeout=20) {
    if(timeout > 500)
        timeout = 500;
    newQuery = (timeout == 20);
    if (updateViz) {
        document.getElementById("timeline").style.display = "none";
        document.getElementById("wordcloud").style.display = "none";
        document.getElementById("viz").style.display = "none";
    }
    updateQuery(page);
    query.result.sort = getSortOptions();
    query.result.async = true;
    document.getElementById("viz").style.display = "none";
    //closeNav();
    lastJsonQuery = JSON.stringify(query);
    $.ajax({
        type: "POST",
        url: "/" + corpus + "/query",
        data: JSON.stringify(query),
        processData: false,
        contentType: "application/json",
        success: function (data) {
            lastData = data;
            displayResults(data, updateViz);

            setTimeout(function(){
                if(data.blockQueried != data.totalBlocks | (data.hasOwnProperty('concordances') && !data.sorted)) {
                    runQuery(page, updateViz, timeout*4);
                }
            }, timeout);
        },
        failure: function (errMsg) {
            handleError(errMsg);
        }
    });
}

function updateQuery(page) {
    var queryString = document.getElementById("queryString").value;

    if(!queryString.includes('{\"') && queryString){
        let bits = queryString.split(" ");
        let open = "{\"token\":\"";
        let close = "\"}"
        let newQueryString = "";
        for(let i = 0; i < bits.length; i++){
            newQueryString += open;
            newQueryString += bits[i];
            newQueryString += close;
        }
        queryString = newQueryString;
    }

    queryString = queryString.replace(/\s/g, '');
    document.getElementById("queryString").value = queryString;
    paramvalue = $("#queryString").val();
    query.query.tokens = paramvalue;
    speakervalue = $("#speaker").val();
    datevalue = $("#date").val();
    if (speakervalue && speakervalue !== "") {
        query.query.tokens = query.query.tokens.replace("}", ", \"speaker\":\"" + speakervalue + "\"}")
    }
    if (datevalue && datevalue !== "") {
        query.query.tokens = query.query.tokens.replace("}", ", \"date\":\"" + datevalue + "\"}")
    }
    query.result.page = page;

    if (document.getElementById("kwic-tab").classList.contains("active")) {
        query.result.type = "kwic";
        if (document.getElementById("context").value.length > 0) {
            context = parseInt(document.getElementById("context").value);
            query.result.context = context;
        }
        if (document.getElementById("resultsPerPage").value.length > 0) {
            rpp = parseInt(document.getElementById("resultsPerPage").value);
            query.result.pageSize = rpp
        }
    }
    if (document.getElementById("ngram-tab").classList.contains("active")) {
        query.result.type = "ngram";
        if (document.getElementById("n").value.length > 0) {
            var n = parseInt(document.getElementById("n").value);
            query.result.n = n;
        }
        if (document.getElementById("ngram-context").value.length > 0) {
            context = parseInt(document.getElementById("ngram-context").value);
            query.result.context = context;
        }
        if (document.getElementById("groupby").value.length > 0) {
            var groupby = document.getElementById("groupby").value;
            query.result.groupby = groupby;
        }
    }
    if (document.getElementById("list-tab").classList.contains("active")) {
        query.result.type = "list";
        if (document.getElementById("groupby-list").value.length > 0) {
            var groupby = document.getElementById("groupby-list").value;
            query.result.groupby = groupby;
            query.result.context = 0;
        }
    }
    if (document.getElementById("col-tab").classList.contains("active")) {
        var coltype = document.getElementById("col-type").value;
        query.result.type = coltype;
        query.result.context = document.getElementById("col-context").value;
        if (document.getElementById("topXResults").value.length > 0) {
            rpp = parseInt(document.getElementById("topXResults").value);
            query.result.pageSize = rpp
        }
    }
}

function loadingResults(data, text){
    let sorted = (data.sorted | !data.hasOwnProperty('concordances'))? 1 : 0;
    let percent = ((data.blockQueried + sorted) / (data.totalBlocks + 1)).toFixed(2);
    let header = document.getElementById("content-title");
    let infoText = document.createTextNode(text);
    if(newQuery)
        header.innerHTML = "";
    if(percent != 1.00 && header.childElementCount == 0){
        let spinnerDiv = document.createElement("div");
        spinnerDiv.className = "spinner-border spinner-border-sm";
        spinnerDiv.setAttribute("role", "status");
        let spinnerSpan = document.createElement("span");
        spinnerSpan.className = "sr-only";
        let spinnerText = document.createTextNode("Loading...");
        spinnerSpan.appendChild(spinnerText);
        spinnerDiv.appendChild(spinnerSpan);
        header.appendChild(spinnerDiv);

        let percentSpan = document.createElement("span");
        percentSpan.id = "percentSpan";
        header.appendChild(percentSpan);

        let infoSpan = document.createElement("span");
        infoSpan.id = "infoSpan";
        infoSpan.style = "color: LightGrey;";
        header.appendChild(infoSpan);
    }
    if(percent != 1.00 && percent != 0.00){
        let percentSpan = document.getElementById("percentSpan");
        let p = percent * 100;
        p = p.toFixed(0);
        let percentText = document.createTextNode(" " + p + "% ");
        percentSpan.innerHTML = "";
        percentSpan.appendChild(percentText);
        let infoSpan = document.getElementById("infoSpan");
        infoSpan.innerHTML = "";
        infoSpan.appendChild(infoText);
    }
    if(percent == 1.00){
        infoText = (data.resultCount > 0) ? infoText : document.createTextNode("No results found");
        header.innerHTML = "";
        header.appendChild(infoText);
    }
}

function displayConcordances(data, updateViz) {
    if (updateViz && data.resultCount > 0 && data.blockQueried == data.totalBlocks) {
        getTimeline(lastJsonQuery);
    }

    concs = data.concordances;
    var results = document.createDocumentFragment();

    var header = document.getElementById("content-title");
    var start = ((data.page - 1) * data.resultsPerPage) + 1;
    var end = data.page * data.resultsPerPage;
    end = (end < data.resultCount) ? end : data.resultCount;
    let percent = (data.blockQueried / data.totalBlocks).toFixed(2);

    let infoText = "Concordances (" + formatNumber(start) + " - " + formatNumber(end) + " of " + formatNumber((data.resultCount / percent).toFixed(0)) + ")";

    loadingResults(data, infoText);

    var table = document.createElement("table");
    table.className = "results";
    results.appendChild(table);
    lineNum = 0;
    for (var i = 0; i < concs.length; i++) {
        var conc = concs[i];
        var hitToken = conc[context + 1];
        var tr = document.createElement("tr");
        table.appendChild(tr);

        var tdnumber = document.createElement("td");
        tdnumber.className = "meta";
        tr.appendChild(tdnumber);
        var number = document.createTextNode(start.toString());
        start = start + 1;
        tdnumber.appendChild(number);


        var tdspeaker = document.createElement("td");
        tdspeaker.className = "meta";
        tr.appendChild(tdspeaker);
        if (hitToken != null && hitToken.speaker != null) {
            var link = document.createElement("a");
            link.target = "blank";
            link.href = "https://hansard.parliament.uk/search/MemberContributions?memberId=" + hitToken.id;
            var speakerName = document.createTextNode(hitToken.speaker);
            link.appendChild(speakerName);
            tdspeaker.appendChild(link);
        }

        var tdhouse = document.createElement("td");
        tdhouse.className = "meta";
        tr.appendChild(tdhouse);
        if (hitToken != null && hitToken.house != null) {
            var house = document.createTextNode(hitToken.house);
            tdhouse.appendChild(house);
        }

        var tddate = document.createElement("td");
        tddate.className = "meta";
        tr.appendChild(tddate);
        if (hitToken != null && hitToken.date != null) {
            var date = document.createTextNode(hitToken.date);
            tddate.appendChild(date);
        }

        var tdpre = document.createElement("td");
        tdpre.className = "pre";
        tr.appendChild(tdpre);
        tokenNum = 0;
        for (var j = 0; j < context; j++) {
            var token = conc[j];
            tdpre.appendChild(getTokenNode(token));
            tokenNum += 1;
        }

        var tdhit = document.createElement("td");
        tdhit.className = "hit";
        tr.appendChild(tdhit);
        for (var j = context; j < conc.length - context; j++) {
            var token = conc[j];
            tdhit.appendChild(getTokenNode(token));
            tokenNum += 1;
        }

        var tdpost = document.createElement("td");
        tdpost.className = "post";
        tr.appendChild(tdpost);
        for (var j = conc.length - context; j < conc.length; j++) {
            var token = conc[j];
            tdpost.appendChild(getTokenNode(token));
            tokenNum += 1;
        }

        var tdfile = document.createElement("td");
        tdfile.className = "meta";
        tr.appendChild(tdfile);
        if (hitToken != null && hitToken.$file != null) {
            var file = document.createTextNode(hitToken.$file);
            tdfile.appendChild(file);
        }

        lineNum += 1;
    }
    var jumbotron = document.getElementById("jumbotron");
    while (jumbotron.firstChild) {
        jumbotron.removeChild(jumbotron.firstChild);
    }
    jumbotron.appendChild(results);

    var paging = "";
    var width = 0
    if (data.resultCount > data.resultsPerPage) {
        paging += "<div class='input-group'>";
        paging += "<div class='input-group-prepend'>"
        paging += "<button class='btn btn-outline-secondary' type='button' onclick='runQuery(" + (data.page - 2) + ", false)'";
        if (data.page == 1)
            paging += "disabled";
        paging += "><i class=\"fas fa-angle-left\"></i></button>";
        var inputWidth = 40 + (data.pages.toString().length * 10);
        paging += "<input style=\"width: " + inputWidth + "px;\" type=\"number\" min=\"1\" max=\"" + data.pages + "\" id=\"page-number\" class=\"form-control\" value=\"" + data.page + "\" aria-label=\"search\" aria-describedby=\"basic-addon1\" onfocusout=\"updatePage(this, " + data.page + ")\" onkeyup=\"updatePageKey(event, " + data.page + ")\">";
        paging += "<span class='input-group-text'>"
        //paging += data.page;
        paging += "/";
        paging += data.pages;
        paging += "</span></div>";
        paging += "<div class='input-group-append'>"
        paging += "<button class='btn btn-outline-secondary' type='button' onclick='runQuery(" + data.page + ", false)'";
        if (data.page == data.pages)
            paging += "disabled";
        paging += "><i class=\"fas fa-angle-right\"></i></button>";
        paging += "</div>";
        paging += "</div>";
        width = 110 + (data.pages.toString().length * 10);
        width = width + inputWidth;
        width = width + "px";
    }
    document.getElementById("paging").style.width = width;
    $("#paging").html(paging);
}

function displayNgrams(data) {
    var header = document.getElementById("content-title");
    var infoText = document.createTextNode("NGrams (" + formatNumber(data.ngrams.length) + " items)");
    infoText = (data.ngrams.length > 0) ? infoText : document.createTextNode("List (No results found)");
    header.innerHTML = "";
    header.appendChild(infoText);

    var paging = document.getElementById("paging");
    paging.innerHTML = "";
    paging.style.width = "";

    document.getElementById("chart").innerHTML = "";
    var results = document.createDocumentFragment();
    var table = document.createElement("table");
    table.className = "results";
    results.appendChild(table);


    var header = document.createElement("tr");
    table.appendChild(header);

    var ngramHeader = document.createElement("th");
    var ngramText = document.createTextNode("N-Gram");
    ngramHeader.appendChild(ngramText)
    header.appendChild(ngramHeader)


    var countHeader = document.createElement("th");
    var countText = document.createTextNode("Count");
    countHeader.appendChild(countText)
    header.appendChild(countHeader);

    for (var i = 0; i < data.ngrams.length; i++) {
        let row = document.createElement("tr");
        table.appendChild(row);

        let ngram = document.createElement("td");
        let ngramText = document.createTextNode(data.ngrams[i].key);
        ngram.appendChild(ngramText);
        row.appendChild(ngram);

        let count = document.createElement("td");
        let countText = document.createTextNode(data.ngrams[i].value);
        count.appendChild(countText);
        row.appendChild(count);
    }


    var jumbotron = document.getElementById("jumbotron");
    while (jumbotron.firstChild) {
        jumbotron.removeChild(jumbotron.firstChild);
    }
    jumbotron.appendChild(results);

    hideViz();

}

function displayCollocates(data, updateViz) {

    if (updateViz && data.resultCount > 0 && data.blockQueried == data.totalBlocks) {
        displayWordcloud(data);
    }

    var infoText = "Collocates (" + formatNumber(data.collocations.length) + " items)";

    loadingResults(data, infoText)

    var paging = document.getElementById("paging");
    paging.innerHTML = "";
    paging.style.width = "";

    document.getElementById("chart").innerHTML = "";

        var results = document.createDocumentFragment();
        var table = document.createElement("table");
        table.className = "results";
        table.style = "table-layout: auto;";
        results.appendChild(table);

        if(data.collocations.length > 0){
            var header = document.createElement("tr");
            table.appendChild(header);
            var ngramHeader = document.createElement("th");
            var ngramText = document.createTextNode("Collocate");
            ngramHeader.appendChild(ngramText);
            header.appendChild(ngramHeader);
            for (var p in data.collocations[0].value) {
                var thisHeader = document.createElement("th");
                thisHeader.style = "text-align: right";
                var thisText = document.createTextNode(p);
                thisHeader.appendChild(thisText);
                header.appendChild(thisHeader);
            }
        }

        for (var i = 0; i < data.collocations.length; i++) {
            let row = document.createElement("tr");
            table.appendChild(row);

            let collocate = document.createElement("td");
            let collocateText = document.createTextNode(data.collocations[i].key);
            collocate.appendChild(collocateText);
            row.appendChild(collocate);

            for (var p in data.collocations[i].value) {
                let val = document.createElement("td");
                val.style = "text-align: right; padding-left: 50px";
                let num = data.collocations[i].value[p];
                if(p === "ll")
                    num = num.toFixed(3);
                let valText = document.createTextNode(num);
                val.appendChild(valText);
                row.appendChild(val);
            }
            let extra = document.createElement("td");
            row.appendChild(extra);
        }

        //console.log("Displaying " + data.collocations.length + " collocations");
        var jumbotron = document.getElementById("jumbotron");
        while (jumbotron.firstChild) {
            jumbotron.removeChild(jumbotron.firstChild);
        }
        //console.log(results);
        jumbotron.appendChild(results);
}

function displayList(data) {
    var header = document.getElementById("content-title");
    var start = ((data.page - 1) * data.resultsPerPage) + 1;
    var end = data.page * data.resultsPerPage;
    end = (end < data.resultCount) ? end : data.resultCount;
    var infoText = document.createTextNode("List (" + formatNumber(start) + " - " + formatNumber(end) + " of " + formatNumber(data.resultCount) + ")");

    //var infoText = document.createTextNode("List (" + formatNumber(data.list.length) + " items)");
    infoText = (data.list.length > 0) ? infoText : document.createTextNode("List (No results found)");
    header.innerHTML = "";
    header.appendChild(infoText);

    var paging = document.getElementById("paging");
    paging.innerHTML = "";
    paging.style.width = "";

    document.getElementById("chart").innerHTML = "";
    var results = document.createDocumentFragment();
    var table = document.createElement("table");
    table.className = "results";
    results.appendChild(table);


    var header = document.createElement("tr");
    table.appendChild(header);

    var listHeader = document.createElement("th");
    var listText = document.createTextNode("Item");
    listHeader.appendChild(listText)
    header.appendChild(listHeader)


    var countHeader = document.createElement("th");
    var countText = document.createTextNode("Count");
    countHeader.appendChild(countText)
    header.appendChild(countHeader);

    for (var i = 0; i < data.list.length; i++) {
        let row = document.createElement("tr");
        table.appendChild(row);

        let ngram = document.createElement("td");
        let ngramText = document.createTextNode(data.list[i].key);
        ngram.appendChild(ngramText);
        row.appendChild(ngram);

        let count = document.createElement("td");
        let countText = document.createTextNode(data.list[i].value);
        count.appendChild(countText);
        row.appendChild(count);
    }


    var jumbotron = document.getElementById("jumbotron");
    while (jumbotron.firstChild) {
        jumbotron.removeChild(jumbotron.firstChild);
    }
    jumbotron.appendChild(results);
}

function displayResults(data, updateViz) {

    if (data.concordances != null) {
        displayConcordances(data, updateViz);
    } else if (data.ngrams != null) {
        displayNgrams(data);
    } else if (data.list != null) {
        displayList(data);
    } else if (data.collocations != null) {
        displayCollocates(data, updateViz);
    } else {
        $(".jumbotron").first().html("No results to show");
        return;
    }

}

function showTooltip(event, token) {
    var tokenspan = event.target;
    if (tokenspan.nodeName !== "DIV")
        return;
    var children = tokenspan.children;
    var create = true;
    for (var i = 0; i < children.length; i++) {
        var child = children[i];
        if (child.className === "lexitooltiptext") {
            create = false;
        }
    }
    if (create) {
        if (token.token != "") {
            var tooltipspan = document.createElement("span");
            tooltipspan.className = "lexitooltiptext";
            tokenspan.appendChild(tooltipspan);

            var tooltiptable = document.createElement("table");
            tooltipspan.appendChild(tooltiptable);

            for (var prop in token) {
                if (prop !== 'speaker' && prop !== 'date' && prop !== 'i' && prop !== 'token' && prop !== 'id' && prop !== 'house' && prop !== '$file') {
                    var tr = document.createElement("tr");
                    tooltiptable.appendChild(tr);

                    var tdkey = document.createElement("td");
                    tr.appendChild(tdkey);
                    var keytext = document.createTextNode(prop);
                    tdkey.appendChild(keytext);

                    var tdval = document.createElement("td");
                    tr.appendChild(tdval);
                    var valtext = document.createTextNode(token[prop]);
                    tdval.appendChild(valtext);
                }
            }
        }
    }
}

function getTokenNode(token) {
    var tokenspan = document.createElement("span");
    tokenspan.className = "lexitooltip";
    if (coloring == "pos" && token != null && token.pos != null)
        tokenspan.className += " " + token.pos.charAt(0);
    if (coloring == "sem" && token != null && token.sem != null)
        tokenspan.className += " " + token.sem.charAt(0);
    if (token.hit)
        tokenspan.className += " hit";


    if (token != null && token.token != null) {
        var tokendiv = document.createElement("div");
        var toAppend = token.token;
        if (toAppend.startsWith("<")) {
            var clipEnd = (toAppend.length < 10) ? toAppend.length : 10;
            toAppend = toAppend.substr(0, clipEnd);
            toAppend = (toAppend.length < token.token.length) ? toAppend + "...>" : toAppend;
        }

        var tokentext = document.createTextNode(toAppend + " ");
        tokendiv.appendChild(tokentext);
        tokenspan.appendChild(tokendiv);
    }

    tokenspan.onmouseover = function (evt) {
        showTooltip(evt, token)
    };

    return tokenspan;
}

function handleError(errMsg) {
    alert(errMsg);
}

function toggleOptions() {
    if (navOpen) {
        closeNav();
    } else {
        openNav();
    }

}

var navOpen = false;

/* Set the width of the sidebar to 250px and the left margin of the page content to 250px */
function openNav() {
    document.getElementById("sidebar").style.width = "450px";
    document.getElementById("jumbotron").style.right = "450px";
    navOpen = true;
}

/* Set the width of the sidebar to 0 and the left margin of the page content to 0 */
function closeNav() {
    document.getElementById("sidebar").style.width = "0";
    document.getElementById("jumbotron").style.marginLeft = "0";
    navOpen = false;
}

function addSortOption() {
    let sortOptions = document.getElementById("sortOptions");
    let oldSortOption = sortOptions.firstElementChild;
    let newSortOption = oldSortOption.cloneNode(true);
    newSortOption.style.display = "";
    sortOptions.appendChild(newSortOption);
    allSortOptions.push(newSortOption);
}


function updatePage(pageSelect, currentPage) {
    var page = pageSelect.value;
    if (page != currentPage) {
        runQuery(page - 1, false);
    }
}

function updatePageKey(event, currentPage) {
    if (event.keyCode === 13) {
        var pageNumber = document.getElementById("page-number");
        updatePage(pageNumber, currentPage);
    }
}

function deleteSortOption(element) {
    parent = element.parentNode;
    grandparent = parent.parentNode;
    grandparent.remove();
}

function getSortOptions() {
    var sortOptions = document.getElementById("sortOptions");
    var sorts = sortOptions.getElementsByClassName("sort")
    var allSorts = [];
    for (var i = 1; i < sorts.length; i++) {
        sort = {};
        var column = sorts[i].getElementsByClassName("sort-column");
        sort.column = column[0].value
        var position = sorts[i].getElementsByClassName("sort-position");
        sort.position = position[0].value
        var type = sorts[i].getElementsByClassName("sort-type");
        sort.ascending = type[0].className.includes("down")
        sort.alphabetical = type[0].className.includes("alpha");
        allSorts.push(sort);
    }
    return allSorts;
}

function changeSort(element) {
    var icon = element.firstChild;
    if (icon.className == "fas fa-sort-alpha-down sort-type") {
        icon.className = "fas fa-sort-alpha-up sort-type";
    } else if (icon.className == "fas fa-sort-alpha-up sort-type") {
        icon.className = "fas fa-sort-numeric-down sort-type";
    } else if (icon.className == "fas fa-sort-numeric-down sort-type") {
        icon.className = "fas fa-sort-numeric-up sort-type";
    } else if (icon.className == "fas fa-sort-numeric-up sort-type") {
        icon.className = "fas fa-sort-alpha-down sort-type";
    }
}


let defaults = new Map();
defaults.set("context", 5);
defaults.set("resultsPerPage", 50);
defaults.set("n", 3);
defaults.set("ngram-context", 2);
defaults.set("col-context", 1);
defaults.set("topXResults", 150);
defaults.set("speaker", "");
defaults.set("date", "");


function resetDefaults() {
    for(const [key, val] of defaults.entries()){
        document.getElementById(key).value = val;
    }
    clearSort();
}

function clearSort(){
    for(let i = 0; i < allSortOptions.length; i++){
        allSortOptions[i].remove();
    }
    allSortOptions = [];
}

function apply() {
    runQuery(0, true);
    closeNav();
}

