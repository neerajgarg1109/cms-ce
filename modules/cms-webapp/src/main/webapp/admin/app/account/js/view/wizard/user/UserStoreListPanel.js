Ext.define('App.view.wizard.user.UserStoreListPanel', {
    extend: 'Ext.view.View',
    alias : 'widget.userStoreListPanel',
    border: false,
    store: 'UserstoreConfigStore',

    autoScroll: true,

    initComponent: function() {
        var tpl = Templates.account.userstoreRadioButton;
        this.tpl = tpl;
        this.itemSelector = 'div.cms-userstore';
        this.callParent(arguments);
    },

    getData: function(){
        if (this.selectedUserStore){
            return {
                userStore: this.selectedUserStore.get('name')
            };
        }else{
            return undefined;
        }
    },

    setData: function(record){
        this.selectedUserStore = record;
    }

});