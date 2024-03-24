package com.github.jurisliepins;

import java.io.IOException;

public class App {

    static class Receiver1 implements Actor.Receiver {

        private int counter = 0;

        @Override
        public Actor.NextState receive(Actor.Envelope envelope) {
            return switch (envelope) {
                case Actor.Envelope.Success ok -> success(ok);
                case Actor.Envelope.Failure error -> failure(error);
            };
        }

        private Actor.NextState success(Actor.Envelope.Success ok) {
            switch (ok.message()) {
                case String value -> {
                    System.out.println("ref 1 " + value + " " + counter++);
                    ok.sender().post(ok.message(), ok.self());
                }
                default -> System.out.println("default 1 " + ok.message());
            }
            return Actor.NextState.Receive;
        }

        private Actor.NextState failure(Actor.Envelope.Failure error) {
            System.out.println("failure 1 " + error.cause());
            return Actor.NextState.Terminate;
        }

    }

    private static Actor.NextState receive2(Actor.Envelope envelope) {
        return switch (envelope) {
            case Actor.Envelope.Success ok -> {
                switch (ok.message()) {
                    case String value -> {
                        System.out.println("ref 2 " + value);
                        ok.sender().post(ok.message(), ok.self());
                    }
                    default -> System.out.println("default 2 " + ok.message());
                }
                yield Actor.NextState.Receive;
            }
            case Actor.Envelope.Failure error -> {
                System.out.println("failure 2 " + error.cause());
                yield Actor.NextState.Terminate;
            }
        };
    }

    public static void main(String[] args) throws IOException {
        var system = new Actor.System();
        var ref1 = system.spawn(new Receiver1());
        var ref2 = system.spawn(App::receive2);
        ref1.post("hello", ref2);

        System.in.read();

        system.shutdown();

        System.in.read();
    }
}
