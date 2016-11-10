CKEDITOR.plugins.add( 'simpleLink',
{
    icons: 'simpleLink', 
    init : function( editor ) {
        editor.addCommand( 'simpleLink', new CKEDITOR.dialogCommand( 'simpleLinkDialog' ) );
        editor.ui.addButton( 'simpleLink', {
            label: 'Link',
            command: 'simpleLink',
        });
        
        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'linkGroup' );
            editor.addMenuItem( 'linkItem', {
                label: 'Link',
                icon: this.path + 'icons/simpleLink.png',
                command: 'simpleLink',
                group: 'linkGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element) {
                    return { linkItem: CKEDITOR.TRISTATE_OFF };
                }
            });
        };

        CKEDITOR.dialog.add( 'simpleLinkDialog', this.path + 'dialogs/simpleLink.js' );
    }
});
