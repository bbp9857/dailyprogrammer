package com.gapid.graph;

/**
 * Created by bbp on 6/5/2015.
 */
public class EdgeAppender {
	public static void main(String[] args) {
		String str = "(0, 4), (1, 31), (1, 63), (1, 92), (1, 97), (2, 72), (5, 11), (6, 95), (7, 36), (7, 52), (8, 27), (9, 44), (10, 15), (10, 51), (12, 8), (14, 82), (15, 42), (16, 14), (16, 70), (17, 78), (18, 10), (19, 37), (20, 15), (21, 14), (21, 17), (21, 45), (22, 4), (22, 23), (22, 34), (23, 46), (24, 89), (25, 3), (25, 40), (25, 47), (26, 38), (27, 33), (27, 91), (28, 60), (29, 89), (32, 77), (32, 97), (33, 57), (33, 59), (33, 87), (34, 7), (37, 31), (41, 16), (45, 67), (45, 90), (46, 80), (47, 98), (49, 97), (50, 78), (52, 60), (52, 99), (53, 28), (53, 37), (55, 36), (55, 73), (55, 75), (56, 23), (56, 78), (57, 45), (58, 6), (60, 14), (61, 31), (61, 39), (63, 11), (63, 30), (63, 94), (64, 68), (65, 1), (67, 81), (67, 87), (69, 30), (69, 37), (69, 47), (70, 9), (70, 32), (71, 26), (71, 75), (72, 54), (75, 13), (75, 43), (75, 61), (75, 89), (76, 37), (76, 66), (77, 9), (77, 45), (79, 20), (79, 26), (80, 57), (80, 67), (81, 31), (82, 35), (82, 37), (83, 12), (83, 77), (84, 57), (84, 91), (85, 66), (86, 59), (87, 74), (89, 2), (89, 36), (90, 95), (91, 72), (92, 9), (92, 40), (92, 88), (93, 56), (93, 62), (93, 64), (94, 2), (96, 60), (97, 37), (97, 48), (99, 44), (99, 56)";
		str.chars().mapToObj(c -> c == ')' ? (", " + (int)(Math.random() * 3000) + ")") : "" + (char) c).forEach(System.out::print);
	}
}
