package client;

public class ClientReaderThread extends Thread {

    private final ChatClient client;
    private final client.ui.ChatWindow ui;

    public ClientReaderThread(ChatClient client, client.ui.ChatWindow ui) {
        this.client = client;
        this.ui = ui;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = client.getReader().readLine()) != null) {
                ui.appendMessage(line);
            }
        } catch (Exception ignored) {}
    }
}
