CKEDITOR.dialog.add( 'simpleLinkDialog', function( editor ) {
    return {
        title: 'Link',
        minWidth: 400,
        minHeight: 200,
        resizable: CKEDITOR.DIALOG_RESIZE_NONE,
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
                        },
                        onLoad : function () {
                            this.getInputElement().$.className = ''; 
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
                        },
                        onLoad : function () {
                            this.getInputElement().$.className = ''; 
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
        },

        onLoad : function () {
            var dialog = this.getElement();
            var dialogCover = document.getElementsByClassName('cke_dialog_background_cover')[0];

            dialog.removeClass('cke_reset_all').$.className += ' uiPopup cke_dialog simpleLinkDialog';
            dialog.findOne('.cke_dialog_ui_button_ok').$.className = 'btn btn-primary';
            dialog.findOne('.cke_dialog_ui_button_cancel').$.className = 'btn';
            dialog.findOne('.cke_dialog_footer').$.className = 'uiActionBorder';
            dialog.findOne('.cke_dialog_title').$.className = 'popupHeader';
            dialog.findOne('.cke_dialog_close_button').$.className = 'uiIconClose cke_dialog_close_button';
            dialogCover.className = 'uiPopupWrapper';
            dialogCover.style.backgroundColor = '';
            dialogCover.style.opacity = '';
        }
    };
});

