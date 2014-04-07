
package com.wecamtoplay;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import co.paralleluniverse.strands.*;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
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
      System.out.println("Handling client");
      ByteBuffer inBuffer = ByteBuffer.allocate(1);
      try {
        while (true) {
          int bytesRead = this.chan.read(inBuffer);
          System.out.println("read stuff: " + bytesRead);
          if (bytesRead < 1) { break; }
          int bytesWritten = this.chan.write(inBuffer);
          System.out.println("wrote stuff: " + bytesWritten);
        }
        this.chan.close();
        System.out.println("Closed.");
      } catch (IOException e) {
        System.out.println(e.toString());
        throw new RuntimeException(e);
      }
    }

  }

  public void start() {
    Fiber<Void> acceptLoop = new  Fiber<Void>((SuspendableRunnable)() -> {
      try {
        FiberServerSocketChannel socket = FiberServerSocketChannel.open().bind(new InetSocketAddress(PORT));
        System.out.println("Listening...");
        while (true) {
          FiberSocketChannel chan = socket.accept();
          Fiber<Void> clientHandler = new Fiber<Void>(new ClientHandler(chan));
          System.out.println("Forking a client handler fiber");
          clientHandler.start(); // No join
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
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
