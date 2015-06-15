package com.gapid.latex;

import java.util.*;
// https://www.reddit.com/r/dailyprogrammer/comments/38nhgx/20150605_challenge_217_practical_exercise_texscii/crx3ys9
public class Latex {
	public static void main(String[] args) {  new Latex().go(args[0]); }
	List<Token> tokens; private Token currentToken;
	Iterator<Token> tokenIterator;
	final static Map<String, Token> TOKEN_MAP = new HashMap<>();
	Object[] tokenPairs = {"\\frac", Token.FRAC, "\\sqrt", Token.SQRT, "\\root",
			Token.ROOT, "{", Token.LBRACE, "}", Token.RBRACE,  "^", Token.SUP, "_", Token.SUB};

	void go(String e) {
		e = e.replace("\\pi", "?");
		for (int i = 0; i < tokenPairs.length; i += 2) TOKEN_MAP.put((String) tokenPairs[i], (Token) tokenPairs[i + 1]);
		tokens = tokenize(e);
		Exp equation = parseEquation(tokens);
		Map<Integer, Map<Integer, Character>> canvas = new HashMap<>();
		equation.paint(canvas, 0, 0);
		int minY =  canvas.keySet().stream() .min(Integer::compare).get();
		int maxY =  canvas.keySet().stream() .max(Integer::compare).get();
		int maxX =  canvas.values().stream() .map(integerCharacterMap -> integerCharacterMap.keySet().stream()
				.max(Integer::compare).get() ).max(Integer::compare).get();
		for (int y = minY; y <= maxY; y++){
			for (int x = 0 ; x <= maxX; x++ ) {
				Character c = canvas.get(y).get(x);
				System.out.print(c == null ? ' ' : c);
			}
			System.out.println();
		}
	}

	List<Token> tokenize(String equationString) {
		List<Token> tokens = new LinkedList<>(); char[] chars = equationString.toCharArray(); int start = -1;
		for (int i = 0; i < chars.length; i++){
			char c = chars[i];
			if (TOKEN_MAP.get("" + c)!=null || c == '\\') {
				if (start > -1) {
					tokens.add(lexString(equationString.substring(start, i)));
				}
				if (c=='\\')  start = i;
				else{
					tokens.add(TOKEN_MAP.get("" + c));
					start = -1;
				}
			}
			else if (start == -1) start  = i;
		}
		if (start > -1) { tokens.add(lexString(equationString.substring(start))); }
		return tokens;
	}

	Token lexString(String substring) {
		Token token = TOKEN_MAP.get(substring); if (token != null) return token;
		token = new Token(Token.TType.IDENTIFIER, substring);
		return token;
	}

	Exp parseExp() {
		Token t = currentToken;
		if (accept(Token.TType.FRAC)) return new Exp(new Fraction(getSubExp(), getSubExp()), parseExp());
		else if (accept(Token.TType.SUP)) return new Exp(new Sup(getSubExp()), parseExp());
		else if (accept(Token.TType.SUB)) return new Exp(new Sub(getSubExp()), parseExp());
		else if (accept(Token.TType.SQRT)) return new Exp(new Sqrt(getSubExp()), parseExp());
		else if (accept(Token.TType.ROOT)) return new Exp( new Root(getSubExp(), getSubExp()), parseExp());
		else if (accept(Token.TType.IDENTIFIER)) return new Exp(new Identifier(t), parseExp());
		return null;
	}

	Exp parseEquation(List<Token> tokens) { tokenIterator = tokens.iterator(); nextToken(); return parseExp(); }
	void lbrace() { if (!accept(Token.TType.LBRACE)) throw new RuntimeException("No left brace.");}
	void rbrace() { if (!accept(Token.TType.RBRACE)) throw new RuntimeException("No right brace.");}
	Exp getSubExp(){ lbrace(); Exp e = parseExp(); rbrace(); return e; }
	Token nextToken() { return currentToken = tokenIterator.hasNext() ? tokenIterator.next() : null;  }
	boolean accept(Token.TType identifier) {
		if (currentToken == null) return false;
		if (currentToken.type == identifier){ nextToken(); return true; }
		return false;
	}
	static void putChar(Map<Integer, Map<Integer, Character>> canvas, int x, int y, char c) {
		Map<Integer, Character> row = canvas.get(y);
		if (row == null) { row = new HashMap<>(); canvas.put(y, row); } row.put(x, c);
	}
}

class Exp extends Factor{
	Factor factor; Exp next;
	int getWidth() { return factor.getWidth() + (next == null ? 0 : next.getWidth()); }
	int getBottom() { return Integer.max(factor.getBottom(), next == null ? 0 : next.getBottom()); }
	int getTop() { return Integer.max(factor.getTop(), next == null ? 0 : next.getTop()); }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		int width = factor.paint(canvas, startx, starty);
		if (next != null) width += next.paint(canvas, startx + width, starty);
		return width;
	}
	Exp(Factor factor, Exp exp) {
		this.factor = factor; this.next = exp;
		if (factor instanceof Sub && next != null && next.factor instanceof Sup ) {
			Sup sup = (Sup) next.factor;
			next = exp.next; exp.next = null; this.factor = new SubSup(new Exp(((Sub) factor).exp, null), sup.exp);
		}
	}
}

