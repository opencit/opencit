/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.SecureRandom;

/**
 * A wrapper around SecureRandom that periodically re-seeds the generator.
 * 
 * You can set the maximum number of random bytes that will be generated
 * before the generator is re-seeded by calling <code>setMax(n)</code>.
 * 
 * If you set the max to zero then it will reseed every time you call one
 * of the next* methods.
 * 
 * The default maximum is Integer.MAX_VALUE.
 * 
 * <code>nextGaussian()</code> is not implemented at this time because we don't have 
 * visibility into how many random bytes are drawn from the generator
 * (it's 16 bytes per iteration, at least one iteration, but we don't know
 * how many iterations it might go through). 
 * 
 * 
 * XXX TODO  may need to create a new SecureRandom instance in the reseed method, 
 * see this post http://bouncy-castle.1462172.n4.nabble.com/Correct-use-of-SecureRandom-td3232395.html 
 * so in that case there's no point to having a constructor with securerandom
 * because we would be recreating it anyway to get a more random seed. the
 * alternative as discussed in the post is to make a cyrpto provider that implements
 * SecureRandom and simply does what we want.  then we only need the wrapper
 * methods ehre for convenience ( byte[] nextBytes(n)  is more fluent than int nextBytes(byte[]) )
 * 
 * 
 * @author jbuhacoff
 */
public class RandomSource {
    private SecureRandom random;
    private long current = 0; // number of random bytes already generated using current seed
    private long max = Integer.MAX_VALUE; // number of random bytes before we force a re-seed
    
    public RandomSource() {
        random = new SecureRandom();
    }
    
    public RandomSource(SecureRandom random) {
        this.random = random;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
    
    private void increment(int n) {
        current += n;
        if( current >= max || current < 0 ) { random.setSeed(random.generateSeed(8)); current = n; }
    }
    
    public byte[] nextBytes(int num) {
        increment(num);
        byte[] data = new byte[num];
        random.nextBytes(data);
        return data;
    }
    
    public boolean nextBoolean() {
        increment(1);
        return random.nextBoolean();
    }
    
    public int nextInt() {
        increment(4);
        return random.nextInt();
    }
    
    public int nextInt(int n) {
        increment(4);
        return random.nextInt(n);
    }
    
    public long nextLong() {
        increment(8);
        return random.nextLong();
    }
    
    public float nextFloat() {
        increment(4);
        return random.nextFloat();
    }
    
    public double nextDouble() {
        increment(8);
        return random.nextDouble();
    }
    
}
