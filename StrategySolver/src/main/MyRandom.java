package main;

// Author: Andrew Merrill

import java.util.Random;

public class MyRandom {
    public static Random random = new Random();

    // returns random double that is >= low and < high
    public static double nextDoubleInRange(double low, double high) {
        return low + random.nextDouble() * (high - low);
    }

    // returns random int in range that includes both low and high endpoints
    public static int nextIntInRange(int low, int high) {
        return low + random.nextInt(high - low + 1);
    }
}