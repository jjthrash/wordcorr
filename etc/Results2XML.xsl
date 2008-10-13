<?xml version="1.0" encoding="UTF-8"?>
<!--Results2XML collects an XML of Wordcorr analysis results for HTML presentation
	Joseph E. Grimes, 6 October 2005, Makaha, Hawaii
	6 December 2005. Add full variety names. OK.
	3 December 2005. Working on variety names via ancestor path, not working yet.
	26 November 2005. Sort clusters numerically, not as text. View/view-member
	needs to get full name from collection/varieties/variety.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<!--<xsl:param name="variety-list" select="//variety"/>-->

    <!--Document root of output-->
    <xsl:template match="/" >
        <results-display-xml>
            <xsl:apply-templates select="//collection" />
       </results-display-xml>
    </xsl:template>

    <!--Current collection in Wordcorr -->
    <xsl:template match="collection" >
        <collection collection-name="{@name}" collection-remarks="{remarks}" >
            <xsl:apply-templates select="//view"/>
        </collection>
    </xsl:template>

    <!--Each view in the collection (restrict later using a variable)-->
    <xsl:template match="view" >
        <xsl:text>
</xsl:text>
        <view view-name="{@view-name}" view-remarks="{remarks}" >
            <xsl:apply-templates select="view-member">
                <xsl:sort select="@order-number" data-type="number" />
            </xsl:apply-templates>
            <xsl:apply-templates select="results" />
        </view>
    </xsl:template>

    <!--List the members of the view in order-->
	<!--This gets the full variety name from collection/varieties/variety-->
    <xsl:template match="view-member" >
		<xsl:param name="short" select="@short-name" />
 		<view-member short-name="{$short}" >
			<xsl:apply-templates select="//variety[@short-name = $short]" />
		</view-member>
    </xsl:template>

	<xsl:template match="variety" >
		<xsl:value-of select="@name" />
	</xsl:template>

    <!--List the information for each protosegment-->
	<!--Sort cluster numbers numerically, not as text.-->
    <xsl:template match="results/protosegment" >
        <xsl:text>
</xsl:text>
        <protosegment symbol="{@symbol}" proto-remarks="{remarks}" >
        <xsl:apply-templates select="cluster" >
            <xsl:sort select="@cluster-order" data-type="number" />
        </xsl:apply-templates>
        </protosegment>
    </xsl:template>

    <!--For each cluster in a protosegment as ordered, show the environment and remarks-->
    <xsl:template match="cluster" >
        <xsl:text>
    </xsl:text>
        <cluster environment="{@environment}" cluster-order="{@cluster-order}" cluster-remarks="{remarks}" >
            <xsl:apply-templates select="correspondence-set" >
               <xsl:sort select="@ignore-count" />
            </xsl:apply-templates>
        </cluster>
    </xsl:template>

    <xsl:template match="correspondence-set" >
        <xsl:text>
        </xsl:text>
        <c-set glyphs="{glyph-string}" remarks="{remarks}" >
            <xsl:apply-templates select="citation" >
                <xsl:sort select="@entry-number" />
            </xsl:apply-templates>
        </c-set>
    </xsl:template>

    <!--Copy the citations for each correspondence set-->
    <xsl:template match="citation" >
        <xsl:text>
            </xsl:text>
        <xsl:copy-of select="." />
    </xsl:template>

</xsl:stylesheet>