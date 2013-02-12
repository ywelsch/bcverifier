function updateEditorSize(editor,editorwrap,textarea) {
	textarea.value = editor.getSession().getValue();
	var newHeight = editor.getSession().getDocument().getLength() * editor.renderer.lineHeight + editor.renderer.scrollBar.getWidth();
    editorwrap.style.height = newHeight + "px";
    textarea.style.height = newHeight + "px";
    editor.resize();
}
function registerAceEditor(editor) {
	if (!window.aceEditors) { window.aceEditors = {}; }
	window.aceEditors[editor.container.id] = editor;
}
function connectLib(libname,talibname,container) {
	var editor = ace.edit(libname);
	var editorwrap = document.getElementById(libname);
	var containerid = document.getElementById(container);
	//var initialheight = editorwrap.style.height;
	editor.setTheme("ace/theme/cobalt");
	 var JavaMode = require("ace/mode/java").Mode;
	    editor.getSession().setMode(new JavaMode());
	    //editor.renderer.setShowGutter(false);
	    editor.setShowPrintMargin(false);
	    editor.setHighlightActiveLine(false);
	    editor.setDisplayIndentGuides(false);
	    var textarea = document.getElementById(talibname);
	    editor.getSession().setValue(textarea.value);
	    updateEditorSize(editor,editorwrap,textarea);
	    setTimeout(function() { updateEditorSize(editor,editorwrap,textarea); }, 100); // fix for internet explorer
	    editor.getSession().on('change', function(){
	      updateEditorSize(editor,editorwrap,textarea);
	    });
	    registerAceEditor(editor);
}
function connectInv(libname,talibname,container) {
	var editor = ace.edit(libname);
	var editorwrap = document.getElementById(libname);
	var containerid = document.getElementById(container);
	editor.setTheme("ace/theme/cobalt");
	 var ISLMode = require("ace/mode/isl").Mode; // https://github.com/ajaxorg/ace/wiki/Creating-or-Extending-an-Edit-Mode
	    editor.getSession().setMode(new ISLMode());
	    //editor.renderer.setShowGutter(false);
	    editor.setShowPrintMargin(false);
	    editor.setHighlightActiveLine(false);
	    editor.setDisplayIndentGuides(false);
	    var textarea = document.getElementById(talibname);
	    editor.getSession().setValue(textarea.value);
	    updateEditorSize(editor,editorwrap,textarea);
	    setTimeout(function() { updateEditorSize(editor,editorwrap,textarea); }, 100); // fix for internet explorer
	    editor.getSession().on('change', function(){
	      updateEditorSize(editor,editorwrap,textarea);
	    });
	    editor.getSession().setValue(textarea.value);
    registerAceEditor(editor);
}
function connectBoogieInput(libname,talibname,container) {
	var editor = ace.edit(libname);
	var editorwrap = document.getElementById(libname);
	editor.setTheme("ace/theme/cobalt");
	var BoogieMode = require("ace/mode/boogie").Mode; // https://github.com/ajaxorg/ace/wiki/Creating-or-Extending-an-Edit-Mode
    editor.getSession().setMode(new BoogieMode());
	    //editor.renderer.setShowGutter(false);
	    //editor.getSession().setUseWrapMode(true);
	    editor.setShowPrintMargin(false);
	    editor.setHighlightActiveLine(false);
	    editor.setDisplayIndentGuides(false);
	    editor.setReadOnly(true);
	    var textarea = document.getElementById(talibname);
	    editor.getSession().setValue(textarea.value);
	    //updateEditorSize(editor,editorwrap);
	    editor.getSession().on('change', function(){
	      textarea.value = editor.getSession().getValue();
	    });
	 window.boogieinput = editor;
	 registerAceEditor(editor);
}
function acegoto(editorId,line,column) {
	var editor = window.aceEditors[editorId];
	if (!editor) alert("editor " + editorId + " does not exist");
	editor.gotoLine(line,column-1);
	var Range = require('ace/range').Range;
	var range = new Range(line-1, column-1, 0, 0);
	var session = editor.getSession();
	var markers = session.getMarkers();
	for (var marker in markers) {
		session.removeMarker(marker);
	}
	editor.getSession().addMarker(range,"ace_selection","line");
}