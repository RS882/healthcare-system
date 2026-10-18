package org.example;

import java.util.concurrent.*;

public class ExecutorServiceAndFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<String> futureA = executorService.submit(
                () -> "Transaction A completed"
        );

        Future<String> futureB = executorService.submit(
                () -> "Transaction B completed"
        );

        try {
            String resultA = futureA.get(5, TimeUnit.SECONDS);
            String resultB = futureB.get(5, TimeUnit.SECONDS);

            System.out.println(resultA);
            System.out.println(resultB);
        } finally {
            executorService.shutdown();
        }
    }
}
