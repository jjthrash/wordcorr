<?xml version="1.0" encoding="UTF-8"?>
<!--ResultsXML2HTML takes XML output lf Results.XML to HTML
	Joseph E. Grimes, Makaha, Hawaii
	3 December 2005 Remarks padded, set smaller.
	2 December 2006 Revised <style> and tried XHTML.
	26 November 2005. For correspondence sets, <p> ... </p>
	replaced with <br /> to tighten display.
	6-7 October 2005. Initial testing.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" encoding="UTF-8" />

	<!--Document root and HTML setup-->
	<xsl:template match="results-display-xml">
	<html>
			<head>
				<title>Wordcorr Results</title>
				<style type="text/css">
					h1, h2 {font-family: Verdana, Helvetica, Arial,sans-serif;}
					h3, h4, .proto, .clus, .env, .sets, li, p
						{font-family: Gentium, Arial Unicode MS, Lucida Sans Unicode;}
					.proto, .clus, .sets, li, blockquote, p {display: block;}
					.env {font: 12pt; display: inline;}
					.rem {font: 11 pt Arial Unicode MS; font-style: italic; display: inline;
						padding-left: 2em; padding-right: 1 em; text-indent: -1em;}
					h1, h2, h3, h4, .proto, .env {font: bold;}
					h1, h2, h3, h4, {text-align: center; display: block;}
					h1, h4, .proto, .clus {font: 14pt;}
					h2, h3, .sets, li, p {font: 12pt }
				</style>
			</head>
			<body>
				<h2>Wordcorr Results</h2>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

	<!--Collection information-->
	<xsl:template match="collection">
		<h3><xsl:value-of select="@collection-name" /></h3>
		<div class="rem"><xsl:value-of select="@collection-remarks" /></div><hr />
		<xsl:apply-templates/>
	</xsl:template>

	<!--View information including member varieties in order-->
	<!--View members should include full variety names from
	collection/varieties/variety.-->
	<xsl:template match="view">
		<h4><xsl:value-of select="@view-name"/> View</h4>
		<div class="rem"><xsl:value-of select="@remarks"/></div>
		<ol>
		<xsl:for-each select="view-member">
			<li><xsl:value-of select="@short-name"/>
			<xsl:text> - </xsl:text>
			<xsl:value-of select="." />
			</li>
		</xsl:for-each>
		</ol><hr />
		<xsl:apply-templates select="protosegment" /><hr />
	</xsl:template>
=
	<!--Evidence for each protosegment, in phonetic order-->
	<xsl:template match="protosegment">
		<div class="proto">Protosegment *<xsl:value-of select="@symbol"/></div>
		<div class="rem"><xsl:value-of select="@proto-remarks"/></div>

		<!--Each cluster in the protosegment, in the order the linguist set-->
		<xsl:for-each select="cluster">
			<blockquote>
				<div class="clus"><div class="env"><xsl:text>Env </xsl:text>
					<xsl:value-of select="@cluster-order"/>
					<xsl:text>   </xsl:text><xsl:value-of select="@environment"/></div>
					<div class="rem">
						<xsl:value-of select="@cluster-remarks"/>
					</div><br />
					<div class="sets"><xsl:apply-templates select="c-set"/></div>
				</div>
			</blockquote>
		</xsl:for-each>
	</xsl:template>

	<!--Each correspondence set in the cluster-->
	<xsl:template match="c-set">
		<xsl:value-of select="@glyphs"/>
			<div class="rem"><xsl:value-of select="@remarks"/></div>

			<!--Each citation for the correspondence set-->
			<xsl:for-each select="citation">
				<xsl:text> </xsl:text><xsl:value-of select="@entry-number"/>
				<xsl:value-of select="@tag"/><xsl:value-of select="@glyph-position"/>
			</xsl:for-each>
		<br />
	</xsl:template>

</xsl:stylesheet>