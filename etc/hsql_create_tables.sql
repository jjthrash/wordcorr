
-- USERS Table
--   USER_KEY:          User Identifier
--   USER_NAME:         User Name
--   FAMILY_NAME:       Family (Last) Name
--   GIVEN_NAME:        Given (First) Name
--   EMAIL:             Email
--   AFFILIATIONS:      Affiliations
CREATE TABLE USERS (
        USER_KEY INTEGER IDENTITY,
        USER_NAME VARCHAR(8),
        FAMILY_NAME VARCHAR(50),
        GIVEN_NAME VARCHAR(50),
        EMAIL VARCHAR(60),
        AFFILIATIONS LONGVARCHAR
);

-- SETTINGS Table
--   SETTING_KEY:       User Identifier
--   SETTING_ID:        ID
--   SETTING_NAME:      Name
--   USER_KEY:          Current User Identifier (Not a FK)
--   COLLECTION_KEY:    Current Collection Identifier (Not a FK)
--   VIEW_KEY:          Current View Identifier (Not a FK)
--   ENTRY_KEY:         Entry Identifier of last entry seen (Not a FK)
--   PROTO_KEY:         Protosegment Identifier of last protosegment seen (Not a FK)
--   CLUSTER_KEY:       Cluster Identifier of last cluster seen (Not a FK)
CREATE TABLE SETTING (
        SETTING_KEY INTEGER IDENTITY,
        SETTING_ID VARCHAR(20),
        SETTING_NAME VARCHAR(100),
        USER_KEY INTEGER,
        COLLECTION_KEY INTEGER,
        VIEW_KEY INTEGER,
        ENTRY_KEY INTEGER,
        PROTO_KEY INTEGER,
        CLUSTER_KEY INTEGER,
        UNIQUE(SETTING_ID)
);

-- COLLECTION Table
--   COL_KEY:           Collection Identifier
--   USER_KEY:          User Identifier (FK)
--   COL_NAME:          Collection Full Name
--   COL_SHORTNAME:     Collection Short Name
--   COL_CREATOR_ROLE:  Collection Creator Role
--   COL_CREATOR:       Collection Creator (Collection Name [Creator User Name])
--   COL_PUBLISHER:     Collection Publisher (Creator Email)
--   COL_CONTRIBUTOR:   Collection Contributors
--   COL_DESCRIPTION:   Collection Description
--   COL_REMARKS:       Remarks on the Collection
--   GLOSS_LANG:        Primary Gloss Language
--   GLOSS_LANG_CODE:   Primary Gloss Language Code
--   GLOSS2_LANG:       Secondary Gloss Language
--   GLOSS2_LANG_CODE:  Secondary Gloss Language Code
--   KEYWORDS:          Keywords
--   COVERAGE:          Geographic Area Covered
--   PUBLISHED_SOURCE:  Published Sources
--   STABLE_LOCATION:   Stable Copy Location
--   RIGHTS_LICENSE:    Rights Management: License Type
--   RIGHTS_COPYRIGHT:  Rights Management: Year Copyright Asserted
--   EXPORT_TIMESTAMP:	Export Timestamp
CREATE TABLE COLLECTION (
        COL_KEY INTEGER IDENTITY,
        USER_KEY INTEGER,
        COL_NAME VARCHAR(50) NOT NULL,
        COL_SHORTNAME VARCHAR(12),
        COL_CREATOR_ROLE VARCHAR(12),
        COL_CREATOR VARCHAR(70),
        COL_PUBLISHER VARCHAR(60),
        COL_CONTRIBUTOR LONGVARCHAR,
        COL_DESCRIPTION LONGVARCHAR,
        COL_REMARKS LONGVARCHAR,
        GLOSS_LANG VARCHAR(50),
        GLOSS_LANG_CODE VARCHAR(3),
        GLOSS2_LANG VARCHAR(50),
        GLOSS2_LANG_CODE VARCHAR(3),
        KEYWORDS LONGVARCHAR,
        COVERAGE LONGVARCHAR,
        PUBLISHED_SOURCE LONGVARCHAR,
        STABLE_LOCATION VARCHAR(100),
        RIGHTS_MANAGEMENT VARCHAR(20),
        RIGHTS_COPYRIGHT INTEGER,
        EXPORT_TIMESTAMP DATETIME,
        FOREIGN KEY (USER_KEY) REFERENCES USERS ON DELETE CASCADE
);

