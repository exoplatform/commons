CKEDITOR.dialog.add( 'simpleLinkDialog', function( editor ) {
    return {
        title: 'Link',
        minWidth: 400,
        minHeight: 200,
        contents: [
            {
                id: 'tab-basic',
                label: 'Link',
                elements: [
                    {
                        type: 'text',
                        id: 'text',
                        label: 'Text',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Text field cannot be empty." )
                    },
                    {
                        type: 'text',
                        id: 'link',
                        label: 'Link',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Link field cannot be empty." )
                    }
                ]
            }
        ],
        onOk: function() {
            var dialog = this;

            var simpleLink = editor.document.createElement( 'simpleLink' );
            simpleLink.setAttribute( 'text', dialog.getValueOf( 'tab-basic', 'text' ) );
            simpleLink.setText( dialog.getValueOf( 'tab-basic', 'link' ) );

            editor.insertElement(simpleLink);
        }
    };
});

