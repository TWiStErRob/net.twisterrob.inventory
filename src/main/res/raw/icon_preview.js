// cls && node icon_preview.js > icon_previews.html
"use strict";
var fs = require('fs');

console.log(`<style>
	body {
		background: lightyellow;
	}
	div.item {
		float: left;
		margin: 4px;
	}
	div.caption {
		text-align: center;
	}
	div.icon {
		width: 256px;
		height: 256px;
	}
	div.icon > embed {
		max-height: 100%;
		max-width: 100%;
		border: 1px solid lightgrey;
	}
	h2 {
		clear: both;
		color: red;
	}
</style>`);
fs.readdir('.', function (err, list) {
	if (err) throw err;
	var pattern = /^(category|property|room|item|snippet|ic).*\.svg$/;
	list = list.filter(function isInterestingSVG(file) { return file.match(pattern); });
	list.sort();
	function outputImage(file) {
		console.log(`<div class="item"><a href="` + file + `">
			<div class="caption">` + file + `</div>
			<div class="icon"><embed src="` + file + `"></embed></div>
		</a></div>`);
	}
	function outGroup(keyStart, title) {
		console.log(`<h2>${title}</h2>`);
		list.filter(function(file) { return file.startsWith(keyStart) }).forEach(outputImage);
	}
	outGroup('category_', "Categories");
	outGroup('item_', "Items");
	outGroup('room_', "Rooms");
	outGroup('property_', "Properties");
	outGroup('ic_', "Icons");
	outGroup('snippet_', "Snippets");
});
