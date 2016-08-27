<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	extension-element-prefixes="xalan"
	exclude-result-prefixes="xml xsi"
	version="1.0">

	<xsl:output method="text" encoding="utf-8" />

	<xsl:template match="/inventory">
		<!-- Outputting BOM like this doesn't work, so the file needs to be prefixed in another way. -->
		<!--<xsl:text disable-output-escaping="yes">&#xEF;&#xBB;&#xBF;</xsl:text>-->
		<xsl:text>type,id,property,room,containers,name,depth,image,description&#x0A;</xsl:text>
		<xsl:apply-templates select="property" />
	</xsl:template>

	<xsl:template match="property">
		<xsl:call-template name="details" />
		<xsl:apply-templates select="room" />
	</xsl:template>

	<xsl:template match="room">
		<xsl:call-template name="details" />
		<xsl:apply-templates select="item" />
	</xsl:template>

	<xsl:template match="item">
		<xsl:call-template name="details" />
		<xsl:apply-templates select="item" />
	</xsl:template>

	<xsl:template name="details">
		<xsl:variable name="type">
			<xsl:value-of select="local-name()" />
		</xsl:variable>
		<xsl:variable name="depth">
			<xsl:value-of select="count(ancestor::*) - 1" />
		</xsl:variable>
		<xsl:variable name="id">
			<xsl:value-of select="@id" />
		</xsl:variable>
		<xsl:variable name="property">
			<xsl:call-template name="cell-value">
				<xsl:with-param name="text" select="ancestor-or-self::property/@name" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="room">
			<xsl:call-template name="cell-value">
				<xsl:with-param name="text" select="ancestor-or-self::room/@name" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="name">
			<xsl:call-template name="cell-value">
				<xsl:with-param name="text" select="@name" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="image">
			<xsl:value-of select="@image" />
		</xsl:variable>
		<xsl:variable name="description">
			<xsl:call-template name="cell-value">
				<xsl:with-param name="text" select="description/text()" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="containers">
			<xsl:call-template name="cell-value">
				<xsl:with-param name="text" xml:space="preserve"><!--
				--><xsl:for-each select="ancestor::item"><!--
					--><xsl:value-of select="@name" /><!--
					 --><xsl:if test="position() != last()"> ▶ </xsl:if><!--
				--></xsl:for-each><!--
			--></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<!-- The following blocks is only to limit the scope of xml:space as CSV needs line-breaks to be correct. -->
		<xsl:if test="true()" xml:space="preserve"><!--
		--><xsl:value-of select="$type" />,<!--
		--><xsl:value-of select="$id" />,<!--
		-->"<xsl:value-of select="$property" />",<!--
		-->"<xsl:value-of select="$room" />",<!--
		-->"<xsl:value-of select="$containers" />",<!--
		-->"<xsl:value-of select="$name" />",<!--
		--><xsl:value-of select="$depth" />,<!--
		--><xsl:value-of select="$image" />,<!--
		-->"<xsl:value-of select="$description"/>"<!--
		--><xsl:text>&#x0A;</xsl:text><!--
		--></xsl:if>
	</xsl:template>

	<xsl:template name="cell-value">
		<xsl:param name="text" />
		<xsl:variable name="escaped">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$text" />
				<xsl:with-param name="replace" select="'&quot;'" />
				<xsl:with-param name="with" select="'&quot;&quot;'" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:value-of select="$escaped" />
	</xsl:template>

	<!-- @see http://stackoverflow.com/a/10528912/253468 -->
	<xsl:template name="string-replace-all">
		<xsl:param name="text" />
		<xsl:param name="replace" />
		<xsl:param name="with" />
		<xsl:choose>
			<xsl:when test="$text = '' or $replace = '' or not($replace)">
				<xsl:value-of select="$text" />
			</xsl:when>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text, $replace)" />
				<xsl:value-of select="$with" />
				<xsl:call-template name="string-replace-all">
					<xsl:with-param name="text" select="substring-after($text, $replace)" />
					<xsl:with-param name="replace" select="$replace" />
					<xsl:with-param name="with" select="$with" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