-- VARIETY Table
--   VARIETY_KEY:       Variety Identifier
--   COL_KEY:           Collection Identifier (FK)
--   VARIETY_NAME:      Full name of the variety
--   VARIETY_SHORTNAME: Short name of the variety
--   VARIETY_ABBR:      Comparativists' abbreviation (for each word list)
--   VARIETY_NAME_ALT:  Alternate variety name list
--   VARIETY_REMARKS:   Remarks
--   ETH_CODE:          Ethnologue code
--   CLASSIFICATION:    Classification
--   QUALITY:           Quality of data
--   LOCALE:            Locale where data were collected
--   SOURCE:            People or publication from whom the data were collected
--   SOURCE_UNPUB:      Source like field notes
--   COUNTRY:           Country where collected
CREATE TABLE VARIETY (
        VARIETY_KEY INTEGER IDENTITY,
        COL_KEY INTEGER,
        VARIETY_NAME VARCHAR(100),
        VARIETY_SHORTNAME VARCHAR(8),
        VARIETY_ABBR VARCHAR(3),
        VARIETY_NAME_ALT LONGVARCHAR,
        VARIETY_REMARKS LONGVARCHAR,
        ETH_CODE VARCHAR(3),
        CLASSIFICATION LONGVARCHAR,
        QUALITY VARCHAR(3),
        LOCALE LONGVARCHAR,
        SOURCE LONGVARCHAR,
        SOURCE_UNPUB VARCHAR(200),
        COUNTRY VARCHAR(50),
        FOREIGN KEY (COL_KEY) REFERENCES COLLECTION ON DELETE CASCADE
);

-- ENTRY Table
--   ENTRY_KEY:         Entry Identifier
--   COL_KEY:           Collection Identifier (FK)
--   ENTRY_NUM:         Entry number in word list
--   GLOSS:             Primary gloss
--   GLOSS2:            Secondary gloss
CREATE TABLE ENTRY (
        ENTRY_KEY INTEGER IDENTITY,
        COL_KEY INTEGER,
        ENTRY_NUM INTEGER,
        GLOSS VARCHAR(50),
        GLOSS2 VARCHAR(50),
        FOREIGN KEY (COL_KEY) REFERENCES COLLECTION ON DELETE CASCADE
);

-- DATAVIEW Table
--   VIEW_KEY:          View Identifier
--   COL_KEY:           Collection Identifier (FK)
--   VIEW_NAME:         View Name
--   USER_KEY:          User Identifier (FK)
--   THRESHOLD:         Threshold percentage (0-100) of varieties required to have a form present
--   VIEW_REMARKS:      Remarks on this view
--   STATUS:            Status
CREATE TABLE DATAVIEW (
        VIEW_KEY INTEGER IDENTITY,
        COL_KEY INTEGER,
        VIEW_NAME VARCHAR(30) NOT NULL,
        USER_KEY INTEGER,
        THRESHOLD INTEGER,
        VIEW_REMARKS LONGVARCHAR,
        STATUS VARCHAR(4),
        FOREIGN KEY (COL_KEY) REFERENCES COLLECTION ON DELETE CASCADE
);

-- DATAVIEW_MEMBER Table
--   VIEW_MEMBER_KEY:   Member Identifier
--   VIEW_KEY:          View Identifier (FK)
--   VARIETY_KEY:       Variety Identifier (FK)
--   ORDER_NUM:         Order within view
CREATE TABLE DATAVIEW_MEMBER (
        VIEW_MEMBER_KEY INTEGER IDENTITY,
        VIEW_KEY INTEGER,
        VARIETY_KEY INTEGER NOT NULL,
        ORDER_NUM INTEGER,
        FOREIGN KEY (VIEW_KEY) REFERENCES DATAVIEW ON DELETE CASCADE,
        FOREIGN KEY (VARIETY_KEY) REFERENCES VARIETY ON DELETE CASCADE
);

