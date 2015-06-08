package com.gapid.palindromic;

import java.math.BigInteger;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;

// http://www.reddit.com/r/dailyprogrammer/comments/38yy9s/20150608_challenge_218_easy_making_numbers/crz1n62
class Palindromic{
	public static void main(String[] args) {
		IntStream.range(0, 1000).parallel().mapToObj(i -> i).collect(
				groupingBy(Palindromic::getPalindromicSum, toList())
		).forEach((i, j) -> System.out.println(
			(i.intValue() == -1 ? "Lychrel numbers:" : ("Sum " + i))
			+ ": " + j.stream().map(n -> "" + n).collect(joining(", "))));
	}

	public static BigInteger getPalindromicSum(int i){
		BigInteger n = new BigInteger("" + i), r; int c = 0;
		while (!n.equals(r = new BigInteger("" + new StringBuilder(n.toString()).reverse()))) {
			n = n.add(r);
			if (++c  > 10000) return new BigInteger("" + (-1));
		}
		return n;
	}
}
