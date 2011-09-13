Ext.define( 'CMS.view.user.EditUserPanel', {
    extend: 'Ext.form.Panel',
    alias: 'widget.editUserPanel',

    requires: [
        'CMS.view.user.EditUserPropertiesPanel',
        'CMS.view.user.EditUserPreferencesPanel',
        'CMS.view.user.EditUserMembershipPanel',
        'CMS.view.user.UserPreferencesPanel'
    ],

    autoScroll: true,

    items: [],
    currentUser: '',

    bodyStyle: {
        background: 'white'
    },

    layout: {
        type: 'border'
    },

    border: 0,

    initComponent: function()
    {
        var me = this;
        var editUserFormPanel = {
            xtype: 'editUserFormPanel',
            region: 'center',
            userFields: [],
            margin: 5,
            autoScroll: true,
            currentUser: me.currentUser,
            flex: 1
        };
        var prefPanel = {
            xtype: 'userPreferencesPanel',
            region: 'east',
            userFields: me.userFields,
            margin: 5,
            flex: 0.2
        };
        var headerPanelTpl = Ext.Template('<div class="cms-edit-form-header clearfix">' +
            '<div class="left">' +
            '<img alt="User" src="data/user/photo?key={key}"/></div>' +
            '<div class="right">' +
            '<h1>{displayName}</h1><a href="javascript:;" class="edit-button"></a>' +
            '<p>{qualifiedName}</p></div></div>');
        var headerPanel = {
            xtype: 'panel',
            tpl: headerPanelTpl,
            border: 0,
            region: 'north',
            styleHtmlContent: true,
            data: me.currentUser
        };
        var shortDescPanel = {
            xtype: 'panel',
            layout: {
                type: 'border'
            },
            bodyStyle: {
                background: 'lightGrey'
            },
            region: 'north',
            measureWidth: true,
            height: 100,
            margin: 5,

            items: [
                {
                    xtype: 'image',
                    cls: 'thumbnail',
                    region: 'west',
                    width: 100,
                    height: 100,
                    src: 'data/user/photo?key=' + me.currentUser.key
                },
                {
                    layout: 'border',
                    region: 'center',
                    bodyStyle: {
                        background: 'lightGrey'
                    },
                    flex: 1,
                    items: [
                        {
                            xtype: 'userFormField',
                            type: 'text',
                            region: 'center',
                            fieldname: 'display-name',
                            currentUser: me.currentUser,
                            required: true,
                            fieldValue: me.currentUser.displayName,
                            flex: 1
                        },
                        {
                            xtype: 'displayfield',
                            region: 'south',
                            flex: 0.25,
                            value: me.currentUser.qualifiedName
                        }
                    ]
                }
            ]};
        if ( this.userFields != null )
        {
            editUserFormPanel.userFields = this.userFields;
        }
        me.items = [ editUserFormPanel, prefPanel, headerPanel ];
        this.callParent( arguments );

    }

} );

