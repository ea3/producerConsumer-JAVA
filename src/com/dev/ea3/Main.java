package com.dev.ea3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.dev.ea3.Main.EOF;

public class Main {
    public static final String EOF = "EOF";

    public static void main(String[] args) {
        List<String> buffer = new ArrayList<String>();
        ReentrantLock bufferLock = new ReentrantLock();

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        MyProducer producer = new MyProducer(buffer, ThreadColor.ANSI_BLUE, bufferLock);
        MyConsumer consumer1 = new MyConsumer(buffer, ThreadColor.ANSI_RED, bufferLock);
        MyConsumer consumer2 = new MyConsumer(buffer, ThreadColor.ANSI_PURPLE, bufferLock);

        executorService.execute(producer);
        executorService.execute(consumer1);
        executorService.execute(consumer2);

        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(ThreadColor.ANSI_GREEN + "I'm being printed from the callable class");
                return "This is the callable result";
            }
        });

        try{
            System.out.println(future.get());
        }catch(InterruptedException e){
            System.out.println("Thread running the task was interrupted");
        }catch(ExecutionException e){
            System.out.println("Something went wrong ");
        }
        executorService.shutdown();
    }
}

class MyProducer implements Runnable{
    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public MyProducer(List<String> buffer, String color, ReentrantLock bufferLock) {
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }

    public void run(){
        Random random = new Random();
        String [] nums = {"1", "2", "3", "4", "5"};

        for(String num : nums){
            try{
                System.out.println(color + "Adding..." + num);
                bufferLock.lock();
                try{
                    buffer.add(num);
                }finally{
                    bufferLock.unlock();
                }



                Thread.sleep(random.nextInt(2000));
            }catch(InterruptedException e){
                System.out.println("Producer was interrupted");
            }
        }

        System.out.println(color + "Adding EOF and exiting");
        bufferLock.lock();
        try{
            buffer.add("EOF");
        }finally {
            bufferLock.unlock();
        }
    }
}

class MyConsumer implements Runnable{
    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public MyConsumer(List<String> buffer, String color, ReentrantLock bufferLock ) {
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }

    public void run(){
        while(true){
                bufferLock.lock();
                try{
                    if(buffer.isEmpty()){
                        continue;
                    }
                    if(buffer.get(0).equals(EOF)){
                        System.out.println(color + "Exiting");
                        break;
                    }else{
                        System.out.println(color + "Rmoved " + buffer.remove(0));
                    }
                }finally{
                    bufferLock.unlock();
                }
        }
    }

}