-- FORM_GROUP Table
--   GROUP_KEY:         Group Identifier
--   TAG:               1-4 character group tag
--   RECONSTRUCTION:    Reconstruction from forms in that group
--   FRANTZ_CLUSTER:    Frantz number calculated over reconstructon positions for cluster
--   FRANTZ_PROTO:      Frantz number calculated over reconstructon positions for protosegment
--   ALL_CITATIONS_RES: All citations associated with residue protosegment
--   IS_DONE:           Has this group been tabulated yet? (1=yes, 0=no)
--   VIEW_KEY:          View Identifier (FK)
--   ENTRY_KEY:         Entry Identifier (FK)
CREATE TABLE FORM_GROUP (
        GROUP_KEY INTEGER IDENTITY,
        VIEW_KEY INTEGER,
        ENTRY_KEY INTEGER,
        TAG VARCHAR(4),
        RECONSTRUCTION VARCHAR(250),
        FRANTZ_CLUSTER DOUBLE,
        FRANTZ_PROTO DOUBLE,
        ALL_CITATIONS_RES SMALLINT,
        IS_DONE SMALLINT,
        FOREIGN KEY (VIEW_KEY) REFERENCES DATAVIEW ON DELETE CASCADE,
        FOREIGN KEY (ENTRY_KEY) REFERENCES ENTRY ON DELETE CASCADE
);

-- DATUM Table
--   DATUM_KEY:         Datum Identifier
--   VARIETY_KEY:       Variety Identifier (FK)
--   ENTRY_KEY:         Entry Identifier (FK)
--   RAW_DATUM:         Raw datum (IPA transcription).
--   SPECIAL_SEMANTICS: Semantics when meaning matches the entry meaning
--                      poorly, yet form matches
--   DATUM_REMARKS:     Remarks on this datum
CREATE TABLE DATUM (
        DATUM_KEY INTEGER IDENTITY,
        VARIETY_KEY INTEGER,
        ENTRY_KEY INTEGER,
        RAW_DATUM VARCHAR(70),
        SPECIAL_SEMANTICS LONGVARCHAR,
        DATUM_REMARKS LONGVARCHAR,
        FOREIGN KEY (VARIETY_KEY) REFERENCES VARIETY ON DELETE CASCADE,
        FOREIGN KEY (ENTRY_KEY) REFERENCES ENTRY ON DELETE CASCADE
);

-- ZONE Table
--   ZONE_KEY:          Zone Identifier
--   ZONE_ROW:          Row in stylized phonetic chart
--   ZONE_COL:          Column in stylized phonetic chart
--   ZONE_ABBR:         Zone abbreviation (e.g. "LabObs")
--   ZONE_NAME:         Zone name (e.g. "Labial Obstruents")
--   ZONE_TYPE:         Zone type (e.g. p, i)
CREATE TABLE ZONE (
        ZONE_KEY INTEGER IDENTITY,
        ZONE_ROW INTEGER,
        ZONE_COL INTEGER,
        ZONE_ABBR VARCHAR(10),
        ZONE_NAME VARCHAR(50),
        ZONE_TYPE VARCHAR(3)
);

-- PROTOSEGMENT Table
--   PROTO_KEY:         Protosegment Identifier
--   VIEW_KEY:          View Identifier (FK)
--   ZONE_KEY:          Zone Identifier (FK)
--   PROTOSEGMENT:      Protosegment symbol without "*"
--   PROTO_REMARKS:     Remarks on this protosegment
CREATE TABLE PROTOSEGMENT (
        PROTO_KEY INTEGER IDENTITY,
        VIEW_KEY INTEGER,
        ZONE_KEY INTEGER,
        PROTOSEGMENT VARCHAR(5),
        PROTO_REMARKS LONGVARCHAR,
        FOREIGN KEY (VIEW_KEY) REFERENCES DATAVIEW ON DELETE CASCADE
);

