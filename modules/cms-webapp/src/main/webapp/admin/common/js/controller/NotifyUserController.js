Ext.define( 'Common.controller.NotifyUserController', {
    extend: 'Ext.app.Controller',

    stores: [],
    models: [],
    views: [ 'NotifyUserWindow'],

    init: function()
    {
        this.control( {
            'notifyUserWindow *[action=send]':{
                click: this.doSend
            },
            'notifyUserWindow *[action=close]': {
                click: this.closeWindow
            }
        } );
        this.application.on({
            'showNotifyUserWindow ': this.showWindow,
            scope: this
        });
        this.application.on({
            'closeNotifyUserWindow ': this.closeWindow,
            scope: this
        });
    },

    showWindow: function( model ) {
        var notifyUserWindow = this.getNotifyUserWindow();
        notifyUserWindow.doShow( model );
        notifyUserWindow.center();
    },

    closeWindow: function() {
        this.getNotifyUserWindow().setPosition(-5000, -5000);
        this.getNotifyUserWindow().close();
    },

    doSend: function( btn, evt ) {
        var form = btn.up('notifyUserWindow').down('form').getForm();
        if( form.isValid() ) {
            form.submit();
            this.closeWindow();
        } else {
            form.markInvalid();
        }
    },

    getNotifyUserWindow: function()
    {
        var win = Ext.ComponentQuery.query( 'notifyUserWindow' )[0];
        if ( !win ) {
            win = Ext.createWidget( 'notifyUserWindow' );
        }
        return win;
    }

} );
