package de.dhbw.mh.redeggs;

import java.util.ArrayList;
import java.util.List;

import static de.dhbw.mh.redeggs.CodePointRange.range;
import static de.dhbw.mh.redeggs.CodePointRange.single;

/**
 * A parser for regular expressions using recursive descent parsing.
 * This class is responsible for converting a regular expression string into a
 * tree representation of a {@link RegularEggspression}.
 */
public class RecursiveDescentRedeggsParser {

	/**
	 * The symbol factory used to create symbols for the regular expression.
	 */
	protected final SymbolFactory symbolFactory;

	/**
	 * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
	 * symbol factory.
	 *
	 * @param symbolFactory the factory used to create symbols for parsing
	 */
	public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {
		this.symbolFactory = symbolFactory;
	}

	private int cursor;
	private String input;

	private void advance() {
		++cursor;
	}

	private boolean match(int codePoint) {
		if (cursor >= input.length()) {
			return false;
		}
		if (input.charAt(cursor) != codePoint) {
			return false;
		}
		advance();
		return true;
	}

	private int current() {
		if (cursor >= input.length()) {
			return -1;
		}
		return input.codePointAt(cursor);
	}

	private boolean isLiteral(int literal) {
		VirtualSymbol symbol = symbolFactory.newSymbol()
				.include(range('a', 'z'), range('A', 'Z'), single('_'))
				.andNothingElse();

		if (symbol.sortedCodePointRanges().stream().filter(
				r -> literal >= r.firstCodePoint && literal <= r.lastCodePoint).toArray().length != 0
		) {
			return true;
		};
		return false;
	}