-- CLUSTER Table
--   CLUSTER_KEY:       Cluster Identifier
--   PROTO_KEY:         Protosegment Identifier (FK)
--   ENVIRONMENT:       Environmental symbol like #_V where the set goes in _
--   CLUSTER_REMARKS:   Remarks on cluster
--   CLUSTER_ORDER:     Order of this cluster within protosegment
CREATE TABLE CLUSTER (
        CLUSTER_KEY INTEGER IDENTITY,
        PROTO_KEY INTEGER NOT NULL,
        ENVIRONMENT VARCHAR(50),
        CLUSTER_REMARKS LONGVARCHAR,
        CLUSTER_ORDER INTEGER NOT NULL,
        FOREIGN KEY (PROTO_KEY) REFERENCES PROTOSEGMENT ON DELETE CASCADE
);

-- CORRESPONDENCE_SET Table
--   SET_KEY:           Set Identifier
--   CLUSTER_KEY:       Cluster Identifier (FK)
--   CORR_SET:          Correspondence set like 'pp.ppb/pp'
--   VARIETY_COUNT:     Number of varieties in this set (exclude ignore, count indel)
--   SET_ORDER:         Set order within cluster
--   SET_REMARKS:       Remarks on this correspondence set
CREATE TABLE CORRESPONDENCE_SET (
        SET_KEY INTEGER IDENTITY,
        CLUSTER_KEY INTEGER NOT NULL,
        CORR_SET VARCHAR(254) NOT NULL,
        VARIETY_COUNT INTEGER,
        SET_REMARKS LONGVARCHAR,
        SET_ORDER INTEGER NOT NULL,
        FOREIGN KEY (CLUSTER_KEY) REFERENCES CLUSTER ON DELETE CASCADE
);

-- CITATION Table
--   CITATION_KEY:      Citation Identifier
--   SET_KEY:           Correspondence Set Identifier (FK)
--   GROUP_KEY:         Group Identifier (FK)
--   POSITION:          Position within aligned group
CREATE TABLE CITATION (
        CITATION_KEY INTEGER IDENTITY,
        SET_KEY INTEGER,
        GROUP_KEY INTEGER,
        POSITION INTEGER,
        FOREIGN KEY (SET_KEY) REFERENCES CORRESPONDENCE_SET ON DELETE CASCADE,
        FOREIGN KEY (GROUP_KEY) REFERENCES FORM_GROUP ON DELETE CASCADE
);

-- ALIGNMENT Table
--   ALIGNMENT_KEY:     Alignment Identifier
--   VIEW_MEMBER_KEY:   View Member Identifier (FK)
--   GROUP_KEY:         Group Identifier (FK)
--   DATUM_KEY:         Datum Identifier (FK)
--   VECTOR:            Alignment vector {x=datum, /=indel, i=ignore}
--   METATHESIS1:       Metathesis start position
--   LENGTH1:           Length of first metathesized section
--   METATHESIS2:       Metathesis second position
--   LENGTH2:           Length of second metathesized section
--   SEMANTIC_SHIFT:    Entry number of another entry whose raw datum should be used
--                      in the alignment, for semantic shift
--   OBSERVATION:       Observations about this datum in this view
CREATE TABLE ALIGNMENT (
        ALIGNMENT_KEY INTEGER IDENTITY,
        VIEW_MEMBER_KEY INTEGER,
        GROUP_KEY INTEGER,
        DATUM_KEY INTEGER,
        VECTOR VARCHAR(140),
        METATHESIS1 INTEGER,
        LENGTH1 INTEGER,
        METATHESIS2 INTEGER,
        LENGTH2 INTEGER,
        SEMANTIC_SHIFT INTEGER,
        OBSERVATION LONGVARCHAR,
        FOREIGN KEY (VIEW_MEMBER_KEY) REFERENCES DATAVIEW_MEMBER ON DELETE CASCADE,
        FOREIGN KEY (GROUP_KEY) REFERENCES FORM_GROUP ON DELETE CASCADE,
        FOREIGN KEY (DATUM_KEY) REFERENCES DATUM ON DELETE CASCADE
);

