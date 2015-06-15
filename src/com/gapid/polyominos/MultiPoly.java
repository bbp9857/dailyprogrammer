package com.gapid.polyominos;

import java.awt.*;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Created by bbp on 6/14/2015.
 */
public class MultiPoly {
    final private int factor;

    public MultiPoly(int i) {
        this.factor = i;
    }

    final Set<Set<Point>> cached = new HashSet<>();
    final Set<Set<Point>> allShapes = new HashSet<>();

    enum D {
        NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);
        private final int x, y;
        D(int x, int y) { this.x = x; this.y = y; }
    };
    public static void main(String[] args) {
        new MultiPoly(4).go();
        Set<Point> shape = new HashSet<>();
        shape.add(new Point(0,1));
        shape.add(new Point(1,1));
        shape.add(new Point(2,1));
        shape.add(new Point(2,2));

        MultiPoly multiPoly = new MultiPoly(4);
        multiPoly.print(shape);
        Set<Set<Point>> sets = multiPoly.generateRotations(shape);
        sets.forEach(multiPoly::print);
        multiPoly.print(multiPoly.reflectShape(shape));
    }

    void go(){
        Set<Point> root = new HashSet<>();
        Point startingBlock = new Point(0, 0);
        root.add(startingBlock);

        buildShapes(root, startingBlock);
        allShapes.forEach(this::print);
    }

    void buildShapes(Set<Point> currentShape, Point startingBlock) {
        if (currentShape.size() == factor) {
            allShapes.add(currentShape);
            return;
        }
        for (D direction : D.values()) {
            Point nextBlock = new Point(startingBlock.x + direction.x, startingBlock.y + direction.y);
            if (currentShape.contains(nextBlock)) continue;
            Set<Point> newShape = appendShape(currentShape, nextBlock);
            if (!isValid(newShape)) {
                print(newShape);
                throw new RuntimeException("Generated an invalid block.. strange!");
            }
            if (!cached.add(transpose(newShape))) continue;
            cached.addAll(generateRotations(newShape));
            Set<Point> reflected = reflectShape(newShape);
            cached.addAll(generateRotations(reflected));

            buildShapes(newShape, nextBlock);
        }
    }

    public static boolean isValid(Set<Point> data){
        for (Point p : data) {
            if (!(data.contains(new Point(p.x, p.y + 1)) ||
                data.contains(new Point(p.x, p.y - 1)) ||
                data.contains(new Point(p.x + 1, p.y)) ||
                data.contains(new Point(p.x - 1, p.y))))
            return false;
        }
        return true;
    }

    private Set<Point> appendShape(Set<Point> currentShape, Point nextBlock) {
        Set<Point> newShape = new HashSet<>(currentShape);
        newShape.add(nextBlock);
        return newShape;
    }

    private Set<Point> reflectShape(Set<Point> set) {
        return transpose(set.stream().map(i -> new Point(-i.x, i.y)).collect(toSet()));
    }

    public static Set<Point> transpose(Set<Point> set) {
        final int minX = (int) (set.stream().map(Point::getX).min(Double::compare).get().doubleValue());
        final int minY = (int) (set.stream().map(Point::getY).min(Double::compare).get().doubleValue());
        return set.stream().map(i -> new Point(i.x - minX, i.y - minY)).collect(toSet());
    }


    private Set<Set<Point>> generateRotations(Set<Point> ints) {
        Set<Set<Point>> rotations = new HashSet<>();
        rotations.add(transpose(ints));
        Set<Point> lastSet = ints;
        for (int i = 0; i < 3; i++) {
            Set<Point> set = new HashSet<>(); rotations.add(set);

            set.addAll(lastSet.stream().map(p -> new Point(factor - p.y - 1, p.x)).collect(toSet()));
            lastSet = set;
        }
        return rotations.stream().map(MultiPoly::transpose).collect(toSet());
    }

    void print(Set<Point> set) {

        Map<Integer, Set<Integer>> canvas = set.stream().collect(groupingBy(p -> p.y, mapping(p -> p.x, toSet())));
        set.stream().forEach(System.out::println);

        for (int i = 0; i < factor; i++) {
//            canvas.putIfAbsent(i, new HashSet<>());
            if (canvas.get(i) == null) {
                System.out.println();
                continue;
            }
            for (int j = 0; j < factor; j++ ) {
                System.out.print(canvas.get(i).contains(j) ? '*' : ' ');
            }
            System.out.println();
        }
        System.out.println("-----");
    }
}


