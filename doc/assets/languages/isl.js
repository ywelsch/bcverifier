/*
Language: ISL
*/

hljs.LANGUAGES.isl = function(){
  var ISL_KEYWORDS = {
    'keyword': {
    	'invariant' : 1, 'boolean' : 1, 'int' : 1,
       'new' : 1, 'old' : 1, 'if' : 1, 'then' : 1, 'else' : 1, 'forall' : 1, 'exists' : 1, 'of' : 1,
       'local' : 1, 'in' : 1, 'place' : 1, 'when' : 1, 'predefined' : 1, 'instanceof' : 1, 'Bijection' : 1, 'in' : 1, 'line' : 1, 'call' : 1,
	},
    'constant': {
       'true': 1, 'false': 1, 'null': 1
    }
  };
  return {
    defaultMode: {
      keywords: ISL_KEYWORDS,
      illegal: '</',
      contains: [
        hljs.C_LINE_COMMENT_MODE,
        hljs.C_BLOCK_COMMENT_MODE,
        hljs.QUOTE_STRING_MODE,
        {
          className: 'string',
          begin: '\'', end: '[^\\\\]\'',
          relevance: 0
        },
        hljs.C_NUMBER_MODE
      ]
    }
  };
}();

