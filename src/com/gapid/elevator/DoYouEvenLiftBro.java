package com.gapid.elevator;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import static java.util.Comparator.comparing;

public class DoYouEvenLiftBro {
    static int maxFloor, minFloor, LINGER_TIME = 1;
    public static void main(String[] args) throws IOException, InterruptedException {
        args = new String[]{"riders.txt"};
        Path fileName = new File(args[0]).toPath();
        int numElevators = Files.lines(fileName).limit(1).map(Integer::parseInt).findFirst().get();
        List<Elevator> elevators = Files.lines(fileName).skip(1).limit(numElevators)
                .map(Elevator::new).collect(toList());
        Map<Integer, List<Call>> presses = Files.lines(fileName).skip(2 + numElevators).map(Call::new)
                .collect(groupingBy(e -> e.time, toList()));
        maxFloor = presses.values().stream().flatMap(l->l.stream().map(p->Integer.max(p.dest, p.source)))
                .max(Integer::compare).get();
        System.out.println(maxFloor);
        minFloor = 1;
        int maxTime = presses.keySet().stream().max(Integer::compare).get(), time = -1;
        List<Call> queued = new ArrayList<>(), space = new ArrayList<>();

        while (time++ < maxTime || !queued.isEmpty()
                || elevators.stream().anyMatch(e->!e.riders.isEmpty())){
            System.out.println("Time: " + time);

            queued.forEach(Call::incrementQueueTime);
            System.out.println(queued.stream().map(
                    p->p.rider + " waits on " + p.source + " to " + p.dest).collect(joining(", ")));

            List<Call> newCalls = presses.getOrDefault(time, new ArrayList<>());
            if (!newCalls.isEmpty())
                System.out.println("Riders " + newCalls.stream().map(p -> p.rider + " on floor "
                        + p.source + " now waiting for " + p.dest).collect(joining(", ")));
            queued.addAll(presses.getOrDefault(time, space));
            elevators.forEach(e -> e.riders.forEach(Call::incrementRidingTime));
            elevators.forEach(Elevator::advance);

            // Drop off passengers
            elevators.forEach(elevator -> {
                List<Call> toDropOff = elevator.riders.stream().filter(r -> elevator.hasPassedThrough(r.dest))
                        .collect(toList());
                elevator.riders.removeAll(toDropOff);

                if (toDropOff.size() > 0)
                    System.out.println("Elevator " + elevator.name + " dropped off "
                            + toDropOff.stream().map(p -> p.rider).collect(joining(", ")));
            });

            // Stop elevators who've lost their purpose in life.
            elevators.stream().filter(e -> e.isEmpty() && !e.isIdle()).forEach(elevator -> {
                if (queued.stream().noneMatch(p -> elevator.isHeadedTowards(p.source))) {
                    elevator.direction = D.IDLE;
                    System.out.println("Elevator " + elevator.name + " idling now.");
                }
            });

            // pick up passed callers going the same direction
            elevators.stream().forEach(elevator -> {
                List<Call> toPickUp = queued.stream()
                        .filter(press -> elevator.hasPassedThrough(press.source)
                                && (elevator.isIdle() || elevator.isGoing(press.direction)))
                        .limit(elevator.capacity - elevator.riders.size()).collect(toList());

                if (toPickUp.size() > 0) {
                    if (elevator.isIdle()) {
                        // there could be both up / down waiters at the same spot.
                        // the fair thing to do is to choose the first one.
                        D d = toPickUp.iterator().next().direction;
                        toPickUp = toPickUp.stream().filter(p -> p.direction == d).collect(toList());
                    }
                    queued.removeAll(toPickUp);
                    System.out.println("Elevator " + elevator.name + " has picked up riders "
                            + toPickUp.stream().map(p -> p.rider).collect(joining(", ")));
                    if (elevator.linger == 0) elevator.linger = LINGER_TIME;

                    elevator.riders.addAll(toPickUp = toPickUp.stream()
                            .filter(p -> p.dest != p.source).collect(toList()));
                    if (elevator.isIdle() && !toPickUp.isEmpty())
                        elevator.direction = toPickUp.iterator().next().direction;
                }
            });

            // sort the callers by first-in-first-out grouping by floor / direction.
            List<Call> distinctQueue = queued.stream()
                    .map(p -> new Call(p.source, p.direction, -1)).distinct().collect(toList());

            // dispatch any idle elevators who would reach a caller first
            while (elevators.stream().anyMatch(Elevator::isIdle) && distinctQueue.size() > 0) {
                Call call = distinctQueue.remove(0);
                elevators.stream().sorted(comparing(e -> e.getTimeTo(call)
                        - e.speed)).findFirst()
                        .ifPresent(e -> {
                            if (e.isIdle()) e.direction = e.getDirectionTo(call.source);
                        });
            }
        }
    }
}
class Elevator {
    String name;
    double floor, speed;
    int capacity;
    private double lastFloor;
    int linger = 0;
    D direction = D.IDLE;
    public List<Call> riders = new ArrayList<>();

