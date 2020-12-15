package com.stevekung.login_form;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.gson.Gson;

public class Test implements ActionListener, KeyListener
{
    private final JFrame frame = new JFrame();
    private JTextField usernameField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private static final Gson GSON = new Gson();
    private TrayIcon trayIcon;
    private SystemTray tray;

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^(?=.*\\d).{11}$");
    private static final String API_URL = "http://aritdoc.lpru.ac.th/api/api2/authentication";

    //TODO Generated QR Code > Phone > Server > Client

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Test::new);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public Test()
    {
        this.init();

        try
        {
            this.frame.setIconImage(ImageIO.read(new File("resources/icon.png")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == this.loginButton)
        {
            if (this.performedLogin())
            {
                this.unfocusComponent(this.loginButton);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        if (event.isAltDown() && event.getKeyCode() == KeyEvent.VK_F4)
        {
            this.frame.dispose();
        }

        if (event.getSource() == this.usernameField || event.getSource() == this.passwordTextField)
        {
            if (event.getKeyCode() == KeyEvent.VK_ENTER)
            {
                if (this.performedLogin())
                {
                    this.unfocusComponent(this.usernameField);
                    this.unfocusComponent(this.passwordTextField);
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {}

    @Override
    public void keyReleased(KeyEvent event) {}

    private void init()
    {
        if (SystemTray.isSupported())
        {
            this.tray = SystemTray.getSystemTray();
            Image image = null;

            try
            {
                image = ImageIO.read(new File("resources/icon.png"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            PopupMenu popup = new PopupMenu();
            MenuItem logout = new MenuItem("Logout");
            logout.addActionListener(event -> this.processLogout());
            popup.add(logout);
            this.trayIcon = new TrayIcon(image, "Net Login", popup);
            this.trayIcon.setImageAutoSize(true);
        }
        else
        {
            Test.displayErrorMessage("System Tray not supported", "Current operation system is not supported!");
        }

        boolean hideAll = true;

        if (hideAll)
        {
            Rectangle rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            this.frame.setUndecorated(true);
            this.frame.setAlwaysOnTop(true);
            this.frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            this.frame.setSize(rectangle.width, rectangle.height);
            this.frame.setType(JFrame.Type.UTILITY);
            this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        else
        {
            this.frame.setBounds(100, 100, 450, 300);
        }

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        this.frame.getContentPane().addKeyListener(this);
        this.frame.getContentPane().setFocusable(true);
        this.frame.getContentPane().setFocusTraversalKeysEnabled(false);
        this.frame.getContentPane().setLayout(null);

        this.usernameField = new JTextField();
        this.usernameField.setBounds(140, 112, 86, 20);
        this.usernameField.addKeyListener(this);
        this.frame.getContentPane().add(this.usernameField);
        this.usernameField.setColumns(10);

        this.passwordTextField = new JPasswordField();
        this.passwordTextField.setBounds(140, 154, 86, 20);
        this.passwordTextField.addKeyListener(this);
        this.frame.getContentPane().add(this.passwordTextField);
        this.passwordTextField.setColumns(10);

        this.loginButton = new JButton("Login");
        this.loginButton.addActionListener(this);
        this.loginButton.setBounds(137, 185, 89, 23);
        this.frame.getContentPane().add(this.loginButton);

        JLabel lblNewLabel = new JLabel("Student ID");
        lblNewLabel.setBounds(51, 115, 58, 14);
        this.frame.getContentPane().add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Password");
        lblNewLabel_1.setBounds(51, 157, 46, 14);
        this.frame.getContentPane().add(lblNewLabel_1);
    }

    private boolean performedLogin()
    {
        boolean checkedUsername = this.checkUsername();

        if (!checkedUsername)
        {
            return false;
        }

        boolean checkedPassword = this.checkPassword();

        if (!checkedPassword)
        {
            return false;
        }

        try
        {
            URL url = new URL(Test.API_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("username", this.usernameField.getText());
            arguments.put("password", String.valueOf(this.passwordTextField.getPassword()));

            String ip = "10.0.0.1";

            try (DatagramSocket socket = new DatagramSocket())
            {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
            }

            arguments.put("ip", ip);
            StringJoiner joiner = new StringJoiner("&");

            for (Map.Entry<String, String> entry : arguments.entrySet())
            {
                joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            }

            byte[] out = joiner.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();

            try (OutputStream os = http.getOutputStream())
            {
                os.write(out);
            }

            String result = new BufferedReader(new InputStreamReader(http.getInputStream())).lines().collect(Collectors.joining("\n"));
            LoginData data = Test.GSON.fromJson(result, LoginData.class);

            if (data.isLoggedIn())
            {
                System.out.println("logged in");

                try
                {
                    Test.this.tray.add(Test.this.trayIcon);
                    Test.this.frame.setVisible(false);
                }
                catch (AWTException e)
                {
                    e.printStackTrace();
                }
                return true;
            }
            else
            {
                Test.displayErrorMessage("Failed to login", "Please make sure your username or password is correct!");
                return false;
            }
        }
        catch (MalformedURLException | ProtocolException e)
        {
            Test.displayErrorMessage("An exception occurred when trying to connect the database", e.getMessage());
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            Test.displayErrorMessage("An exception occurred when trying to post data", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkUsername()
    {
        String username = this.usernameField.getText();

        if (username == null || username.isEmpty())
        {
            Test.displayInfoMessage("Empty username!", "Please complete fill in the box");
            return false;
        }

        Matcher usernameMat = Test.STUDENT_ID_PATTERN.matcher(username);

        if (usernameMat.matches())
        {
            return true;
        }
        else
        {
            Test.displayInfoMessage("Invalid username pattern!", "Must be your Student ID and at least 11 characters");
            return false;
        }
    }

    private boolean checkPassword()
    {
        String password = String.valueOf(this.passwordTextField.getPassword());

        if (password == null || password.isEmpty())
        {
            Test.displayInfoMessage("Empty password!", "Please complete fill in the box");
            return false;
        }
        return true;
    }

    private void unfocusComponent(Component com)
    {
        com.setFocusable(false);
        com.setFocusable(true);
    }

    private void processLogout()
    {
        this.frame.setVisible(true);
        this.frame.setExtendedState(Frame.NORMAL);
        this.tray.remove(this.trayIcon);
        this.passwordTextField.setText("");
    }

    private static void displayInfoMessage(String message, String info)
    {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, "Reason: " + info, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void displayErrorMessage(String message, String info)
    {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, "Reason: " + info, message, JOptionPane.ERROR_MESSAGE);
    }
}