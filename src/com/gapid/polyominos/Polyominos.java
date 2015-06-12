package com.gapid.polyominos;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class Polyominos {
    public static void main(String[] args) {
        int degree = 6;
        Set<Set<Integer>> finalSet = new HashSet<>(), dupes = new HashSet<>();

        // In parallel threads, find all valid shapes, weed out dupes
        getAllPositions(IntStream.range(0, degree * degree).toArray(), degree)
                .parallelStream().filter(Polyominos::isValid).map(Polyominos::transpose).distinct()
                .map(Polyominos::generateRotations).forEach(s -> {
            Set<Integer> firstShape = s.iterator().next();
            if (dupes.contains(firstShape)) return;
            dupes.addAll(s);
            finalSet.add(firstShape);
        });

        finalSet.forEach(Polyominos::print);
    }

    static void print(Set<Integer> set) {
        Map<Integer, Set<Integer>> canvas = new HashMap<>();
        set.forEach(v -> {
            canvas.putIfAbsent(v / set.size(), new HashSet<>());
            canvas.get(v / set.size()).add(v % set.size());
        });

        for (int i = 0; i < set.size(); i++) {
            canvas.putIfAbsent(i, new HashSet<>());
            for (int j = 0; j < set.size(); j++ )
                System.out.print(canvas.get(i).contains(j) ? '*' : ' ');
            System.out.println();
        }
    }

    private static Set<Set<Integer>> generateRotations(Set<Integer> ints) {
        int degree = ints.size();
        Set<Set<Integer>> rotations = new HashSet<>();
        rotations.add(ints);
        Set<Integer> lastSet = ints;
        for (int i = 0; i < 3; i++) {
            Set<Integer> set = new HashSet<>(); rotations.add(set);
            lastSet.forEach(v -> set.add(degree* (v % degree) + degree - (v / degree) - 1));
            lastSet = set;
        }
        return rotations.stream().map(Polyominos::transpose).collect(toSet());
    }

    public static Set<Integer> transpose(Set<Integer> set) {
        final int minY = set.stream().map(i -> i / set.size()).min(Integer::compare).get();
        final int minX = set.stream().map(i -> i % set.size()).min(Integer::compare).get();
        return set.stream().map(i -> i - minX - set.size() * minY).collect(toSet());
    }

    public static boolean isValid(Set<Integer> data){
        Integer[] set = data.toArray(new Integer[data.size()]);
        for (int i = 0 ; i < set.length - 1; i++ ){
            boolean found = false;
            for (int j = i + 1 ; !found && j < set.length; j++) {
                int difference = Math.abs(set[i] - set[j]);
                found = ((set[i] / set.length) == (set[j] / set.length) && difference == 1)  ||
                        ((set[i] % set.length) == (set[j] % set.length) && difference == set.length);
            }
            if (!found) return false;
        }
        return true;
    }

    static List<Set<Integer>> getAllPositions(int domain[],  int r) {
        int data[] = new int[r];
        List<Set<Integer>> combos = new ArrayList<>();
        nextCombination(domain, data, 0, domain.length - 1, 0, r, combos);
        return combos;
    }

    static void nextCombination(int domain[], int data[], int start,
                                int end, int index, int r, List<Set<Integer>> combos) {
        if (index == r) {
            Set<Integer> newSet = new HashSet<>();
            for (int aData : data) newSet.add(aData);
            combos.add(newSet);
            return;
        }
        for (int i=start; i<=end && end-i+1 >= r-index; i++) {
            data[index] = domain[i];
            nextCombination(domain, data, i + 1, end, index + 1, r, combos);
        }
    }
}