    public Elevator(String args){
        String[] fields = args.split(" ");
        this.name = fields[0];
        this.capacity = Integer.parseInt(fields[1]);
        this.speed = Double.parseDouble(fields[2]);
        this.floor = this.lastFloor = Integer.parseInt(fields[3]);
    }

    public void advance() {
        if (isIdle()) {return;}
        if (linger > 0) { linger --; System.out.println("Elevator " + name + " is lingering"); return;}
        lastFloor = floor;  // (int) (direction== D.DOWN ? Math.ceil(floor) : (int)floor);
        floor += speed * direction.factor;
        floor = (int) (Math.round(floor * 100));
        floor = floor / 100;
        System.out.println("Elevator " + name + " now at " + floor
                + "; last floor was " + lastFloor + "; direction = " + direction.name()
                + "; riders: " + riders.stream().map(r->r.rider + " (" + r.dest + ")")
                .collect(joining(", ")));
    }

    public boolean isIdle(){ return direction == D.IDLE;}
    public boolean isGoing(D d) {return direction == d;}
    public boolean isEmpty() { return riders.isEmpty(); }
    D getDirectionTo(int source) { return floor > source ? D.DOWN : D.UP; }
    public boolean isHeadedTowards(int source) { return isGoing(D.DOWN) ? source  < floor : floor  < source; }

    public boolean hasPassedThrough(int dest) {
        return floor == dest || (isGoing(D.DOWN) && dest < lastFloor && dest >= floor)
                || (isGoing(D.UP) && dest <= floor && dest > lastFloor);
    }

    public double getTimeTo(Call call) {
        if (Math.abs((double) call.source - floor) < 1) return 0;

        if (isHeadedTowards(call.source) || isIdle()) {
            if (getDirectionTo(call.source) == call.direction)
                return (call.source - floor) / speed;
            if (getDirectionTo(call.source) == D.DOWN) return (floor + call.source) / speed;
            return ((DoYouEvenLiftBro.maxFloor - floor)
                    + (DoYouEvenLiftBro.maxFloor - call.source)) / speed;
        }
        if (getDirectionTo(call.source) != call.direction) {
            if (isGoing(D.DOWN))  return (floor + call.source) / speed;
            return ((DoYouEvenLiftBro.maxFloor - floor)
                    + (DoYouEvenLiftBro.maxFloor - call.source)) / speed;
        }
        else {
            if (isGoing(D.DOWN))
                return ((floor + DoYouEvenLiftBro.maxFloor
                        + DoYouEvenLiftBro.maxFloor - call.source) / speed);
            return ((DoYouEvenLiftBro.maxFloor - floor
                    + DoYouEvenLiftBro.maxFloor + call.source) / speed);
        }
    }
}

class Call {
    double queueTime, rideTime;
    String rider; int time, source, dest;
    public D direction;

    public Call(String args) {
        String fields[] = args.split(" ");
        this.rider = fields[0]; this.time = Integer.parseInt(fields[1]);
        this.source = Integer.parseInt(fields[2]);
        this.dest = Integer.parseInt(fields[3]);
        direction = source > dest ? D.DOWN : D.UP;
    }

    public Call(int source, D direction, int time) {
        this.source = source; this.direction = direction; this.time = time;
    }

    void incrementRidingTime() { rideTime++; }
    public boolean equals(Object p) {
        return this.source == ((Call) p).source
                && this.direction == ((Call) p).direction && this.time == ((Call) p).time;
    }
    void incrementQueueTime() { queueTime++; }
}

enum D {IDLE(0), UP(1), DOWN(-1); public double factor; D(int i) {this.factor = i;} }