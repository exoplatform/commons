CKEDITOR.dialog.add( 'simpleImageDialog', function( editor ) {
    function isBase64Image(imageLink) {
        if (!imageLink) return false;
        if (imageLink.match(/^data:image\/([a-zA-Z]+);base64/ig)) {
            return true;
        } else {
            return false;
        }
    }
    var dialogId = Math.floor(Math.random() * 100000);
    return {
        title: 'Select Picture',
        minWidth: 400,
        minHeight: 400,
        resizable: CKEDITOR.DIALOG_RESIZE_NONE,
        contents: [
            {
                id: 'tab',
                label: 'Link',
                elements: [
                    {
                        type: 'text',
                        id: 'imgUrl',
                        label: 'Image URL',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Image URL cannot be empty!" ),
                        setup: function(element) {
                            this.setValue(element.getAttribute("src"));
                        },
                        commit: function(element) {
                            var val = this.getValue();
                            if (isBase64Image(val)) {
                                val = "";
                            }
                            element.setAttribute("src", val);
                        },
                        onChange: function() {
                            var preview = CKEDITOR.document.getById('previewImageId' + dialogId);
                            var imagePreviewLoader = CKEDITOR.document.getById('imagePreviewLoaderId' + dialogId);
                            var imagePreviewLoaderIcon = CKEDITOR.document.getById('imagePreviewLoaderIconId' + dialogId);
                            var imagePreviewError = CKEDITOR.document.getById('imagePreviewErrorId' + dialogId);
                            imagePreviewLoader.setAttribute("style", "background-color: #8D8D8D; margin-left:20%;");
                            imagePreviewError.hide();
                            preview.hide();
                            imagePreviewLoader.show();
                            if (this.getValue() && this.getValue().trim().length > 0) {
                                if (isBase64Image(this.getValue())) {
                                    preview.hide();
                                    imagePreviewLoaderIcon.hide();
                                    imagePreviewError.setHtml('Base64 image is not supported');
                                    imagePreviewError.show();
                                } else {
                                    imagePreviewLoader.show();
                                    imagePreviewLoaderIcon.show();
                                    preview.setAttribute("src", this.getValue());
                                    preview.on("load", function() {
                                        preview.show();
                                        imagePreviewLoader.setAttribute("style", "background-color: white; margin-left:20%;");
                                        imagePreviewLoader.hide();
                                        imagePreviewLoaderIcon.hide();
                                    });
                                    preview.on("error", function() {
                                        preview.hide();
                                        imagePreviewLoaderIcon.hide();
                                        imagePreviewError.setHtml('Error: image url incorrect!');
                                        imagePreviewError.show();
                                    });
                                }
                            } else {
                                imagePreviewLoaderIcon.hide();
                            }
                        }
                    },
                    {
                        type: 'text',
                        id: 'description',
                        label: 'Description',
                        setup: function(element) {
                            this.setValue(element.getAttribute("title") );
                        },
                        commit: function(element) {
                            element.setAttribute("title", this.getValue());
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
                        validate: CKEDITOR.dialog.validate.notEmpty( "Aligment cannot be empty!" ),
                        setup: function(element) {
                            var clazz = element.getAttribute("class");
                            if (clazz === "pull-left") {
                                this.setValue("left");
                            } else if (clazz === "pull-right") {
                                this.setValue("right");
                            }

                        },
                        commit: function(element) {
                            element.setAttribute("class", "pull-" + this.getValue());
                            element.setAttribute("style", "float:" + this.getValue());
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
                                '<div id="' + 'imagePreviewLoaderId' + dialogId + '" class="ImagePreviewLoader" style="background-color: #8D8D8D; margin-left:20%;">' + 
                                    '<div id="' + 'imagePreviewLoaderIconId' + dialogId + '" class="loading" style="background: url(\'/eXoSkin/skin/images/themes/default/Loading/loadingProgressBar.gif\') no-repeat center; display:none; width:100%;height:100%">&nbsp;</div>' +
                                    '<span id="' + 'imagePreviewErrorId' + dialogId + '" class="error" style="display:none; color:red; position:absolute; top: 30%; left: 0; right: 0;text-align: center">Error: image url incorrect!</span>' +
                                '</div>' +
                                '<div style="width:230px;margin-left:20%"><table><tr><td>' +
                                    '<a href="javascript:void(0)" target="_blank" onclick="return false;" id="' + 'previewLinkId' + dialogId + '">' +
                                    '<img style="display:none" id="' + 'previewImageId' + dialogId + '" alt="" /></a>' +
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
                element = element.getAscendant( 'img', true );

            if ( !element || element.getName() != 'img' ) {
                element = editor.document.createElement( 'img' );
//                element.setText(selection.getSelectedText());
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

