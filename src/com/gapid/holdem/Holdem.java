package com.gapid.holdem;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;
import static java.util.function.Function.identity;

// http://www.reddit.com/r/dailyprogrammer/comments/37idka/20150527_challenge_216_intermediate_texas_hold_em/crn4x6z
public class Holdem {
	final static List<Hand> players = new ArrayList<>();
	static List<Card> board = new ArrayList<>();
	public final static int POWERS[] = {1, 15, 225, 3375, 50625, 759375};

	static HandScore scoreHand(Hand hand) {
		return Stream.iterate(new int[]{0, 1}, t -> (t[0] == t[1] - 1) ? new int[]{0, t[1] + 1} : new int[]{t[0] + 1, t[1]})
				.limit(21).parallel() // 7! / (5! * (7 - 5)!)  - 7 choose 5
				.map(t -> IntStream.rangeClosed(0, 6).filter(i -> i != t[0] && i != t[1]).toArray())
				.map(hand::computeScore)
				.sorted(HandScore::compare).findFirst().get();
	}

	static void maybeFold(HandScore minHand) {
		List<HandScore> scores = players.stream().skip(1).map(Holdem::scoreHand).collect(toList());
		scores.stream().filter(hand ->
				HandScore.compare(hand, minHand) > 0 && !players.get(hand.playerNumber).folded && Math.random() > .14)
				.forEach(hand -> {
							System.out.println("CPU " + hand.playerNumber + " Folds!");
							players.get(hand.playerNumber).folded = true;
						}
				);
	}

	static List<Function<Card[], HandTypeScore>> scoreTypes = Arrays.asList(
			Holdem::sStraightFlush, Holdem::sFour, Holdem::sFullHouse,
			Holdem::sFlush, Holdem::sStraight, Holdem::sThree,
			Holdem::sTwoPair, Holdem::sPair, Holdem::sHigh
	);

	static class Hand{
		private final int playerNumber;
		Card[] cards;
		public boolean folded;

		public Hand(int playerNumber, Card[] cards) {
			this.cards = cards;
			this.playerNumber = playerNumber;
		}

		public HandScore computeScore(int[] ints) {
			HandScore score = new HandScore();
			List<Card> possibleCards = new ArrayList<>();
			possibleCards.addAll(Arrays.asList(cards));
			possibleCards.addAll(board);
			int index = -1;
			while(possibleCards.size() < 7) possibleCards.add(new Card(index,index--));
			Card[] cards = Arrays.stream(ints).mapToObj(possibleCards::get).collect(toList()).toArray(new Card[ints.length]);
			score.cards = cards;
			score.playerNumber = playerNumber;
			score.scores = scoreTypes.parallelStream().map(s->s.apply(cards)).collect(toList());

			return score;
		}
	}

	static class HandScore {
		public List<HandTypeScore> scores;
		public Card[] cards;
		public int playerNumber;

		public static int compare(HandScore a, HandScore b) {
			for (int i = 0; i < scoreTypes.size(); i++){
				int scoreA = a.scores.get(i).score, scoreB = b.scores.get(i).score;
				if (scoreA > scoreB) return -1;
				if (scoreA < scoreB) return 1;
			}
			return 0;
		}

		public String toString() {
			return (playerNumber == 0 ? "You" : ("CPU " + playerNumber)) + ". " + scores.stream().filter(i-> i.score > 0).findFirst().get().message;
		}
	}

	static class HandTypeScore {
		int score;
		int[] cards;
		String message;

		public String toString() {
			return Arrays.stream(cards).mapToObj(String::valueOf).collect(joining(" ")) + " - " + message;
		}

		public HandTypeScore(){}
		public HandTypeScore(int score, String message) { this.score = score; this.message = message;}
		public HandTypeScore(HandTypeScore score, String message) {this.score = score.score; this.cards = score.cards; this.message = message;}
	}

	private static HandTypeScore sFlush(Card[] aHand){
		return Arrays.stream(aHand).filter(t->t.suit == aHand[0].suit).count() == 5 ? new HandTypeScore(sHigh(aHand), "Flush") : new HandTypeScore();
	}

	private static Map<Integer, Long> countFaces(Card[] aHand) {
		return Arrays.stream(aHand).map(c->c.face).collect(groupingBy(identity(), counting()));
	}

	private static HandTypeScore sFullHouse(Card[] aHand) {
		Map<Integer, Long> map = countFaces(aHand);
		Set<Integer> keySet = map.keySet();

		if (keySet.size() != 2 || !map.containsValue(3L)) return new HandTypeScore();
		return new HandTypeScore(POWERS[1] * keySet.stream().filter(i->map.get(i) == 3L).findAny().get()
				+ POWERS[0] * keySet.stream().filter(i->map.get(i) == 2L).findAny().get(), "Full House");
	}

	private static HandTypeScore sTwoPair(Card[] aHand) {
		Map<Integer, Long> map = countFaces(aHand);
		Set<Integer> keySet = map.keySet();

		if (keySet.size() != 3 || !map.containsValue((long) 2)) return new HandTypeScore();
		return new HandTypeScore(POWERS[2] * keySet.stream().filter(i->map.get(i) == 2L).max(Integer::compareTo).get()
				+ POWERS[1] *keySet.stream().filter(i->map.get(i) == 2L).min(Integer::compareTo).get()
				+ POWERS[0] * keySet.stream().filter(i->map.get(i) == 1L).findFirst().get(), "Two Pair");
	}

