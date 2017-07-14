import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class ParfAHighlighter extends AbstractTokenMaker
{
	public Token getTokenList(Segment text, int startTokenType, int startOffset) 
	{
	   resetTokenList();
	   char[] array = text.array;
	   int offset = text.offset;
	   int count = text.count;
	   int end = offset + count;
	   int newStartOffset = startOffset - offset;
	   int currentTokenStart = offset;
	   int currentTokenType  = startTokenType;

	   for (int i=offset; i<end; i++) {

	      char c = array[i];

	      switch (currentTokenType) {

	         case Token.NULL:

	            currentTokenStart = i;   // Starting a new token here.

	            switch (c) {

	               case ' ':
	               case '\t':
	                  currentTokenType = Token.WHITESPACE;
	                  break;

	               case '"':
	                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
	                  break;

	               case '#':
	                  currentTokenType = Token.COMMENT_EOL;
	                  break;

	               default:
	                  if (RSyntaxUtilities.isDigit(c)) {
	                     currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
	                     break;
	                  }
	                  else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
	                     currentTokenType = Token.IDENTIFIER;
	                     break;
	                  }
	                  
	                  // Anything not currently handled - mark as an identifier
	                  currentTokenType = Token.IDENTIFIER;
	                  break;

	            } // End of switch (c).

	            break;

	         case Token.WHITESPACE:

	            switch (c) {

	               case ' ':
	               case '\t':
	                  break;   // Still whitespace.

	               case '"':
	                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
	                  break;

	               case '#':
	                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.COMMENT_EOL;
	                  break;

	               default:   // Add the whitespace token and start anew.

	                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;

	                  if (RSyntaxUtilities.isDigit(c)) {
	                     currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
	                     break;
	                  }
	                  else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
	                     currentTokenType = Token.IDENTIFIER;
	                     break;
	                  }

	                  // Anything not currently handled - mark as identifier
	                  currentTokenType = Token.IDENTIFIER;

	            } // End of switch (c).

	            break;

	         default: // Should never happen
	         case Token.IDENTIFIER:

	            switch (c) {

	               case ' ':
	               case '\t':
	                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.WHITESPACE;
	                  break;

	               case '"':
	                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
	                  break;

	               default:
	                  if (RSyntaxUtilities.isLetterOrDigit(c) || c=='/' || c=='_') {
	                     break;   // Still an identifier of some type.
	                  }
	                  // Otherwise, we're still an identifier (?).

	            } // End of switch (c).

	            break;

	         case Token.LITERAL_NUMBER_DECIMAL_INT:

	            switch (c) {

	               case ' ':
	               case '\t':
	                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.WHITESPACE;
	                  break;

	               case '"':
	                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
	                  currentTokenStart = i;
	                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
	                  break;

	               default:

	                  if (RSyntaxUtilities.isDigit(c)) {
	                     break;   // Still a literal number.
	                  }

	                  // Otherwise, remember this was a number and start over.
	                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
	                  i--;
	                  currentTokenType = Token.NULL;

	            } // End of switch (c).

	            break;

	         case Token.COMMENT_EOL:
	            i = end - 1;
	            addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
	            // We need to set token type to null so at the bottom we don't add one more token.
	            currentTokenType = Token.NULL;
	            break;

	         case Token.LITERAL_STRING_DOUBLE_QUOTE:
	            if (c=='"') {
	               addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
	               currentTokenType = Token.NULL;
	            }
	            break;

	      } // End of switch (currentTokenType).

	   } // End of for (int i=offset; i<end; i++).

	   switch (currentTokenType) {

	      // Remember what token type to begin the next line with.
	      case Token.LITERAL_STRING_DOUBLE_QUOTE:
	         addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
	         break;

	      // Do nothing if everything was okay.
	      case Token.NULL:
	         addNullToken();
	         break;

	      // All other token types don't continue to the next line...
	      default:
	         addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
	         addNullToken();

	   }

	   // Return the first token in our linked list.
	   return firstToken;

	}

	@Override
	public TokenMap getWordsToHighlight() 
	{
		TokenMap tokenMap = new TokenMap();
		
		//Compound Statements
		tokenMap.put("if",  Token.RESERVED_WORD);
		tokenMap.put("else",  Token.RESERVED_WORD);
		tokenMap.put("loop",  Token.RESERVED_WORD);
		tokenMap.put("repeatUntil",  Token.RESERVED_WORD);
		tokenMap.put("forever",  Token.RESERVED_WORD);
		//Data Types
		tokenMap.put("number",  Token.DATA_TYPE);
		tokenMap.put("logic",   Token.DATA_TYPE);
		tokenMap.put("text",    Token.DATA_TYPE);
		tokenMap.put("list", Token.DATA_TYPE);
		//Literals
		tokenMap.put("true",    Token.DATA_TYPE);
		tokenMap.put("false", Token.DATA_TYPE);
		//Separators
		tokenMap.put("(",  Token.SEPARATOR);
		tokenMap.put(")",   Token.SEPARATOR);
		tokenMap.put("{",    Token.SEPARATOR);
		tokenMap.put("}", Token.SEPARATOR);
		tokenMap.put(",", Token.SEPARATOR);
		//Functions
		tokenMap.put("announce", Token.FUNCTION);
		tokenMap.put("ask",  Token.FUNCTION);
		tokenMap.put("wait",  Token.FUNCTION);
		tokenMap.put("waitUntil",  Token.FUNCTION);
		tokenMap.put("add", Token.FUNCTION);
		tokenMap.put("remove",  Token.FUNCTION);
		tokenMap.put("create",  Token.FUNCTION);
		tokenMap.put("store",  Token.FUNCTION);
		//Operators
		tokenMap.put("+",  Token.OPERATOR);
		tokenMap.put("-",  Token.OPERATOR);
		tokenMap.put("*",  Token.OPERATOR);
		tokenMap.put("/",  Token.OPERATOR);
		tokenMap.put("%",  Token.OPERATOR);
		tokenMap.put("contains",  Token.OPERATOR);
		tokenMap.put("numberof",  Token.OPERATOR);
		tokenMap.put("length",  Token.OPERATOR);
		tokenMap.put("lengthof",  Token.OPERATOR);
		tokenMap.put("equals",  Token.OPERATOR);
		tokenMap.put("==",  Token.OPERATOR);
		tokenMap.put("doesnotequal",  Token.OPERATOR);
		tokenMap.put("!=",  Token.OPERATOR);
		tokenMap.put("&&",  Token.OPERATOR);
		tokenMap.put("&",  Token.OPERATOR);
		tokenMap.put("and",  Token.OPERATOR);
		tokenMap.put("||",  Token.OPERATOR);
		tokenMap.put("|",  Token.OPERATOR);
		tokenMap.put("or",  Token.OPERATOR);
		tokenMap.put("not",  Token.OPERATOR);
		tokenMap.put("!",  Token.OPERATOR);
		tokenMap.put(">",  Token.OPERATOR);
		tokenMap.put("<",  Token.OPERATOR);
		tokenMap.put(">=",  Token.OPERATOR);
		tokenMap.put("<=",  Token.OPERATOR);
		tokenMap.put("||",  Token.OPERATOR);
		tokenMap.put("|",  Token.OPERATOR);
		tokenMap.put("or",  Token.OPERATOR);
		tokenMap.put("not",  Token.OPERATOR);
		//Variables
		tokenMap.put("answer", Token.VARIABLE);
		//Comments
		tokenMap.put("//", Token.COMMENT_EOL);
		tokenMap.put("/*", Token.COMMENT_MULTILINE);
		tokenMap.put("*/", Token.COMMENT_MULTILINE);

		
		return tokenMap;
	}
	
	@Override
	public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
	   // This assumes all keywords, etc. were parsed as "identifiers."
	   if (tokenType==Token.IDENTIFIER) {
	      int value = wordsToHighlight.get(segment, start, end);
	      if (value != -1) {
	         tokenType = value;
	      }
	   }
	   super.addToken(segment, start, end, tokenType, startOffset);
	}
}
