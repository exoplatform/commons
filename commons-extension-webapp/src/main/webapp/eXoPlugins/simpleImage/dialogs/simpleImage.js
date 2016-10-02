CKEDITOR.dialog.add( 'simpleImageDialog', function( editor ) {
    return {
        title: 'Select Picture',
        minWidth: 400,
        minHeight: 200,
        contents: [
            {
                id: 'tab',
                label: 'Link',
                elements: [
                    {
                        type: 'text',
                        id: 'imgUrl',
                        label: 'Image URL',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Image URL cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getText());
                        },
                        commit: function(element) {
                            element.setText(this.getValue());
                        }
                    },
                    {
                        type: 'text',
                        id: 'description',
                        label: 'Description',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Description cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getAttribute("href") );
                        },
                        commit: function(element) {
                            element.setAttribute("href", this.getValue());
                        }
                    },
                    {
                        type: 'select',
                        id: 'aligment',
                        label: 'Aligment',
                        widths: [ '35%', '65%' ],
                        style: 'width:90px',
                        'default': 'left',
                        items: [
                            [ editor.lang.common.alignLeft, 'left' ],
                            [ editor.lang.common.alignRight, 'right' ]
                        ],
                        validate: CKEDITOR.dialog.validate.notEmpty( "Aligment cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getAttribute("href") );
                        },
                        commit: function(element) {
                            element.setAttribute("href", this.getValue());
                        }
                    },
                    {
                        type: 'vbox',
                        height: '250px',
                        children: [ {
                            type: 'html',
                            id: 'htmlPreview',
                            style: 'width:95%;',
                            html: '<div>' + CKEDITOR.tools.htmlEncode( editor.lang.common.preview ) + '<br>' +
                                '<div id="' + 'imagePreviewLoaderId' + '" class="ImagePreviewLoader" style="display:none"><div class="loading">&nbsp;</div></div>' +
                                '<div><table><tr><td>' +
                                    '<a href="javascript:void(0)" target="_blank" onclick="return false;" id="' + 'previewLinkId' + '">' +
                                    '<img id="' + 'previewImageId' + '" alt="" /></a>' +
                                // jscs:disable maximumLineLength
                                    ("") +
                                // jscs:enable maximumLineLength
                                '</td></tr></table></div></div>'
                        } ]
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

