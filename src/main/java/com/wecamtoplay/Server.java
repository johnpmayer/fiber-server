
package com.wecamtoplay;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import co.paralleluniverse.strands.*;
import java.io.*;
import java.net.*;

public class Server {

  private FiberScheduler scheduler;
  private static final int PORT = 9000;

  public Server() {
    scheduler = new FiberForkJoinScheduler("Test", 4, null, false);
  }

  public void start() {

    System.out.println("Go");

    try {

      Fiber serverLoop = new Fiber(scheduler, new SuspendableRunnable() {
        @Override
        public void run() throws SuspendExecution {

          System.out.println("Starting a server loop");

          try {

            System.out.println("Listening on address");
            FiberServerSocketChannel socket = FiberServerSocketChannel.open().bind(new InetSocketAddress(PORT));

            while (true) {

              FiberSocketChannel ch = socket.accept();
              System.out.println("Accepted a client");

              Fiber clientHandler = new Fiber(scheduler, new SuspendableRunnable() {
                @Override
                public void run() throws SuspendExecution {
                  System.out.println("Running in the client handler");
                }
              });

              clientHandler.start(); // No join
              System.out.println("Forking a client handler fiber");

            }

          } catch (IOException e) {
            throw new RuntimeException(e);
          }

        }

      });

      serverLoop.start();
      serverLoop.join();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static void main(String[] args) {
    Server server = new Server();
    server.start();
  }

}
