<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	extension-element-prefixes="xalan"
	exclude-result-prefixes="xml xsi"
	version="1.0"
>
	<!-- Using Xalan namespace to have better output:
	     Indent a little for readability (change to non-zero). indent="no" omits new lines too.
	     It would be nice to get rid of entities, because we have the awesome utf-8 to handle fancy characters.
	     It's sadly hard to achieve, because:
	      * xalan:entities="" breaks quotes in attribute values
	      * method="xml" can't handle open empty tags (e.g. <div></div> must be output as such)
	      * xalan:entities="org/apache/xml/serializer/XMLEntities" doesn't work
	        not even prefixed with "classpath:" or suffixed with ".properties"
	        however if given in code, it works:
	        transformer.setOutputProperty("{http://xml.apache.org/xalan}entities", "org/apache/xml/serializer/XMLEntities");
	-->
	<xsl:output method="html" encoding="utf-8" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="0" />

	<xsl:variable name="appLink">https://play.google.com/store/apps/details?id=net.twisterrob.inventory</xsl:variable>
	<xsl:template match="/inventory">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<!--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />-->
				<title>Magic Home Inventory</title>
				<xsl:copy-of select="$style" />
			</head>
			<body>
				<!-- Fake shadow for all .location DIVs.
				     There are multiple visible at the same time, so it cannot be applied to each of them. -->
				<div id="location-shadow">
					This file was generated with the <a href="{$appLink}">Magic Home Inventory</a> app for Android.
				</div>
				<div id="toc">
					<xsl:call-template name="toc" />
				</div>
				<div id="inventory">
					<xsl:call-template name="children">
						<xsl:with-param name="children" select="property" />
						<xsl:with-param name="childrenName">properties</xsl:with-param>
					</xsl:call-template>
				</div>
				<div id="lists">
					<xsl:apply-templates select="list" />
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="toc">
		<ul class="toc-properties">
			<xsl:for-each select="property">
				<li data-id="toc-property-{@id}">
					<a href="#property-{@id}">
						<xsl:value-of select="@name" />
					</a>
					<ul class="toc-rooms">
						<xsl:for-each select="room">
							<li data-id="toc-room-{@id}">
								<a href="#room-{@id}">
									<xsl:value-of select="@name" />
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</li>
			</xsl:for-each>
		</ul>
		<ul class="toc-lists">
			<xsl:for-each select="list">
				<li data-id="toc-list-{position()}">
					<a href="#list-{position()}">
						<xsl:value-of select="@name" />
					</a>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>

	<xsl:template match="list">
		<div class="belonging list" data-id="list-{position()}">
			<xsl:call-template name="name">
				<xsl:with-param name="type">list</xsl:with-param>
				<xsl:with-param name="id">
					<xsl:value-of select="position()" />
				</xsl:with-param>
			</xsl:call-template>
			<ul class="items">
				<xsl:for-each select="item-ref">
					<li class="item-ref" data-id="item-{@id}">
						<a href="#item-{@id}"></a>
						<div class="belonging item">
							<!-- This is a single-iteration, always! -->
							<xsl:for-each select="//item[@id=current()/@id]">
								<xsl:call-template name="details" />
							</xsl:for-each>
						</div>
					</li>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="property">
		<li class="belonging property" data-id="property-{@id}">
			<xsl:call-template name="details">
				<xsl:with-param name="type">property</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="children">
				<xsl:with-param name="children" select="room" />
				<xsl:with-param name="childrenName">rooms</xsl:with-param>
			</xsl:call-template>
		</li>
	</xsl:template>

	<xsl:template match="room">
		<li class="belonging room" data-id="room-{@id}">
			<xsl:call-template name="details">
				<xsl:with-param name="type">room</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="children">
				<xsl:with-param name="children" select="item" />
				<xsl:with-param name="childrenName">items</xsl:with-param>
			</xsl:call-template>
		</li>
	</xsl:template>

	<xsl:template match="item">
		<li class="belonging item" data-id="item-{@id}">
			<xsl:call-template name="details">
				<xsl:with-param name="type">item</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="children">
				<xsl:with-param name="children" select="item" />
				<xsl:with-param name="childrenName">items</xsl:with-param>
			</xsl:call-template>
		</li>
	</xsl:template>

	<xsl:template name="children">
		<xsl:param name="children" />
		<xsl:param name="childrenName" />
		<xsl:choose>
			<xsl:when test="count($children) = 0">
				<span class="{$childrenName} empty" />
			</xsl:when>
			<xsl:otherwise>
				<ul class="{$childrenName}">
					<xsl:apply-templates select="$children" />
				</ul>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="details">
		<xsl:param name="type" select="''" />
		<xsl:call-template name="image" />
		<xsl:call-template name="name">
			<xsl:with-param name="type">
				<xsl:value-of select="$type" />
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="description" />
		<xsl:call-template name="location" />
	</xsl:template>

	<xsl:template name="name">
		<xsl:param name="type" select="''" />
		<xsl:param name="id" select="@id" />
		<div class="name">
			<xsl:if test="$type != '' and $id != ''">
				<xsl:attribute name="id" xml:space="preserve"><xsl:value-of select="$type"/>-<xsl:value-of select="$id"/></xsl:attribute>
			</xsl:if>
			<xsl:value-of select="@name" />
		</div>
	</xsl:template>

	<xsl:template name="image">
		<xsl:choose>
			<xsl:when test="@image">
				<a class="image" href="{@image}" target="_blank">
					<img src="{@image}" />
				</a>
			</xsl:when>
			<xsl:otherwise>
				<div class="no-image" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="description">
		<xsl:if test="description[normalize-space()]">
			<pre class="description">
				<xsl:value-of select="description" />
			</pre>
		</xsl:if>
	</xsl:template>

	<xsl:template name="location">
		<xsl:variable name="location" xml:space="preserve"><!--
		--><xsl:for-each select="ancestor::*/@name"><!--
			--><xsl:value-of select="." /> ▶ <!--
		--></xsl:for-each><!--
		--><xsl:value-of select="@name" /><!--
		--></xsl:variable>
		<div class="location">
			<xsl:value-of select="$location" />
		</div>
	</xsl:template>

	<xsl:variable name="style" xml:space="preserve">
	<style type="text/css">/*&lt;![CDATA[*/
	/*
	  Tested browsers:
	  Desktop: IE7+, Chrome 52, Firefox 46,
	  Android: Chrome 52, Android 5.0 HTMLViewer , Android 5.0 Samsung Galaxy S5 Internet
	*/

	ul.properties,
	ul.rooms,
	ul.items {
		list-style-type: none;
		margin: 0;
		padding: 4px 0 0 0;
		clear: both;
	}

	.belonging {
		padding: 4px;
	}

	@media only screen and (min-width: 40em) {
		.belonging {
			margin-left: 8px;
		}
	}

	@media only screen and (min-width: 64em) {
		.belonging {
			margin-left: 16px;
		}
	}

	@media only screen and (min-width: 78em) {
		.belonging {
			margin-left: 24px;
		}
	}

	@media only screen and (min-width: 92em) {
		.belonging {
			margin-left: 32px;
		}
	}

	.property, .list {
		border: 2px solid black;
		margin-bottom: 8px; /* separate properties and lists */
		margin-left: 0;
	}

	.room {
		border: 1px solid black;
		margin-bottom: 4px; /* separate rooms */
	}

	.belonging:last-child {
		margin-bottom: 0; /* separate, except last */
	}

	#lists, #inventory {
		margin-top: 32px; /* separate sections */
	}

	.item {
		border: 1px solid lightgray;
	}

	.belonging > .image {
		float: left;
		width: 64px;
		height: 64px;
		line-height: 62px; /* for vertical-align (need to be a little bit smaller) */
		background: lightgray;
		text-align: center; /* if image is tall */
		margin: 0 0 0 4px;
	}

	.belonging > .image > img {
		max-width: 100%;
		max-height: 100%;
		vertical-align: middle; /* if image is wide */
		border: 0; /* old IE blue border when linked */
	}

	.belonging > .no-image {
		float: left;
		height: 0;
		width: 0;
		margin: 0;
	}

	.belonging > .image ~ .name,
	.belonging > .image ~ .description {
		/* When description is taller than image, don't wrap below the image, keep the line. */
		padding-left: 68px; /* image.margin-left + image.width + image.margin-right */
	}

	.belonging > .name {
		margin-top: 4px;
		margin-left: 4px;
		font-family: sans-serif;
	}

	.property > .name {
		font-size: x-large;
		font-weight: bolder;
	}

	.room > .name {
		font-size: large;
		font-weight: bold;
	}

	.list > .name {
		font-size: large;
		font-weight: bold;
	}

	.belonging > .description {
		margin-top: 4px;
		margin-left: 4px;
		margin-bottom: 0;
		font-style: italic;
		font-family: serif;
		white-space: pre-wrap;
	}

	.item-ref {
		position: relative;
	}

	.item-ref:hover {
		background: rgba(0, 0, 0, .025);
	}

	/* http://stackoverflow.com/a/22074404/253468 */
	.item-ref > a {
		position: absolute;
		width: 100%;
		height: 100%;
		top: 0;
		left: 0;
		text-decoration: none; /* No underlines on the link */
		z-index: 1; /* Places the link above everything else in the div */
	}

	.item-ref > * a {
		position: relative;
		z-index: 2;
	}

	.belonging > .location,
	#location-shadow {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		padding: 8px;
		z-index: 10;
		background: white;
		border-bottom: 1px solid lightgray;
	}

	.belonging > .location {
		visibility: hidden;
	}

	.belonging:hover > .location,
	.item-ref:hover .location {
		visibility: visible;
	}

	#location-shadow {
		box-shadow: 0 0 4px rgba(0, 0, 0, .15), 0 4px 8px rgba(0, 0, 0, .3);
	}

	/* Push the body down so the first line is properly visible. */
	body {
		padding-top: 34px;
	}

	/* Push the targeted element down, so it's not hiding under the bar. */
	*:target::before {
		content: " ";
		width: 0;
		height: 0;
		display: block;
		/* two lines of text worth */
		padding-top: 68px !important;
		margin-top: -68px !important;
	}

	.belonging:hover {
		/*background: rgba(0,0,0,.025);*/
	}

	@media only print {
		body {
			font-size: 75%;
		}

		#toc,
		#location-shadow,
		.location,
		.belonging > .image,
		.belonging > .no-image {
			display: none;
		}

		.belonging {
			margin-left: 6px;
			padding-bottom: 0;
			padding-top: 0;
			border-bottom: 0;
		}

		.belonging > .image ~ .name,
		.belonging > .image ~ .description {
			padding-left: 0;
		}

		#lists, #inventory, .property {
			page-break-before: always;
		}
	}

	/* Clearfix */
	.belonging {
		zoom: 1; /* IE6/7 hasLayout */
	}

	.belonging:before,
	.belonging:after {
		content: " "; /* Older browsers don't support empty content */
		display: table;
	}

	.belonging:after {
		clear: both;
	}

	/*]]&gt;*/</style>
	</xsl:variable>

</xsl:stylesheet>
