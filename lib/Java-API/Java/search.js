function searchQuery(query) {
	$("#searchForm").removeAttr("action");
	query = query.replace(/[&<>"']/g, "");
	if (!query || query.length<2) {
		$("#nav").removeClass("searching");
		$("#nav li").each(function(index, element){
			var e = $(element);
			e.css("display", "");
		});
		$("#nav ul:first-child").css("display", "block");
		$("#search-results-list").css("display", "none");
		return;
	}
	var queryParts = query.toLowerCase().split(" ");
	var listItems = [];
	var bestMatch = 200;
	$("#nav").addClass("searching");
	$("#nav ul:first-child").css("display","none");
	$("#nav li").each(function(index, element) {
		var e = $(element);
		if (!e.hasClass("expando")) {
			let full_name = e.attr("data_path");
			let split_full_name = full_name.split('.');
			let className = split_full_name[split_full_name.length - 1];

			// Don't search in info pages
			if (split_full_name[0] == 'Info Pages') {
				return;
			}
			
			/*
			This is kept even though not used, in case it will be needed in the future (for the case of multiple packages).
			let packageName = full_name.substring(0, full_name.lastIndexOf('.'));

			if (packageName !== '')  // if there is a package, append a '.' to it's name for later printing
				packageName += '.';
			*/

			searchData[className].concat(['']).forEach( (attrName) => {  // empty string is added to represent the class itself and prevent code duplicate
				let pathEnd = attrName !== '' ? "#" + attrName : '';  // should be appended to the end of the URL
				let searchVal = attrName !== '' ? attrName : className
				attrName = attrName !== '' ? className + '.' + attrName : className // attribute name is appended to the class name
				let score = searchMatch(searchVal, queryParts);
				if (score < 0) {
					// try to search the full attribute name, to support searches such as 'PirateGame.g...'
					score = searchMatch(attrName, queryParts);
				}
				if (score >= 0) {
					if (score < bestMatch) {
						let url = dox.rootPath + e.attr("data_path").split(".").join("/") + ".html" + pathEnd;
						$("#searchForm").attr("action", url);
						// best match will be form action
						bestMatch = score;
					}

					let elLink = $("a", element);
					// highlight matched parts - elLinkContent is the content to be displayed in the search bar.
					let elLinkContent = attrName.replace(
						new RegExp("(" + queryParts.join("|").split(".").join("|") + ")", "ig"), "<strong>$1</strong>");
					let liStyle = (score == 0) ? ("font-weight:bold") : "";
					listItems.push({score: score, item: "<li style='" + liStyle + "'><a href='" + elLink.attr("href") + pathEnd + "'>" + elLinkContent + "</a></li>"});
				}
			});
		}
	});
	if ($("#search-results-list").length == 0) {
		// append to nav
		$("#nav").parent().append("<ul id='search-results-list' class='nav nav-list'></ul>");
	}
	listItems.sort(function(x, y) { return x.score - y.score; }); // put in order
	$("#search-results-list").css("display","block").html(listItems.map(function(x) { return x.item; }).join(""));
}

$(document).ready(() => {
	const ENTER = 13;
	// make sure that using 'enter' when searching doesn't redirect the page.
	$("#search").keypress((e) => e.which !== ENTER);
});
