package com.gapid.elevator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Created by bbp on 6/11/2015.
 */
public class Elevator {
    private final String name;
    private double floor;
    private final int capacity;
    private final double speed;
    private double velocityPerTic;
    private State state = State.IDLE;

    public Elevator(String args){
        String[] fields = args.split(" ");
        this.name = fields[0];
        this.capacity = Integer.parseInt(fields[1]);
        this.speed = Double.parseDouble(fields[2]);
        this.floor = Integer.parseInt(fields[3]);
    }

    public static void main(String[] args) throws IOException {
        Path fileName = new File(args[0]).toPath();
        int elevatorCount = Files.lines(fileName).limit(1).map(Integer::parseInt).findFirst().get();
        List<Elevator> elevators = Files.lines(fileName).skip(1).limit(elevatorCount).map(Elevator::new).collect(toList());
        Map<Integer, List<Press>> presses = Files.lines(fileName).skip(2 + elevatorCount).map(Press::new).collect(groupingBy(e -> e.time, toList()));
        Map<String, Rider> riders = presses.values().stream().flatMap(l -> l.stream().map(p -> p.rider).distinct().map(Rider::new)).collect(toMap(e -> e.name, e -> e));

        double timeIncrement = getGCD(elevators);
        int maxFloor = presses.values().stream().flatMap(l->l.stream().map(p->Integer.max(p.destination, p.source))).max(Integer::compare).get();
        int maxTime = presses.keySet().stream().max(Integer::compare).get();
        BitSet buttonPresses = new BitSet(maxFloor);

        double currentTime = 0;
        List<Rider> queuedRiders = new ArrayList<>();
        List<Rider> ridingRiders = new ArrayList<>();

        while (true) {
           if (currentTime == (int) currentTime) {
               List<Press> newPresses = presses.get((int) currentTime);
               queuedRiders.addAll(newPresses.stream().map(e -> e.rider).map(riders::get).collect(toList()));
               newPresses.stream().map(e->e.source).forEach(buttonPresses::set);
               newPresses.stream().forEach(p -> riding);
           }
        }
    }

    static double getGCD(List<Elevator> elevators) {
        int factor = 1;
        double min = elevators.stream().map(e -> e.speed).min(Double::compare).get();
        while (min * factor < 1) factor*=10;

        int result = (int) (factor * elevators.get(0).speed);
        for(int i = 1; i < elevators.size(); i++) result = gcd(result, (int)(factor * elevators.get(i).speed) );
        return (double) result / (double) factor;
    }

    private static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}

class Press{
    String rider; int time, source, destination;

    public Press(String args) {
        String fields[] = args.split();
        this.rider = fields[0]; this.time = Integer.parseInt(fields[1]);
        this.source = Integer.parseInt(fields[2]);
        this.destination = Integer.parseInt(fields[3]);
    }
}

class Rider{
    String name; double waitTime; double rideTime;
    Rider(String name) { this.name = name};
    public boolean equals(Object t) { return ((Rider) t).name.equals(name);}

    public String getName() {return name; }
}

enum State{IDLE, UP, DOWN};
