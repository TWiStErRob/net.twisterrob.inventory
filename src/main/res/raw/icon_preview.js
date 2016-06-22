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
</style>`);
fs.readdir('.', function (err, list) {
	if (err) throw err;
	var icons = /^(category|property|room|ic|snippet).*\.svg$/;
	list.forEach(function (file) {
		if (!file.match(icons)) return;
		console.log(`<div class="item"><a href="` + file + `">
			<div class="caption">` + file + `</div>
			<div class="icon"><embed src="` + file + `"></embed></div>
		</a></div>`);
	});
});
