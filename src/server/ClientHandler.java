package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

//            System.out.println("socket.getPort() "+ socket.getPort());
//            System.out.println("socket.getLocalPort() "+socket.getLocalPort());
//
//            System.out.println("socket.getInetAddress() "+socket.getInetAddress());
//            System.out.println("socket.getLocalSocketAddress() "+socket.getLocalSocketAddress());
//            System.out.println("socket.getRemoteSocketAddress() "+socket.getRemoteSocketAddress());


            new Thread(() -> {
                try {
                    // цикл авторизации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");
                            String enteredLogin = token[1];
                            String enteredPass = token[2];

                            Integer userId = AuthService.getUserIdByLoginAndPass(enteredLogin, enteredPass);
                            if (userId != null) {
                                sendMSG("/authok");
                                login = enteredLogin;
                                server.subscribe(this);
                                System.out.println("Клиент " + login + " авторизовался");
                                break;
                            } else {
                                sendMSG("Неверный логин / пароль");
                            }
                        }
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            break;
                        }

                        if (str.startsWith("/w")) {
                            String[] info = str.split(" ");
                            String userName = info[1];
                            String msg = info[2];

                            server.personalMsg(userName, login + " : " + msg);
                        }
                        else if (str.startsWith("/rename")) {
                            String[] info = str.split(" ");
                            String newLogin = info[1];

                            if (AuthService.renameUser(login, newLogin)) {
                                server.personalMsg(login, "server: Логин изменен");
                                login = newLogin;
                            }
                            else {
                                server.personalMsg(login, "server: Данный логин уже используется");
                            }
                        }
                        else {
                            server.broadcastMsg(login + " : " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                    System.out.println("Клиент " + login + " отключился");
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMSG(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
