CKEDITOR.dialog.add( 'simpleLinkDialog', function( editor ) {
    return {
        title: 'Link',
        minWidth: 400,
        minHeight: 200,
        contents: [
            {
                id: 'tab',
                label: 'Link',
                elements: [
                    {
                        type: 'textarea',
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

            var simpleLink = editor.document.createElement( 'a' );
            simpleLink.setAttribute( 'href', dialog.getValueOf( 'tab', 'link' ) );
            simpleLink.setText( dialog.getValueOf( 'tab', 'text' ) );

            editor.insertElement(simpleLink);
        }
    };
});

