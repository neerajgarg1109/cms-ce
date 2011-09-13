Ext.define( 'App.view.NewUserPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.newUserPanel',

    requires: [
        'App.view.EditUserPropertiesPanel',
        'App.view.EditUserPreferencesPanel',
        'App.view.EditUserMembershipPanel'
    ],

    autoScroll: true,

    defaults: {
        bodyPadding: 10
    },
    items: [],
    currentUser: '',



    modal: true,

    layout: {
        type: 'border'
    },

    initComponent: function()
    {
        var me = this;
        var editUserFormPanel = {
            xtype: 'editUserFormPanel',
            userFields: [],
            autoScroll: true,
            currentUser: me.currentUser
        };
        var tabPanel = {
            xtype: 'tabpanel',
            region: 'center',
            items: [
                //editUserFormPanel,
                {
                    xtype: 'editUserPropertiesPanel'
                },
                {
                    xtype: 'editUserMembershipPanel'
                },
                {
                    xtype: 'editUserPreferencesPanel'
                }
            ]
        };
        if ( this.userFields != null )
        {
            editUserFormPanel.userFields = this.userFields;
        }
        me.items = [ tabPanel ];
        this.callParent( arguments );

    }

} );

