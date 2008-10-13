<?xml version="1.0" encoding="UTF-8"?>
<!-- ... \Wordcorr\Testing\XSL\SummaryStyle.xsl
	Converts the output of Refine | Summary of Evidence to human readable.
	2005-12-09 JG. Use <div> to control formatting.
	2005-11-26 JG. Most <p> ... </p> sequences are replaced
	with <br />. Ordering of clusters and groups is now explicit.
-->

<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/TR/REC-html40">
	<xsl:output method="html" encoding="UTF-8" />

	<!-- html and body elements -->
	<xsl:template match="//collection"> <!--collection-->
		<html>
			<head>
				<title>Wordcorr Summary of Evidence</title>
				<style type="text/css">
					h1 {font-family: Verdana,Helvetica,Arial,sans-serif; text-align: center;}
					h2, h4 {font-family: Verdana,Helvetica,Arial,sans-serif; text-align: left;}
					h3, p, .intro, .viewhead, .members, .env, .data
							{font-family: Gentium,Arial Unicode MS,Lucida Sans Unicode;}
					h1 {font-size: 14pt;}
					h2, h3, .env {font-size: 12pt;}
					h4 {font-size: 11pt;}
					p, .intro, .viewhead, .members. .data {font-size: 12pt;}
					.viewhead, .env {display: inline;}
					.rem {font-size 10pt; font-family: Arial Unicode MS; font-style: italic;
						display: inline; padding-left: 2em; padding-right: 1em; text-indent: 1em;}
				</style>
			</head>
			<body>
				<h1>Wordcorr Summary of Evidence</h1>
				<h1><xsl:value-of select="@name"/></h1>
				<div class="intro">Short name of the collection: <xsl:value-of select="@short-name"
					/>. Primary glosses are in <xsl:value-of select="@gloss-language"/>.
					<div class="rem"><xsl:value-of select="./remarks"/></div>
				</div>

					<h2>Varieties:</h2>
					<div class="intro">
						<xsl:for-each select="./varieties/variety">
							<xsl:value-of select="@name"
								/> (<xsl:value-of select="@short-name"
								/> / <xsl:value-of select="@abbreviation"
								/>). <xsl:value-of select="source"/><br />
						</xsl:for-each>
					</div>

					<h2>Views:</h2>
					<xsl:for-each select="//view">
						<div class="viewhead">
							<b><xsl:value-of select="@view-name" /></b> View with threshold of
								<xsl:value-of select="@threshold" />%.
							<div class="rem"><xsl:value-of select="remarks" />.</div>
						</div>
						<!--Needs information on Frantz numbers: cluster, protosegment, or both?-->

						<div class="members">This view's members are in the following order:<br />
						<xsl:for-each select="./view-member">
							<xsl:value-of select="@order-number"
							/>. <xsl:value-of select="@name"
							/> (<xsl:value-of select="@short-name"
							/> / <xsl:value-of select="@abbreviation"/>). <br />
						</xsl:for-each>
						</div>

					<!--Remarks for protosegments and clusters should be transferred.-->
					<h4>Protosegments for this view, in phonetic order:</h4>
					<xsl:for-each select="./protosegments/protosegment">
						<h3>*<xsl:value-of select="@symbol"
						/>      (<xsl:value-of select="@zone-place-manner"/>)</h3>

						<!--Clusters are ordered numerically.-->
						<xsl:for-each select="cluster">
							<xsl:sort order="ascending" select="@cluster-order" data-type="number" />
							<xsl:param name="groupcount" select="group" />

							<xsl:if test="count($groupcount) > 0" >
								<div class="env">Env <xsl:value-of select="@environment"/></div>

								<div class="data">
									<blockquote>
									<!--Groups are ordered by descending Frantz numbers.-->
									<xsl:for-each select="group">
										<xsl:sort select="frantz-strength"
											order="descending" data-type="number" />
										*<xsl:value-of select="@reconstruction" />
										[<xsl:value-of select="format-number (@frantz-strength,
													   '#0.00')"/>]
										"<xsl:value-of select='@gloss' />"

										<!--Wordcorr should put single quotes into datum-varieties
										when there is datum/special-semantics information,
										instead of double quotes. Then we can put @gloss
										(above) back nto single quotes, as most editors prefer.-->
										<xsl:for-each select="datum-varieties">
					 						 <xsl:value-of select="."/> <xsl:text>&#160;&#160;</xsl:text>
											<xsl:value-of select="@datum"/>.
										</xsl:for-each><br />
									</xsl:for-each>
									</blockquote>
								</div>
							</xsl:if>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
