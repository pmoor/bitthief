/*
 * BitThief - A Free Riding BitTorrent Client
 * Copyright (C) 2006 Patrick Moor <patrick@moor.ws>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

package ws.moor.bt.http;

import org.apache.log4j.Logger;
import ws.moor.bt.util.LoggingUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class HttpServer {

  private final static Logger logger = LoggingUtil.getLogger(HttpServer.class);

  private final int listenPort;
  private final RequestHandler handler;
  private final Thread listenerThread;
  private final HttpServer.Listener listener;
  private boolean quit = false;


  public HttpServer(RequestHandler handler) {
    this(-1, handler);
  }

  public HttpServer(int listenPort, RequestHandler handler) {
    this.listenPort = listenPort;
    this.handler = handler;

    listener = new Listener();
    listenerThread = new Thread(listener, "HTTP Server on Port " + listenPort);
    listenerThread.start();
  }

  public int getListeningPort() {
    return listener.getListeningPort();
  }

  public synchronized void stop() {
    quit = true;
  }

  public boolean isReady() {
    return listener.ready;
  }

  private synchronized boolean doQuit() {
    return quit;
  }

  public boolean isStopped() {
    return listener.stopped;
  }

  private class Listener implements Runnable {

    private boolean ready = false;
    private boolean stopped = false;
    private ServerSocket serverSocket;

    public void run() {
      try {
        execute();
      } catch (Exception e) {
        logger.warn("http listener unexpectedly quit", e);
      }
    }

    private void execute() throws IOException {
      logger.info("http listener starting");
      serverSocket = new ServerSocket();
      if (listenPort > 0) {
        serverSocket.bind(new InetSocketAddress(listenPort));
      } else {
        serverSocket.bind(null);
      }
      serverSocket.setSoTimeout(100);
      while (!doQuit()) {
        ready = true;
        Socket clientSocket = null;
        try {
          clientSocket = serverSocket.accept();
        } catch (SocketTimeoutException e) {
          continue;
        }
        Reader reader = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader);

        List<String> request = new ArrayList<String>();
        String line = bufferedReader.readLine();
        while (line != null && line.length() > 0) {
          request.add(line);
          line = bufferedReader.readLine();
        }

        OutputStream outputStream = clientSocket.getOutputStream();
        handler.handleRequest(request.toArray(new String[0]), outputStream);

        clientSocket.close();
      }
      serverSocket.close();
      logger.info("http listener stopped");
      stopped = true;
    }

    public int getListeningPort() {
      return serverSocket.getLocalPort();
    }
  }

  public interface RequestHandler {
    public void handleRequest(String[] request, OutputStream result) throws IOException;
  }
}
