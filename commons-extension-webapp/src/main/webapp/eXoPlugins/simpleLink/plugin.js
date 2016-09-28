CKEDITOR.plugins.add( 'simpleLink',
{
    icons: 'simpleLink', 
    init : function( editor ) {
        editor.addCommand( 'simpleLink', new CKEDITOR.dialogCommand( 'simpleLinkDialog' ) );
        editor.ui.addButton( 'simpleLink', {
            label: 'Link',
            command: 'simpleLink',
        });

        CKEDITOR.dialog.add( 'simpleLinkDialog', this.path + 'dialogs/simpleLink.js' );
    }
});
