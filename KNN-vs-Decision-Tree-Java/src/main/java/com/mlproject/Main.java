package com.mlproject;


import com.mlproject.server.SimpleWebServer;

import java.io.IOException;





public class Main {
    public static void main(String[] args) {
        System.out.println("Prolab 2. Proje");
        try {
            

            SimpleWebServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Sunucu baslatilamadi: " + e.getMessage());
        }
    }



}
