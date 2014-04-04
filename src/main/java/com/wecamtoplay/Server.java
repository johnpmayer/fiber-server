
package com.wecamtoplay;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import co.paralleluniverse.strands.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

  private static final int PORT = 9000;

  public class ClientHandler implements SuspendableRunnable {

    private FiberSocketChannel chan;
  
    public ClientHandler(FiberSocketChannel chan) {
      this.chan = chan;
    }

    @Override
    public void run() throws SuspendExecution {
      try {
        System.out.println("Handling client... just closing it");
        this.chan.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  }

  public class AcceptLoop implements SuspendableRunnable {
    @Override
    public void run() throws SuspendExecution {
      try {
        System.out.println("Binding to address");
        FiberServerSocketChannel socket = FiberServerSocketChannel.open().bind(new InetSocketAddress(PORT));
        System.out.println("Listening...");
        while (true) {
          FiberSocketChannel chan = socket.accept();
          System.out.println("Accepted a client");
          Fiber clientHandler = new Fiber(new ClientHandler(chan));
          System.out.println("Forking a client handler fiber");
          clientHandler.start(); // No join
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void start() {
    Fiber acceptLoop = new Fiber(new AcceptLoop());
    acceptLoop.start();
    try {
      acceptLoop.join();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    Server server = new Server();
    server.start();
  }

}
