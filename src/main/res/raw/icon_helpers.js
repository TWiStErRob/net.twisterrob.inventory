function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
	var angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;
	return {
		x: centerX + (radius * Math.cos(angleInRadians)),
		y: centerY + (radius * Math.sin(angleInRadians))
	};
}

function round(numberOrCoord) {
	if (typeof numberOrCoord === 'number') {
		return Math.round(numberOrCoord * 1000) / 1000;
	} else {
		numberOrCoord.x = round(numberOrCoord.x);
		numberOrCoord.y = round(numberOrCoord.y);
		return numberOrCoord;
	}
}

//noinspection JSUnusedGlobalSymbols
function describeArc(x, y, radius, startAngle, endAngle) {
	var start = round(polarToCartesian(x, y, radius, endAngle));
	var end = round(polarToCartesian(x, y, radius, startAngle));
	var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";
	return [
		"M", x, y,
		"l", start.x, start.y,
		"A", radius, radius, 0, arcSweep, 0, end.x, end.y,
		"Z"
	].join(" ");
}

//noinspection JSUnusedGlobalSymbols
function rounded_rect_uni(x, y, w, h, tl, tr, bl, br) {
	return rounded_rect(x, y, w, h, tl, tl, tr, tr, bl, bl, br, br);
}

function rounded_rect(x, y, w, h, rTLx, rTLy, rTRx, rTRy, rBLx, rBLy, rBRx, rBRy) {
	console.debug(arguments);
	var path = "";
	path += "M" + (x + rTLx) + "," + y;
	path += "\n";
	path += "h" + (w - (rTLx + rTRx));
	path += "\n";
	if (rTRx > 0 && rTRy > 0) {
		path += "a " + rTRx + "," + rTRy + " 0 0 1 " + rTRx + "," + rTRy;
	} else {
		path += "h" + rTRx;
		path += "v" + rTRy;
	}
	path += "\n";
	path += "v" + (h - (rTRy + rBRy));
	path += "\n";
	if (rBRx > 0 && rBRy > 0) {
		path += "a " + rBRx + "," + rBRy + " 0 0 1 " + -rBRx + "," + rBRy;
	} else {
		path += "v" + rBRy;
		path += "h" + -rBRx;
	}
	path += "\n";
	path += "h" + -(w - (rBRx + rBLx));
	path += "\n";
	if (rBLx > 0 && rBLy > 0) {
		path += "a " + rBLx + "," + rBLy + " 0 0 1 " + -rBLx + "," + -rBLy;
	} else {
		path += "h" + -rBLx;
		path += "v" + -rBLy;
	}
	path += "\n";
	path += "v" + -(h - (rBLy + rTLy));
	path += "\n";
	if (rTLx > 0 && rTLy > 0) {
		path += "a " + rTLx + "," + rTLy + " 0 0 1 " + rTLx + "," + -rTLy;
	} else {
		path += "v" + -rTLy;
		path += "h" + rTLx;
	}
	path += "\n";
	path += "z";
	return path;
}
