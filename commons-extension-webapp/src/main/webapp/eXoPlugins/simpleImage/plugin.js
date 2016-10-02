CKEDITOR.plugins.add( 'simpleImage',
{
    icons: 'simpleImage', 
    init : function( editor ) {
        editor.addCommand( 'simpleImage', new CKEDITOR.dialogCommand( 'simpleImageDialog' ) );
        editor.ui.addButton( 'simpleImage', {
            label: 'Link',
            command: 'simpleImage',
        });
        
        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'linkGroup' );
            editor.addMenuItem( 'linkItem', {
                label: 'Link',
                icon: this.path + 'icons/simpleImage.png',
                command: 'simpleImage',
                group: 'linkGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element) {
                    return { linkItem: CKEDITOR.TRISTATE_OFF };
                }
            });
        };

        CKEDITOR.dialog.add( 'simpleImageDialog', this.path + 'dialogs/simpleImage.js' );
    }
});
