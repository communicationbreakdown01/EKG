import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Jessica Laxa on 05.12.2016.
 */
public class XMPP {
    public static final String host = "192.168.2.24";
    public static final int port = 5222;
    private String userName="";
    private String passWord="";
    AbstractXMPPConnection connection;
    ChatManager chatManager;
    Chat newChat;

    /* Erstellen der Konfigurationseinstellungen für die XMPP-Verbindung. Es werden Passwort,
    sowie Benutzername übergeben. Die Sicherheitseinstellungen sind im Developermode ausgeschaltet.
    Die Resource ist nach dem Client, der auf den Server verbunden wird, benannt. Der Servicename und
    der Host sind hier identisch. Der Port beläuft sich auf den standardmäßigen XMPP-Port 5222.
     */
    public void createConfig(String usrName, String pwd){
        this.userName=usrName;
        this.passWord=pwd;

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(userName, passWord);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setResource("sepp");
        configBuilder.setServiceName(host);
        configBuilder.setHost(host);
        configBuilder.setPort(port);

        connection = new XMPPTCPConnection(configBuilder.build());
    }


    /* Schließen der aufgebauten Verbindung.
     */
    public void disconnectConnection(){
        connection.disconnect();
    }


    /* Aufbau der Verbindung in einem try-catch Block, um Informationen über möglicherweise fehlgeschlagene
    Verbidungsaufbauversuche zu erhalten.
     */
    public void connectMe(){
        try {
            connection.connect();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }

    }


    /* Senden von Nachrichten durch das Getten von Verbindungen mit dem chatManager und den Empfängern.
     */
    public void sendMessage(String data){
        chatManager=ChatManager.getInstanceFor(connection);
        newChat = chatManager.createChat("admin@localhost");


        try {
            newChat.sendMessage(data);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }


    /* Empfangen von Chatnachrichten durch den Chatmanager.
     */
    public void listenChat(){
        ChatManager manager = ChatManager.getInstanceFor(this.connection);
        manager.addChatListener(new ChatManagerListener() {


            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new ChatMessageListener() {


                    public void processMessage(Chat chat, Message message) {
                        System.out.println(message.getBody());

                    }
                });

            }
        });
    }


    /* Login durch Benutzername und Passwort.
     */
    public void login(){
        try {
            connection.login(userName,passWord);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* Senden eines Status, der beim Empfänger angezeit wird.
     */
    public void setStatus(boolean available, String status) {

        Presence.Type type = available? Presence.Type.available: Presence.Type.unavailable;
        Presence presence = new Presence(type);

        presence.setStatus(status);
        try {
            connection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }


    /* Rostereintrag erstellen.
     */
    public void createEntry(String user, String name) throws Exception {
        System.out.println(String.format("New Sensor Available " + user + " with name " + name));
        Roster roster = Roster.getInstanceFor(connection);
        roster.createEntry(user, name, null);
    }


    /* Rostereintrag anzeigen.
     */
    public void printRoster() throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            System.out.println(String.format("Observer:" + entry.getName() + " - Status:" + entry.getStatus()));
        }
    }
}