abstract class Factor {
	abstract int getWidth(); abstract int getBottom(); abstract int getTop();
	abstract int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty);
}
abstract class Container extends Factor{ Exp exp; Container(Exp exp){ this.exp = exp; } }
class Sqrt extends Root {  Sqrt(Exp exp) { super(null, exp);} }

class Fraction extends Factor {
	Exp top, bottom;
	Fraction(Exp top, Exp bottom ) { this.top = top; this.bottom = bottom;}
	int getWidth() {  return Integer.max(top.getWidth(), bottom.getWidth()); }
	int getBottom() { return 1 + bottom.getTop() + bottom.getBottom(); }
	int getTop() { return top.getTop() + top.getBottom() + 1; }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		return paintWithDivider(canvas, startx, starty, '-');
	}
	int paintWithDivider(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty, char divider) {
		int topWidth = top.paint(new HashMap<>(), startx, starty);
		int bottomWidth = bottom.paint(new HashMap<>(), startx, starty);
		if (topWidth > bottomWidth) {
			top.paint(canvas, startx, starty - top.getBottom() - 1);
			bottom.paint(canvas, startx + (topWidth - bottomWidth) / 2, starty + bottom.getTop() + 1);
		}
		else {
			top.paint(  canvas, startx + (bottomWidth - topWidth) / 2, starty - top.getBottom() - 1);
			bottom.paint(canvas, startx, starty + bottom.getTop() + 1);
		}
		for (int i = 0; i <= Integer.max(bottomWidth, topWidth); i++ )
			Latex.putChar(canvas, startx + i, starty, divider);
		return Integer.max(bottomWidth, topWidth);
	}
}

class Sub extends Container {
	Sub(Exp exp) { super(exp); }
	int getWidth() { return exp.getWidth(); }
	int getBottom() { return 1 + exp.getBottom(); }
	int getTop() { return 0; }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		return exp.paint(canvas, startx, starty + 1);
	}
}

class Sup extends Container {
	Sup(Exp exp) { super(exp); }
	int getBottom() { return 0; }
	int getTop() { return 1 + exp.getBottom(); }
	int getWidth() { return exp.getWidth(); }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		return exp.paint(canvas, startx, starty - 1);
	}
}

class SubSup extends Fraction {
	SubSup(Exp bottom, Exp top) { super(top, bottom); }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		return super.paintWithDivider(canvas, startx, starty, ' ') + 1;
	}
}

class Identifier extends Factor {
	final String value;
	Identifier(Token currentToken) { this.value = currentToken.value; }
	int getWidth() {return value.length();} public int getBottom() {return 0;} public int getTop() {return 0;}
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		char[] chars = value.toCharArray();
		for (int i = 0 ; i < value.length(); i++) Latex.putChar(canvas, startx + i, starty, chars[i]);
		return value.length();
	}
}

class Root extends Container{
	final Exp identifier;
	Root(Exp i, Exp exp) {
		super(exp);
		this.identifier = i == null ? new Exp(new Identifier(new Token(Token.TType.IDENTIFIER, "2")), null) : i;
	}
	int getWidth() { return 1 + exp.getWidth() + exp.getTop() + exp.getBottom(); }
	int getTop() { return 2 + exp.getBottom() + exp.getTop()  - 1; }
	int getBottom() {return 0; }
	int paint(Map<Integer, Map<Integer, Character>> canvas, int startx, int starty) {
		Latex.putChar(canvas, startx, starty, '?');
		int height = exp.getTop() + exp.getBottom();
		for (int i = 1; i <= height; i++) {
			Latex.putChar(canvas, startx + i, starty - i, '/');
		}
		if (!"2".equals(((Identifier) (identifier.factor)).value)) identifier.paint(canvas, startx + height, starty - 1 - height);
		for (int i = 1 ; i <= exp.getWidth(); i++ ) Latex.putChar(canvas, startx + height + i, starty - height - 1 , '_');
		return height + 2 + exp.paint(canvas, startx + height + 1, starty - exp.getBottom());
	}
}

class Token{
	enum TType{ FRAC, SQRT, ROOT, LBRACE, RBRACE, IDENTIFIER, SUB, SUP; }
	final static Token RBRACE = new Token(TType.RBRACE),  LBRACE = new Token(TType.LBRACE),
			SQRT = new Token(TType.SQRT), ROOT = new Token(TType.ROOT), FRAC = new Token(TType.FRAC),
			SUB = new Token(TType.SUB), SUP = new Token(TType.SUP);
	TType type; String value;
	Token(TType type) { this.type = type; }
	Token(TType type, String identifier) { this.type = type; this.value = identifier; }
	public String toString() { return type.toString() + " - " + value;}
}