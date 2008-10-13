bmyWordCorr Release 2.0
Copyright (c) 2002 DataHouse, Inc. All rights reserved.

___________________
SYSTEM REQUIREMENTS

-Java JRE version 1.3 or higher.

_________________
INSTALLING WordCorr
- Installation instructions can be found in the Appendix of the User's Guide
  (http://sourceforge.net/projects/wordcorr/).
- Adobe Acrobat Reader is necessary to read the User's Guide and can be
  downloaded from http://www.adobe.com/products/acrobat/readstep2.html.

_________________
NOTE FOR PREVIOUS Users
- If you are working from release 1.3.8 or later,
  DO NOT DELETE ANY files in your home .wordcorr directory prior to using this
  release. If you are working from an earlier release, delete all four files.

_______
HISTORY
-11/4/2004 - Release 2.0
 * Updated Wordcorr Help file.
-10/26/2004 - Release 1.3.10
 * Fixed problem with AnnotatePane tag and TabulatePane environment columns
   not having IPA Dialog functionality.
 * Fixed problem with dialog box dimensions.
 * Changed Collection creator and publisher data exported to wordcorr file.
 * Add Wordcorr Help function to Help menu.
 * Fixed problem with RefinePane merge clusters function not working.
 * Fixed problem with correspondence set variety count calculation to take into
   account grapheme clusters.
 * Fixed problem with AnnotatePane losing group tag when leaving pane directly.
 * Fixed problem with ViewsPane copy function not working.
 * Fixed problem of VarietiesPane locale field having 100 character limitation.
 * Fixed problem of exporting collection name instead of user name for collection
   creator field.
=-10/22/2004 - Release 1.3.9
 * Fixed problem with RefinePane SummarizeEvidence maximum reconstruction handling.
-10/22/2004 - Release 1.3.8
 * Add .xml extension to RefinePane SummarizeEvidence function if no extension
   specified.
 * Add Export Metadata XML function.
 * Add creator and publisher metadata to Collection and all affected objects.
 * Fixed problem of not closing dialog boxes with escape key.
 * Fixed problem with AnnotatePane identifying grapheme cluster candidates when
   editing datum.
 * Fixed problem with RefinePane move set function of deleting correspondence set
   when moving to identical cluster.
 * Fixed problem with RefinePane move set function of not taking into account
   grapheme clusters when determining conformability.
 * Fixed problem with Export View XML function due to metadata export modifications.
 * Fixed problem with AnnotatePane buttons by allowing wrapping of buttons to
   multiple rows.
 * Fixed problem with cluster definition in RefinePane Merge Cluster function.
 * Renamed view Cluster Count to Groups Tabulated.
 * Changed dialog box height to application window height.
 * Fixed problem with Quality field in Add Variety dialog box.
 * Fixed problem importing Wordcorr export file with blank
   rights-management-year-copyright-asserted property of collection tag.
-10/1/2004 - Release 1.3.7
 * Add metadata fields to database.
 * Changed CollectionPane to capture metadata.
 * Changed VarietyPane to capture metadata.
 * Changed ViewPane to capture metadata.
 * Add metadata to new (version 2) WordCorr export file.
 * Allow import of both old (prior to version 2) and new WordCorr export files.
 * Fixed duplicate mnemonic keys in panes.
 * Fixed problem of clearing Integer text fields.
 * Add collection export timestamp to status.
-9/14/2004 - Release 1.3.6
 * Changed DataPane to allow direct editing of Datum cell.
 * Fixed problem with collection node not restoring after application restart.
 * Changed AnnotatePane to allow direct editing of Tag cell.
 * Changed labels in UserPane.
 * Removed replication fields from database.
-7/14/2004 - Release 1.3.5
 * Changed User objects to Setting.
 * Changed Project objects to User.
 * Changed UserPane to capture user metadata.
-6/17/2004 - Release 1.3.4
 * Fixed problem with Import XML files with .xml extension.
 * Changed Import Wordsurv dialog box labels.
-5/12/2004 - Release 1.3.3
 * Changed Export XML to always have .zip extension.
 * Changed Export XML to have zip entry with same prefix as filename.
 * Fixed problem with datum-varieties element not associated with group appearing in
   Summary XML file.
 * Fixed problem with Summarize Evidence not accepting Minimum Frantz Number < 0.
-4/22/2004 - Release 1.3.2
 * Changed Export XML to generate compressed zip file.
 * Changed Export XML to automatically append .zip extension if missing from input filename.
 * Changed Import XML to accept zip file and use first zip entry.
-4/20/2004 - Release 1.3.1
 * Add datum attribute to datum-varieties element of output Summary XML file.
-4/16/2004 - Release 1.3
 * Add Summarize Evidence to RefinePane to output Summary XML file.
 * Fixed problem with not being able to enter negative numbers in text boxes.
-2/24/2004 - Release 1.1.2
 * Fixed problem with XML import of files that does not contain tabulated-groups element.
-2/24/2004 - Release 1.1.1
 * Fixed problem with aligned column display of tables using grapheme clusters.
 * Changed DataPane position to the left of ViewPane.
 * Changed RefinePane row colors to repeat after first cluster.
 * Fixed problem with XML export/import of tabulated groups below threshold.
 * Changed ViewPane to prevent duplicate view names.
 * Fixed problem with font style persistence.
 * Fixed problem with scrollbars to ensure selected item is visible.
 * Changed RefinePane move set function to specify protosegment and environment.
-2/4/2004 - Release 1.1
 * Changed TabulatePane to align alignments based on grapheme cluster lengths.
 * Changed AnnotatePane to align alignments based on grapheme cluster lengths.
 * Add Grapheme Clusters list to ViewPane.
 * Add Undefine Grapheme Cluster function to ViewPane.
 * Updated IPA list to include Diacritical Marks.
 * Fixed table row height to account for Diacritical Marks by adding Wordcorr property.
-1/21/2004 - Release 1.0.4
 * Add Grapheme Cluster definition function in EditAlignment dialog box.
 * Add Grapheme Cluster uncluster function in EditAlignment dialog box.
 * Add Grapheme Cluster recognize function when opening EditAlignment dialog box.
 * Fixed Add Variety problem with undefined abbreviations.
 * Changed RefinePane to align correspondence sets based on grapheme cluster lengths.
 * Changed TabulatePane to align correspondence sets based on grapheme cluster lengths.
 * Changed Correspondence Set generation and definition to incorporate grapheme clusters.
 * Changed metathesis algorithm to incorporate grapheme clusters.
 * Changed color of grapheme clusters to alternate depending on position.
-12/9/2003 - Release 1.0.3
 * Changed DataPane, AnnotatePane, TabulatePane and RefinePane to enable glyph alignment
   for datum, annotation and correspondence set columns.
 * Changed IPA dialog box to contain multiple columns.
-11/26/2003 - Release 1.0.2
 * Fixed RefinePane Delete Protosegment query problem.
 * Fixed ViewsPane Varieties In View scrollbar to ensure selected item is visible.
 * Changed TabulatePane below threshold messages.
 * Changed sort order of alignments in AnnotatePane to dynamically change based upon
   group and variety.
 * Changed last pane from TabulatePane to AnnotatePane when exiting program.
 * Changed RefinePane correspondence set column label.
-11/9/2003 - Release 1.0.1
 * Fixed problem with VarietyPane AddDialog box by allowing different form from one
   used in VarietyPane.
 * Fixed font change problem of moving to CollectionPane after refresh.
 * Add cluster count to ViewPane.
 * Add enhanced validation of view changes with tabulation.
 * Changed VarietyPane to prevent duplicate varieties from being added.
 * Changed VarietyPane message and label.
 * Removed Refresh button from toolbar and View menu item.
 * Fixed AnnotatePane Metathesis row indicator for null values.
 * Changed AnnotatePane abbreviation column label and width.
-10/21/2003 - Release 1.0.0
 * Add remarks indicator column in RefinePane.
 * Add Protosegment remarks to RefinePane Edit Remarks function.
 * Add remarks Add Protosegment functions.
 * Add remarks to Protosegment table and XML file formats.
 * Fixed view change problem with TabulatePane where correspondence sets did not reflect
   properties of new view after Tabulate refresh.
 * Changed selected pane to CollectionPane when Collection is changed.
 * Changed Delete Collection warning message.
 * Changed RefinePane Edit Remarks type label.
 * Changed Zone labels to Place and Manner.
 * Fixed RefinePane refresh problem when deleting project.
 * Fixed RefinePane Correspondence Set display grouping problem by adding set sort.
-10/15/2003 - Release 0.9.9
 * Add RefinePane Retabulate Group function.
 * Changed Delete Project warning message.
-9/10/2003 - Release 0.9.8
 * Fixed tabulate conformable test problem to include all correspondence sets instead
   of one or more.
 * Fixed RefinePane Move Correspondence Set conformable test problem to include all
   correspondence sets instead of one or more.
 * Changed Edit Alignment Data Observations label to Remarks.
 * Add RefinePane Edit Remarks function.
 * Add Special Semantics and Remarks indicator column to DataPane.
 * Changed DataPane column widths.
 * Add Metathesis and Remarks indicator column to AnnotatePane.
 * Changed AnnotatePane column widths.
-8/13/2003 - Release 0.9.7
 * Fixed autosave validation problem.
 * Add entry number and primary gloss to Datum dialog box.
 * Add XML Export for Current and Original View.
 * Add reorder message to VarietiesPane.
 * Add invalid character message in Edit Alignment Data dialog box.
 * Fixed bypass of validation by closing edit dialog box directly.
-7/25/2003 - Release 0.9.6 - Alpha Release
 * Changed table row height to be 1.1 times font size.
 * Fixed problem of Project and Collection recreation after deletion.
-7/17/2003 - Release 0.9.5a - Alpha Release
 * Fixed problem saving datum when entry creation is cancelled.
-7/16/2003 - Release 0.9.5 - Alpha Release
 * Fixed Exit problem when data unsucessfully loaded.
 * Changed RefinePane move correspondence set functionality to move to new cluster when
   protosegment does not have matching cluster.
 * Fixed problem with table row height sizing with font size changes.
 * Add labels to ViewsPane source and destination boxes.
 * Changed ViewsPane component sizes to allow more space for source and destination boxes.
 * Fixed TabulatePane problem with missing alignment group.
 * Changed TextArea boxes to line wrap.
 * Changed TabulatePane to not require remarks in dialog box.
 * Fixed problem opening application in Project pane.
 * Add delete Project function.
 * Removed creation of 'res' and 'pro' protosegments and all references in queries.
 * Changed no space field validation to include appending spaces.
-7/14/2003 - Release 0.9.4 - Alpha Release
 * Fixed missing RefinePane problem.
 * Automatically save entry after Replace Vector function in AnnotatePane.
-7/14/2003 - Release 0.9.3 - Alpha Release
 * Add delete WordCollection function.
 * Add AnnotatePane copy and replace vector functions.
 * Add autosave function.
 * Removed Revert and Refresh buttons on panes except TabulatePane.
 * Removed Revert button on Tabulate pane.
 * Removed refreshing of entry list when selecting TabulatePane.
 * Add panel and entry persistence across application sessions.
 * Fixed DatabasePane refresh problem when changing font.
 * Add consistent entry selection across DataPane, AnnotatePane and TabulatePane.
 * Changed User table and class to consolidate DATA_ENTRY_KEY and ANALYZE_ENTRY_KEY to ENTRY_KEY.
 * Reorganized panes and changed PropertyPane label to "Collection" and ViewPane to "View".
 * Changed Show/Hide Tree labels to Show/Hide Projects.
 * Changed Validate button label to "Missing Data Check".
-5/17/2003 - Release 0.9.2 - Alpha Release
 * Changed Import/Export XML menu activation to bypass dialog box.
 * Changed XML file format to accommodate multiple datum ambiguity for alignments.
 * Changed correspondence set construction in TabulatePane.
 * Add Data Validate function.
 * Fixed DataPane problem with Revert of deleted datums after Save causing original list to appear.
-5/15/2003 - Release 0.6.3 - Pre-Alpha Release
 * Fixed problem with ViewPane Copy where duplicate groups should be based on entry not view.
 * Add TabulatePane threshold based on Correspondence Set variety count.
 * Removed id numbers from displays.
-5/11/2003 - Release 0.6.2 - Pre-Alpha Release
 * Fixed import xml failure bug.
-5/9/2003 - Release 0.6.1 - Pre-Alpha Release
 * Modified RefinePane colors.
 * Add XML Export.
 * Add XML Import.
 * Changed project and collection icons.
 * Add RefinePane message for merge clusters when no change is made.
 * Fixed RefinePane select box for merge clusters and move correspondence set.
 * Variety short name required.
 * Add reorder of entry numbers to insure data integrity for WordSurv import and entry deletion.
 * Changed import to Wordsurv Import.
 * Wordsurv import uses variety abbreviation for short name.
 * Changed dirty object indicator to appear on left side of chooser.
 * Confirm datum deletion in DataPane.
 * Add prompt to mark all groups below threshold in TabulatePane.
 * Beginning spaces in datum prevented.
 * Shortcut key message placed in Datum dialog.
-4/25/2003 - Release 0.5.5 - Pre-Alpha Release
 * Check correspondence set lengths for isConformable function.
 * Sort protosegment queries by zone row and column.
 * Fixed problem with tabulate new cluster, cluster order assignment.
 * Prevent duplicate protosegments from being created.
 * Cluster reorder added in tabulate and refine panes added.
 * Add RefinePane add protosegment functionality.
 * Add RefinePane delete protosegment functionality.
 * Add RefinePane display citations functionality.
 * Fixed problem with label contents not displaying.
 * Add RefinePane merge cluster functionality.
 * RefinePane simple multi-row processing.
 * Add RefinePane merge protosegments.
 * Add RefinePane move protosegment.
 * Add RefinePane change protosegment.
 * Protosegment now able to display zone.
 * Add RefinePane row colors.
 * Add RefinePane double click citation display.
 * Add RefinePane recorder clusters.
 * Add button row wrapping.
 * Changed RefinePane button labels.
 * Updated IPA list.
-3/29/2003 - Release 0.5.3 - Pre-Alpha Release
 * Add RefinePane move cluster functionality.
 * Add RefinePane move correspondence set functionality.
 * Add RefinePane change environment functionality.
 * Fixed problem with Import ignore symbol.
 * Fixed problem with tabulation correspondence set processing.
 * Variety Count computed with correspondence set.
-02/20/2003 - Release 0.5.2 - Pre-Alpha Release
 * View Copy added.
 * New view initialized with "?" group and initial alignment.
 * Restores last view when loading the application.
-02/19/2003 - Release 0.5.1 - Pre-Alpha Release
 * Fixed problem with updating view members when saving.
 * View requirement of at least 2 members added.
 * Persistent object validation checking added.
-02/18/2003 - Release 0.5.0 - Pre-Alpha Release
 * Fixed problem with the Add Protosegment dialog, where the currently
   selected drop-down box was not being refreshed after adding a protosegment.
 * Fixed problem with Tabulate Pane, where if you filled in all parameters
   but still had the cursor in an environment box and clicked Save, it
   would say that you still had to fill in more information.
 * Fixed problem with the Edit Alignment dialog box, where if you open
   an entry that does not have a group selected, it shows that the
   first group is selected.
 * Improved the loading time of the Annotate and Data panes by lazily loading
   entry data. This results in a small delay when selecting an Entry, but
   a noticeable speedup in loading the page.
 * Improved the loading time of the Tabulate pane by lazily loading
   entry correspondence set data.
 * Fixed issue where when you save something in the right hand pane, the
   list in the left hand pane was being completely reloaded unnecessarily.
 * Fixed problem where when you start WordCorr, the last selected
   project/collection shows up in the right pane but is not selected in the
   left pane.
 * Do not allow user to select Import unless a project is selected.
 * Fixed problem where wait cursor did not show up the first time you visit a pane.
 * Fixed problem where creating a new view defaults the threshold to 50%  but
   does not actually save that value.
 * Fixed problem preventing tabulation of Original view.
 * Fixed tabulate group alignment length problem when initial groups must be
   skipped.  Group alignment lengths were errorneously calculated.
 * Tabulate group alignment length error message modified to concatenate
   multiple groups into single message.
 * Tabulate displays No Groups Available message when groups do not meet
   alignment requirements.
 * Changed required message for tabulate pane.
 * Annotate deletes unused groups.
 * Tabulate deletes unused protosegments except for 'pro' and 'res'.
 * Zone order changed to use row and column.
 * Make new collection current after import.
 * Remaining groups that are below threshold requirements are marked done
   when tabulating entries.
 * Message displayed in tabulate when all groups are below threshold requirements.
 * Newly created collection is now set active after import.
 * Tabulation results report added to Refine Pane.
-01/24/2003 - Release 0.4.6 - Pre-Alpha Release
 * Fixed database creation bug.
-01/21/2003 - Release 0.4.5 - Pre-Alpha Release
 * Fixed minimum value default that caused error for 0 metathesis value.
 * Changed Find button labels.
 * Extract alignment from datum entry.
 * Check for equal group lengths as precondition to tabulation.
 * Fixed shortcut key bug for IPA popup.
-01/10/2003 - Release 0.4.0 - Pre-Alpha Release.
 * Import Wordsurv bug fix.  Calculate glyph length by groups for each entry.
 * Object names are now required fields.
 * Required field labels appended with *.
 * Nospace validation added to fields.
 * Add new variety to Original View.
 * Prevent deletion of Original View.
 * Update view list when view added or deleted from Manage Views pane.
 * Data pane sorts datum by Original View.
 * Varieties pane sorts varieties by Original View.
 * Preload Zone table data.
 * Remove Add Zone capability.
 * Default protosegments created with view.
 * Removed ViewChooser functionality.
 * Modified frame status message.
 * Status message reflects current view and db.
-12/24/2002 - Release 0.3.0 - Pre-Alpha Release.
 * Import Wordsurv functionality completed.
 * Tabulate save function modified to reflect UC-18.
 * Fixed new database file creation bug.
-12/13/2002 - Release 0.2.0 - Pre-Alpha Release.
 * Tabulate functionality added.
 * Popup dialog positioning fix.
 * Status field added to DATAVIEW table.
-11/13/2002 - Release 0.1.0 - Pre-Alpha Release.
