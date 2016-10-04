CKEDITOR.plugins.add( 'simpleImage',
{
    icons: 'simpleImage', 
    init : function( editor ) {
        editor.addCommand( 'simpleImage', new CKEDITOR.dialogCommand( 'simpleImageDialog' ) );
        editor.ui.addButton( 'simpleImage', {
            label: 'image',
            command: 'simpleImage',
        });
        
        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'imageGroup' );
            editor.addMenuItem( 'imageItem', {
                label: 'Image',
                icon: this.path + 'icons/simpleImage.png',
                command: 'simpleImage',
                group: 'imageGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element) {
                    return { imageItem: CKEDITOR.TRISTATE_OFF };
                }
            });
        };

        CKEDITOR.dialog.add( 'simpleImageDialog', this.path + 'dialogs/simpleImage.js' );
        
        editor.on( 'doubleclick', function( evt ) {
            var element = evt.data.element;

            if ( element.is( 'img' ) && !element.data( 'cke-realelement' ) && !element.isReadOnly() )
                evt.data.dialog = 'simpleImageDialog';
        } );
    }
});
