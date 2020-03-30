Diffing util for folders of images.

For example:
 * compare_foo_bar
   * foo
     * x
       * image1.png
     * y
       * image2.png
   * bar
     * x
       * image1.png
     * y
       * image2.png

To compare the above structure, run this command:
```
node image_compare compare_foo_bar/foo compare_foo_bar/bar compare_foo_bar/diff
```
and investigate the results in `compare_foo_bar/diff`.

## Setup
```
npm install
```

## Usage

SVN ignored all `compare_*` files and folders.
