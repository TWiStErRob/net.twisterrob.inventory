const fs = require('fs');
const path = require('path');
const PNG = require('pngjs').PNG;
const pixelmatch = require('pixelmatch');
const base = __dirname;

if (process.argv.length < 1 + 1 + 3) {
	console.log('Usage: image_compare batch1 batch2 result_dir');
	process.exit(64);
}

const [, , batch1, batch2, out] = process.argv;
compareDir(batch1, batch2, out);

function compareDir(batch1, batch2, out) {
	fs.readdirSync(path.join(base, batch1)).forEach(function (file) {
		compareFiles(
				path.join(base, batch1, file),
				path.join(base, batch2, file),
				path.join(base, out, file),
		);
	});
}

function compareFiles(dir1, dir2, out) {
	if (!fs.existsSync(dir1) || !fs.existsSync(dir2)) {
		console.log(`${dir1} x ${dir2} -> doesn't exist`);
		console.log(`\t${dir1}: ${fs.existsSync(dir1)}`);
		console.log(`\t${dir2}: ${fs.existsSync(dir2)}`);
		return;
	} else {
		console.log(`${dir1} x ${dir2} -> ...`);
	}
	const dion = path.join(out, "descript.ion");
	if (fs.existsSync(dion)) fs.unlinkSync(dion);
	fs.readdirSync(dir1).forEach(function (file) {
		if (!fs.existsSync(out)) fs.mkdirSync(out, {recursive: true});
		compareFile(
				path.join(dir1, file),
				path.join(dir2, file),
				path.join(out, file),
				dion,
		);
	});
}

function compareFile(file1, file2, out, dion) {
	console.log(`\t${file1} x ${file2} -> ${out}`);
	let img1, img2;
	try {
		img1 = PNG.sync.read(fs.readFileSync(file1));
		img2 = PNG.sync.read(fs.readFileSync(file2));
	} catch (e) {
		console.log(`Cannot render diff because inputs are wrong: ${out}`);
		return;
	}
	const {width, height} = img1;
	const diff = new PNG({width, height});

	const totalCount = pixelmatch(img1.data, img2.data, diff.data, width, height, {
		threshold: 0.0,
		includeAA: true,
	});

	if (totalCount === 0) {
		console.log(`\tSkipping identical: ${out}`);
		return;
	}

	const nonAliasCount = pixelmatch(img1.data, img2.data, diff.data, width, height, {
		threshold: 0.0,
		includeAA: false,
		diffColor: [255, 0, 0],
		aaColor: [0, 0, 0],
		alpha: 0.01,
	});

	fs.writeFileSync(out, PNG.sync.write(diff));
	fs.appendFileSync(dion, `${path.basename(out)}\t${totalCount} (non-alias: ${nonAliasCount}, alias: ${totalCount - nonAliasCount})\n`);
}