	private static HandTypeScore sHigh(Card[] aHand){
		List<Integer> sorted = Arrays.stream(aHand).map(c->c.face).sorted().collect(toList());
		return new HandTypeScore(IntStream.rangeClosed(0, 4).map(i->POWERS[i] * sorted.get(i)).sum(), Card.FACE_NAMES[sorted.get(4)] + " high");
	}

	private static HandTypeScore scoreGroup(Card[] aHand, long aGroup) {
		Map<Integer, Long> map = countFaces(aHand);
		Optional<Integer> face = map.keySet().stream().filter(i->map.get(i) >= aGroup).findAny();
		if (!face.isPresent()) return new HandTypeScore();

		return new HandTypeScore(POWERS[5] * face.get() + sHigh(aHand).score, aGroup + " of a kind");
	}

	private static HandTypeScore sPair(Card[] aHand){ return new HandTypeScore(scoreGroup(aHand, 2), "Pair"); }
	private static HandTypeScore sThree(Card[] aHand){ return scoreGroup(aHand, 3); }
	private static HandTypeScore sFour(Card[] aHand) { return scoreGroup(aHand, 4); }

	private static HandTypeScore straightHelper(List<Integer> aHand) {
		int sum = aHand.stream().mapToInt(Integer::intValue).sum();
		int min = aHand.stream().min(Integer::compareTo).get();
		int max = aHand.stream().max(Integer::compareTo).get();

		return new HandTypeScore((sum == (max + 1) * (max) / 2 - (min * (min - 1) / 2)) ? max :0 , "Straight");
	}

	private static HandTypeScore sStraight(Card[] aHand){
		if (countFaces(aHand).keySet().size() != 5) return new HandTypeScore();
		HandTypeScore score = straightHelper(Arrays.stream(aHand).map(c -> c.face).collect(toList()));
		if (score.score > 0) return score;
		return straightHelper(Arrays.stream(aHand).map(c -> c.face == 14 ? 1 : c.face).collect(toList()));
	}

	private static HandTypeScore sStraightFlush(Card[] aHand) {
		HandTypeScore score = sStraight(aHand);
		if (score.score == 0) return score;
		if (sFlush(aHand).score == 0) return new HandTypeScore();
		return new HandTypeScore(score, "straight flush");
	}
	public static void main(String args[]){
		Scanner in = new Scanner(System.in);

		System.out.print("How many players? (2-8) ");
		int numPlayers = Integer.parseInt(in.nextLine());
		Deck deck = new Deck();
		deck.shuffle();

		for (int i = 0; i < numPlayers; i++) {
			Hand hand = new Hand(i, new Card[]{deck.draw(), deck.draw()});
			players.add(hand);
			System.out.print((i == 0 ? "Your hand" :("CPU " + i)) + ": ");
			System.out.println(hand.cards[0] + ", " + hand.cards[1]);
		}

		deck.draw(); // burn

		board.add(deck.draw()); board.add(deck.draw()); board.add(deck.draw());
		System.out.println();
		System.out.println("Flop: " + board);


		// until we have pot info, all bets are .... off?  when bets are in, we can make this
		// more interesting.  for now, let's just fold if we don't have a min hand of some sort.

		// if we don't at least have an ace high, let's fold if our rnd is at some threshold.
		HandScore aceHigh = new HandScore();

		aceHigh.scores = scoreTypes.stream().map(s -> s.apply(
				new Card[]{new Card(14, 1), new Card(-1, -1), new Card(-2, -2), new Card(-6, -6), new Card(-7, -7)
				})).collect(toList());

		maybeFold(aceHigh);

		deck.draw(); // burn
		Card turn = deck.draw();
		System.out.println(); System.out.println("Turn: " + turn);
		board.add(turn);

		// If we don't at least have a pair of 9's, let's fold if our rnd is at some threshold.

		HandScore nines = new HandScore();
		nines.scores = scoreTypes.stream().map(s -> s.apply(
				new Card[]{new Card(9, 1), new Card(9, 2), new Card(-2, -2), new Card(-6, -6), new Card(-7, -7)
				})).collect(toList());

		maybeFold(nines);

		deck.draw(); // burn
		Card river = deck.draw();
		System.out.println();
		System.out.println("River: " + river);
		board.add(river);

		List<HandScore> hands = players.stream().map(Holdem::scoreHand).collect(toList());
		hands.sort(HandScore::compare);

		System.out.println();
		System.out.println("Winner: " + hands.stream().filter(h -> !players.get(h.playerNumber).folded).findFirst().get());
		System.out.println("Best hand: " + hands.get(0));

		System.out.println(Arrays.stream(hands.get(0).cards).map(String::valueOf).collect(joining(", ")));
	}
}

class Card {
	public static String FACE_NAMES[] = {"?", "?", "2", "3", "4", "5", "6", "7", "8", "9", "Ten", "Jack", "Queen", "King", "Ace"};
	public static String SUIT_NAMES[] = {"Clubs", "Diamonds", "Hearts", "Spades"};

	public int suit, face;

	public String toString(){return FACE_NAMES[face] + " of " + SUIT_NAMES[suit];}

	public Card(int face, int suit){
		this.suit = suit;
		this.face = face;
	}
}

class Deck {
	List<Card> cards = new ArrayList<>();
	int index = 0;
	public Deck(){for (int i = 0; i < 52; i++) cards.add(new Card(i % 13 + 2, i / 13));}
	public Card draw() { return cards.get(index++);}
	public void shuffle() { index = 0; Collections.shuffle(cards);}
}