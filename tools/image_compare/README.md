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
```shell
node image_compare compare_foo_bar/foo compare_foo_bar/bar compare_foo_bar/diff
```
and investigate the results in `compare_foo_bar/diff`.

## Setup
```shell
npm install
```

## Usage

GIT ignores all `compare_*` files and folders,
so it's recommended to start by setting up a diff root that way.
