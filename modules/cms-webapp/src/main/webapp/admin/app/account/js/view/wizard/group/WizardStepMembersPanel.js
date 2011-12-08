Ext.define( 'App.view.wizard.group.WizardStepMembersPanel', {
    extend: 'Ext.form.Panel',
    alias : 'widget.wizardStepMembersPanel',

    requires: [ 'Common.BoxSelect' ],
    border: false,

    initComponent: function()
    {
        this.items = [
            {
                xtype: 'fieldset',
                title: 'Members',
                padding: '10px 15px',
                defaults: {
                    width: 600
                },
                items: [
                    {
                        allowBlank:true,
                        minChars: 1,
                        forceSelection : true,
                        triggerOnClick: true,
                        typeAhead: true,
                        xtype:'boxselect',
                        cls: 'cms-groups-boxselect',
                        resizable: false,
                        name: 'members',
                        store: 'UserStore',
                        mode: 'local',
                        displayField: 'name',
                        listConfig: {
                            getInnerTpl: function()
                            {
                                return Templates.common.groupList;
                            }

                        },
                        valueField: 'key',
                        growMin: 75,
                        hideTrigger: true,
                        pinList: false,
                        labelTpl: '{name} ({userStore})'
                    }
                ]
            }
        ];

        this.callParent( arguments );

    }
} );