	private RegularEggspression ruleRegex() throws RedeggsParseException {
		if (match(949)) {
			return new RegularEggspression.EmptyWord();
		}
		else if (match(8709)) {
			return new RegularEggspression.EmptySet();
		}
		if (isLiteral(current()) || current() == 40 || current() == 91) {
			RegularEggspression concat = ruleConcat();
			RegularEggspression union = ruleUnion();
			if (!(union instanceof RegularEggspression.EmptyWord)) {
				return new RegularEggspression.Alternation(concat, union);
			}
			return concat;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.charAt(cursor), cursor), cursor);
	}

	private RegularEggspression ruleUnion() throws RedeggsParseException {
		if (match(124)) {
			RegularEggspression concat = ruleConcat();
			RegularEggspression union = ruleUnion();
			if (!(union instanceof RegularEggspression.EmptyWord)) {
				return new RegularEggspression.Alternation(concat, union);
			}
			return concat;
		}
		else if (current() == -1 || current() == 41) {
			return new RegularEggspression.EmptyWord();
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private RegularEggspression ruleConcat() throws RedeggsParseException {
		if (isLiteral(current()) || current() == 40 || current() == 91) {
			RegularEggspression kleene = ruleKleene();
			RegularEggspression suffix = ruleSuffix();
			if (!(suffix instanceof RegularEggspression.EmptyWord)) {
				return new RegularEggspression.Concatenation(kleene, suffix);
			}
			return kleene;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private RegularEggspression ruleSuffix() throws RedeggsParseException {
		if (isLiteral(current()) || current() == 40 || current() == 91) {
			RegularEggspression kleene = ruleKleene();
			RegularEggspression suffix = ruleSuffix();
			if (!(suffix instanceof RegularEggspression.EmptyWord)) {
				return new RegularEggspression.Concatenation(kleene, suffix);
			}
			return kleene;
		}
		else if (current() == -1 || current() == 41 || current() == 124) {
			return new RegularEggspression.EmptyWord();
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private RegularEggspression ruleKleene() throws RedeggsParseException {
		if (isLiteral(current()) || current() == 40 || current() == 91) {
			RegularEggspression base = ruleBase();
			boolean star = ruleStar();
			if (star) {
				return new RegularEggspression.Star(base);
			}
			return base;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private boolean ruleStar() throws RedeggsParseException {
		if (match(42)) {
			return true;
		}
		else if (current() == -1 || current() == 41 || current() == 124 || current() == 40  || current() == 91 || isLiteral(current())) {
			return false;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private RegularEggspression ruleBase() throws RedeggsParseException {
		if (isLiteral(current())) {
			VirtualSymbol literal = symbolFactory.newSymbol()
					.include(single(current()))
					.andNothingElse();
			advance();
			return new RegularEggspression.Literal(literal);
		}
		else if (match(40)) {
			RegularEggspression regex = ruleRegex();
			if (match(41)) {
				return regex;
			}
			throw new RedeggsParseException(String.format("Input ended unexpectedly, expected symbol ')' at position %s.", ++cursor), ++cursor);
		}
		else if (match(91)) {
			boolean negate = ruleCaret();
			CodePointRange chars = ruleChars();
			List<CodePointRange> range = ruleRange();
			if (match(93)) {
				List<CodePointRange> newRange = new ArrayList<>();
				newRange.add(chars);
				if (range != null) {
					newRange.addAll(range);
				}
				VirtualSymbol ranges;
				if (negate) {
					ranges = symbolFactory.newSymbol()
							.exclude(newRange.toArray(new CodePointRange[0]))
							.andNothingElse();
				}
				else {
					ranges = symbolFactory.newSymbol()
							.include(newRange.toArray(new CodePointRange[0]))
							.andNothingElse();
				}

				return new RegularEggspression.Literal(ranges);
			}
			throw new RedeggsParseException(String.format("Input ended unexpectedly, expected symbol ']' at position %s.", ++cursor), cursor);
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), cursor);
	}

	private boolean ruleCaret() throws RedeggsParseException {
		if (match(94)) {
			return true;
		}
		else if (isLiteral(current())) {
			return false;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), 1);
	}

	private List<CodePointRange> ruleRange() throws RedeggsParseException {
		if (isLiteral(current())) {
			CodePointRange chars = ruleChars();
			List<CodePointRange> range = ruleRange();
			if (range != null) {
				List<CodePointRange> newRange = new ArrayList<>();
				newRange.add(chars);
				newRange.addAll(range);
				return newRange;
			}
			return List.of(chars);
		}
		else if (current() == 93) {
			return null;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), 1);
	}

	private CodePointRange ruleChars() throws RedeggsParseException {
		if (isLiteral(current())) {
			int lower_range = current();
			advance();
			if (ruleRest()) {
				int upper_range = current();
				advance();
				return new CodePointRange(lower_range, upper_range);
			}
			return new CodePointRange(lower_range, lower_range);
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), 1);
	}

	private boolean ruleRest() throws RedeggsParseException {
		if (match(45)) {
			return true;
		}
		else if (current() == -1 || isLiteral(current())) {
			return false;
		}
		throw new RedeggsParseException(String.format("Unexpected symbol %s at position %s.", input.substring(cursor), ++cursor), 1);
	}

	/**
	 * Parses a regular expression string into an abstract syntax tree (AST).
	 * 
	 * This class uses recursive descent parsing to convert a given regular
	 * expression into a tree structure that can be processed or compiled further.
	 * The AST nodes represent different components of the regex such as literals,
	 * operators, and groups.
	 *
	 * @param regex the regular expression to parse
	 * @return the {@link RegularEggspression} representation of the parsed regex
	 * @throws RedeggsParseException if the parsing fails or the regex is invalid
	 */
	public RegularEggspression parse(String regex) throws RedeggsParseException {
		// This is a placeholder implementation to demonstrate how to create a symbol.
		this.cursor = 0;
		this.input = regex;


		RegularEggspression reg = ruleRegex();
		if (current() != -1) {
			throw new RedeggsParseException(String.format("Unexpected symbol '%s' at position %s.", input.charAt(cursor), ++cursor), 1);
		}
		return reg;
	}
}
