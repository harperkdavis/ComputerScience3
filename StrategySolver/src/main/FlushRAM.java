package main;

class FlushRAM
{
  public static void main(String[] args)
  {
    System.out.println("max heap size is " + Runtime.getRuntime().maxMemory()/1024.0/1024.0/1024.0 + " gigs");

    int megs = (int) (Runtime.getRuntime().maxMemory()/1024/1024) ;

    int megsToFlush = megs - 100;
    int longs;
    long[] array;

    while (true) {
      try {
        longs = megsToFlush * 1024 * 128;
        array = new long[longs];
        break;
      }
      catch (OutOfMemoryError oome) {
        megsToFlush -= 100;
      }
    }

    System.out.println("flushing " + megsToFlush + " megs...");
    int pass = 1;
    long sum;
    while (true) {
      sum = 0;
      for (int i=0; i<longs; i++) {
        array[i] = array[i] + 1;
        sum += array[i];
      }
      System.out.println("pass " + pass++ + " complete with sum " + sum);
    }
  }
}
