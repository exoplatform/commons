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
                        validate: CKEDITOR.dialog.validate.notEmpty( "Text field cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getText());
                        },
                        commit: function(element) {
                            element.setText(this.getValue());
                        }
                    },
                    {
                        type: 'text',
                        id: 'link',
                        label: 'Link',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Link field cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getAttribute("href") );
                        },
                        commit: function(element) {
                            var url = this.getValue();
                            if (url && !url.match(/^(\/|((https?|ftp|file):\/\/))/ig)) {
                                url = "http://" + url;
                            }
                            element.setAttribute("href", url);
                        }
                    }
                ]
            }
        ],
        
        onShow: function() {
            var selection = editor.getSelection();
            var element = selection.getStartElement();

            if ( element )
                element = element.getAscendant( 'a', true );

            if ( !element || element.getName() != 'a' ) {
                element = editor.document.createElement( 'a' );
                element.setText(selection.getSelectedText());
                this.insertMode = true;
            }
            else
                this.insertMode = false;

            this.element = element;
//            if (!this.insertMode )
                this.setupContent( this.element );
        },
        
        onOk: function() {
            var dialog = this;
            var a = this.element;
            this.commitContent(a);

            if ( this.insertMode )
                editor.insertElement(a);
        }
    };
});

