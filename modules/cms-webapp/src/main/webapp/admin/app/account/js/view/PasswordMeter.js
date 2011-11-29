Ext.define( 'App.view.PasswordMeter', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.passwordMeter',

    requires: ['Ext.ProgressBar'],
    height: 38,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },



    updateMeter: function ( field, e, opts)
    {
        var passwordStrength = this.calculatePasswordStrength( field.getValue() );
        var passwordMeter = this.down( 'progressbar' );
        passwordMeter.updateProgress( passwordStrength, '', true);
    },

    calculatePasswordStrength: function ( pwd )
    {
        var nScore=0, nLength=0, nAlphaUC=0, nAlphaLC=0, nNumber=0, nSymbol=0, nMidChar=0, nRequirements=0,
            nAlphasOnly=0, nNumbersOnly=0, nUnqChar=0, nRepChar=0, nRepInc=0, nConsecAlphaUC=0,
            nConsecAlphaLC=0, nConsecNumber=0, nConsecSymbol=0, nConsecCharType=0, nSeqAlpha=0,
            nSeqNumber=0, nSeqSymbol=0, nSeqChar=0, nReqChar=0;
        var nMultMidChar=2, nMultConsecAlphaUC=2, nMultConsecAlphaLC=2, nMultConsecNumber=2;
        var nMultSeqAlpha=3, nMultSeqNumber=3, nMultSeqSymbol=3;
        var nMultLength=4, nMultNumber=4;
        var nMultSymbol=6;
        var nTmpAlphaUC="", nTmpAlphaLC="", nTmpNumber="", nTmpSymbol="";
        var sAlphaUC="0", sAlphaLC="0", sNumber="0", sSymbol="0", sMidChar="0", sRequirements="0", sAlphasOnly="0",
            sNumbersOnly="0", sRepChar="0", sConsecAlphaUC="0", sConsecAlphaLC="0", sConsecNumber="0", sSeqAlpha="0",
            sSeqNumber="0", sSeqSymbol="0";
        var sAlphas = "abcdefghijklmnopqrstuvwxyz";
        var sNumerics = "01234567890";
        var sSymbols = ")!@#$%^&*()";
        var nMinPwdLen = 8;
        if (pwd) {
            nScore = parseInt(pwd.length * nMultLength);
            nLength = pwd.length;
            var arrPwd = pwd.replace(/\s+/g,"").split(/\s*/);
            var arrPwdLen = arrPwd.length;

            /* Loop through password to check for Symbol, Numeric, Lowercase and Uppercase pattern matches */
            for (var a=0; a < arrPwdLen; a++) {
                if (arrPwd[a].match(/[A-Z]/g)) {
                    if (nTmpAlphaUC !== "") { if ((nTmpAlphaUC + 1) == a) { nConsecAlphaUC++; nConsecCharType++; } }
                    nTmpAlphaUC = a;
                    nAlphaUC++;
                }
                else if (arrPwd[a].match(/[a-z]/g)) {
                    if (nTmpAlphaLC !== "") { if ((nTmpAlphaLC + 1) == a) { nConsecAlphaLC++; nConsecCharType++; } }
                    nTmpAlphaLC = a;
                    nAlphaLC++;
                }
                else if (arrPwd[a].match(/[0-9]/g)) {
                    if (a > 0 && a < (arrPwdLen - 1)) { nMidChar++; }
                    if (nTmpNumber !== "") { if ((nTmpNumber + 1) == a) { nConsecNumber++; nConsecCharType++; } }
                    nTmpNumber = a;
                    nNumber++;
                }
                else if (arrPwd[a].match(/[^a-zA-Z0-9_]/g)) {
                    if (a > 0 && a < (arrPwdLen - 1)) { nMidChar++; }
                    if (nTmpSymbol !== "") { if ((nTmpSymbol + 1) == a) { nConsecSymbol++; nConsecCharType++; } }
                    nTmpSymbol = a;
                    nSymbol++;
                }
                /* Internal loop through password to check for repeat characters */
                var bCharExists = false;
                for (var b=0; b < arrPwdLen; b++) {
                    if (arrPwd[a] == arrPwd[b] && a != b) { /* repeat character exists */
                        bCharExists = true;
                        /*
                        Calculate icrement deduction based on proximity to identical characters
                        Deduction is incremented each time a new match is discovered
                        Deduction amount is based on total password length divided by the
                        difference of distance between currently selected match
                        */
                        nRepInc += Math.abs(arrPwdLen/(b-a));
                    }
                }
                if (bCharExists) {
                    nRepChar++;
                    nUnqChar = arrPwdLen-nRepChar;
                    nRepInc = (nUnqChar) ? Math.ceil(nRepInc/nUnqChar) : Math.ceil(nRepInc);
                }
            }
            var sFwd, sRev;
            /* Check for sequential alpha string patterns (forward and reverse) */
            for (var s=0; s < 23; s++) {
                sFwd = sAlphas.substring(s,parseInt(s+3));
                sRev = sFwd.split("").reverse().join(""); //string reverse
                if (pwd.toLowerCase().indexOf(sFwd) != -1 || pwd.toLowerCase().indexOf(sRev) != -1)
                {
                    nSeqAlpha++;
                    nSeqChar++;
                }
            }

            /* Check for sequential numeric string patterns (forward and reverse) */
            for (var s=0; s < 8; s++) {
                sFwd = sNumerics.substring(s,parseInt(s+3));
                sRev = sFwd.split("").reverse().join("");
                if (pwd.toLowerCase().indexOf(sFwd) != -1 || pwd.toLowerCase().indexOf(sRev) != -1)
                {
                    nSeqNumber++;
                    nSeqChar++;
                }
            }

            /* Check for sequential symbol string patterns (forward and reverse) */
            for (var s=0; s < 8; s++) {
                sFwd = sSymbols.substring(s,parseInt(s+3));
                sRev = sFwd.split("").reverse().join("");
                if (pwd.toLowerCase().indexOf(sFwd) != -1 || pwd.toLowerCase().indexOf(sRev) != -1)
                {
                    nSeqSymbol++;
                    nSeqChar++;
                }
            }

            /* Modify overall score value based on usage vs requirements */

            /* General point assignment */
            if (nAlphaUC > 0 && nAlphaUC < nLength) {
                nScore = parseInt(nScore + ((nLength - nAlphaUC) * 2));
                sAlphaUC = "+ " + parseInt((nLength - nAlphaUC) * 2);
            }
            if (nAlphaLC > 0 && nAlphaLC < nLength) {
                nScore = parseInt(nScore + ((nLength - nAlphaLC) * 2));
                sAlphaLC = "+ " + parseInt((nLength - nAlphaLC) * 2);
            }
            if (nNumber > 0 && nNumber < nLength) {
                nScore = parseInt(nScore + (nNumber * nMultNumber));
                sNumber = "+ " + parseInt(nNumber * nMultNumber);
            }
            if (nSymbol > 0) {
                nScore = parseInt(nScore + (nSymbol * nMultSymbol));
                sSymbol = "+ " + parseInt(nSymbol * nMultSymbol);
            }
            if (nMidChar > 0) {
                nScore = parseInt(nScore + (nMidChar * nMultMidChar));
                sMidChar = "+ " + parseInt(nMidChar * nMultMidChar);
            }

            /* Point deductions for poor practices */
            if ((nAlphaLC > 0 || nAlphaUC > 0) && nSymbol === 0 && nNumber === 0) {  // Only Letters
                nScore = parseInt(nScore - nLength);
                nAlphasOnly = nLength;
                sAlphasOnly = "- " + nLength;
            }
            if (nAlphaLC === 0 && nAlphaUC === 0 && nSymbol === 0 && nNumber > 0) {  // Only Numbers
                nScore = parseInt(nScore - nLength);
                nNumbersOnly = nLength;
                sNumbersOnly = "- " + nLength;
            }
            if (nRepChar > 0) {  // Same character exists more than once
                nScore = parseInt(nScore - nRepInc);
                sRepChar = "- " + nRepInc;
            }
            if (nConsecAlphaUC > 0) {  // Consecutive Uppercase Letters exist
                nScore = parseInt(nScore - (nConsecAlphaUC * nMultConsecAlphaUC));
                sConsecAlphaUC = "- " + parseInt(nConsecAlphaUC * nMultConsecAlphaUC);
            }
            if (nConsecAlphaLC > 0) {  // Consecutive Lowercase Letters exist
                nScore = parseInt(nScore - (nConsecAlphaLC * nMultConsecAlphaLC));
                sConsecAlphaLC = "- " + parseInt(nConsecAlphaLC * nMultConsecAlphaLC);
            }
            if (nConsecNumber > 0) {  // Consecutive Numbers exist
                nScore = parseInt(nScore - (nConsecNumber * nMultConsecNumber));
                sConsecNumber = "- " + parseInt(nConsecNumber * nMultConsecNumber);
            }
            if (nSeqAlpha > 0) {  // Sequential alpha strings exist (3 characters or more)
                nScore = parseInt(nScore - (nSeqAlpha * nMultSeqAlpha));
                sSeqAlpha = "- " + parseInt(nSeqAlpha * nMultSeqAlpha);
            }
            if (nSeqNumber > 0) {  // Sequential numeric strings exist (3 characters or more)
                nScore = parseInt(nScore - (nSeqNumber * nMultSeqNumber));
                sSeqNumber = "- " + parseInt(nSeqNumber * nMultSeqNumber);
            }
            if (nSeqSymbol > 0) {  // Sequential symbol strings exist (3 characters or more)
                nScore = parseInt(nScore - (nSeqSymbol * nMultSeqSymbol));
                sSeqSymbol = "- " + parseInt(nSeqSymbol * nMultSeqSymbol);
            }

            /* Determine if mandatory requirements have been met and set image indicators accordingly */
            var arrChars = [nLength,nAlphaUC,nAlphaLC,nNumber,nSymbol];
            var arrCharsIds = ["nLength","nAlphaUC","nAlphaLC","nNumber","nSymbol"];
            var arrCharsLen = arrChars.length;
            for (var c=0; c < arrCharsLen; c++) {
                if (arrCharsIds[c] == "nLength")
                {
                    var minVal = parseInt(nMinPwdLen - 1);
                }
                else
                {
                    var minVal = 0;
                }
                if (arrChars[c] == parseInt(minVal + 1))
                {
                    nReqChar++;
                }
                else
                if (arrChars[c] > parseInt(minVal + 1))
                {
                    nReqChar++;
                }
            }
            nRequirements = nReqChar;
            var nMinReqChars;
            if (pwd.length >= nMinPwdLen)
            {
                nMinReqChars = 3;
            }
            else
            {
                var nMinReqChars = 4;
            }
            if (nRequirements > nMinReqChars)
            {  // One or more required characters exist
                nScore = parseInt(nScore + (nRequirements * 2));
                sRequirements = "+ " + parseInt(nRequirements * 2);
            }

            return nScore / 100;
        }

        return nScore;
    },


    initComponent: function ( )
    {
        var me = this;
        this.items = [{
            xtype: 'textfield',
            inputType: 'password',
            enableKeyEvents: true,
            listeners: {
                keyup: {
                    fn: me.updateMeter,
                    scope: me
                }
            },
            flex: 1
        },
        {
            xtype: 'progressbar',
            cls: 'cms-password-meter',
            flex: 0.5
        }
        ];
        this.callParent( arguments );
    },

    getValue: function()
    {
        return this.down('textfield').getValue();
    },

    setValue: function( value )
    {
        this.down('textfield').setValue( value );
    }
});