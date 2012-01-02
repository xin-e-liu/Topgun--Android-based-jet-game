/*
 Copyright 2011 codeoedoc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.box.game.planeandroid;

import java.io.*;
import java.net.*;

public class SocketClient {
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;

    public void getConnection() {
        try {
            requestSocket = new Socket("127.0.0.1", 2004);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
        } catch (Exception e) {

        }
    }

    public void closeConnection() {

        try {
            in.close();
            out.close();
            requestSocket.close();
        } catch (Exception e) {

        }
    }

    public void sendXY(double x, double y) {
        try {
            out.writeDouble(x);
            out.writeDouble(y);
            out.flush();
        } catch (Exception classNot) {

        }
    }

    public double rcv() {
        double r = 0;
        try {
            int t = in.available();
            if (t != 0) {
                r = in.readDouble();
            } else {
                r = 0;
            }
        } catch (Exception classNot) {
            r = 0;
        }
        return r;
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
