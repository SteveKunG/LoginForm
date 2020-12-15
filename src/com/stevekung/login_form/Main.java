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

public class Main implements ActionListener, KeyListener
{
    private final JFrame frame = new JFrame();
    private JTextField usernameField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private JButton infoButton;
    private static final Gson GSON = new Gson();
    private TrayIcon trayIcon;
    private SystemTray tray;
    private Font font;

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^(?=.*\\d).{11}$");
    private static final String API_URL = "http://aritdoc.lpru.ac.th/api/api2/authentication";

    //TODO Generated QR Code > Phone > Server > Client

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Main::new);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public Main()
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
        else if (event.getSource() == this.infoButton)
        {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(frame, "สำหรับบุคลากร\r\n"
                    + "ใช้ ชื่อผู้ใช้ และ รหัสผ่าน ระบบบัญชีเดียว (Single Sign On.)\r\n"
                    + "\r\n"
                    + "สำหรับนักศึกษา\r\n"
                    + "ชื่อผู้ใช้งาน ใช้ รหัสนักศึกษา\r\n"
                    + "รหัสผ่าน ใช้ วันเดือนปีเกิด 6 หลัก และ\r\n"
                    + "เลขท้าย 4 หลัก ของเลขบัตรประจำตัวประชาชน\r\n"
                    + "\r\n"
                    + "ตัวอย่าง\r\n"
                    + "เกิดวันที่ 5 กันยายน 2545 > (050945)\r\n"
                    + "เลขบัตรประจำตัวประชาชน 2500300066489\r\n"
                    + "จะได้รหัสผ่านดังนี้ : 0509456489", "การลงชื่อเข้าใช้งานระบบ", JOptionPane.INFORMATION_MESSAGE);



            //            JOptionPane.showMessageDialog(frame, "<html><p><strong>สำหรับบุคลากร</strong></p>\r\n"
            //                    + "ใช้ ชื่อผู้ใช้ และ รหัสผ่าน ระบบบัญชีเดียว (Single Sign On.)\r\n"
            //                    + "<html><p><strong>สำหรับนักศึกษา</strong></p></html>\r\n"
            //                    + "ชื่อผู้ใช้งาน ใช้ รหัสนักศึกษา<html><p><br />รหัสผ่าน ใช้&nbsp;<em>วันเดือนปีเกิด 6 หลัก และ<br />เลขท้าย 4 หลัก ของเลขบัตรประจำตัวประชาชน</em></p></html>\r\n"
            //                    + "<html><p><br /><strong>ตัวอย่าง</strong><br />เกิดวันที่ 5 กันยายน 2545 &gt; (050945)<br />เลขบัตรประจำตัวประชาชน 250030006<span style=\"color: #ff0000;\">6489</span><br />จะได้รหัสผ่านดังนี้ :&nbsp;<strong>0509456489</strong></p></html>", "การลงชื่อเข้าใช้งานระบบ", JOptionPane.INFORMATION_MESSAGE);
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
            Main.displayErrorMessage("System Tray not supported", "Current operation system is not supported!");
        }

        Rectangle rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.frame.setUndecorated(true);
        this.frame.setAlwaysOnTop(true);
        this.frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.frame.setSize(rectangle.width, rectangle.height);
        this.frame.setType(JFrame.Type.UTILITY);
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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

        try
        {
            this.font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream("resources/kanit.ttf"));
            this.font = this.font.deriveFont(Font.PLAIN, 18.0F);
        }
        catch (FontFormatException | IOException e)
        {
            e.printStackTrace();
        }

        UIManager.put("OptionPane.messageFont", this.font);
        UIManager.put("OptionPane.buttonFont", this.font);
        this.frame.setFont(this.font);

        this.usernameField = new JTextField();
        this.usernameField.setBounds(121, 102, 174, 20);
        this.usernameField.addKeyListener(this);
        this.usernameField.setFont(this.font.deriveFont(Font.PLAIN, 18.0F));
        this.frame.getContentPane().add(this.usernameField);
        this.usernameField.setColumns(10);

        this.passwordTextField = new JPasswordField();
        this.passwordTextField.setBounds(121, 164, 174, 20);
        this.passwordTextField.addKeyListener(this);
        this.passwordTextField.setFont(this.passwordTextField.getFont().deriveFont(18.0F));
        this.frame.getContentPane().add(this.passwordTextField);
        this.passwordTextField.setColumns(10);

        this.loginButton = new JButton("Login");
        this.loginButton.addActionListener(this);
        this.loginButton.setBounds(121, 195, 89, 44);
        this.loginButton.setFont(this.font);
        this.frame.getContentPane().add(this.loginButton);

        JLabel usernameLabel = new JLabel("รหัสนักศึกษา/Student ID");
        usernameLabel.setBounds(111, 71, 199, 20);
        usernameLabel.setFont(this.font);
        this.frame.getContentPane().add(usernameLabel);

        JLabel passwordLabel = new JLabel("รหัสผ่าน/Password");
        passwordLabel.setBounds(113, 133, 197, 20);
        passwordLabel.setFont(this.font);
        this.frame.getContentPane().add(passwordLabel);

        JLabel titleLabel = new JLabel("LPRU Authentication");
        titleLabel.setBounds(111, 38, 184, 20);
        titleLabel.setFont(this.font);
        this.frame.getContentPane().add(titleLabel);

        this.infoButton = new JButton("");
        this.infoButton.addActionListener(this);
        this.infoButton.setBounds(251, 195, 44, 44);

        try
        {
            Image image = ImageIO.read(new File("resources/help.png")).getScaledInstance(44, 44, Image.SCALE_SMOOTH);
            this.infoButton.setIcon(new ImageIcon(image));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.frame.getContentPane().add(this.infoButton);
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
            URL url = new URL(Main.API_URL);
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
            LoginData data = Main.GSON.fromJson(result, LoginData.class);

            if (data.isLoggedIn())
            {
                System.out.println("logged in");

                try
                {
                    Main.this.tray.add(Main.this.trayIcon);
                    Main.this.frame.setVisible(false);
                }
                catch (AWTException e)
                {
                    e.printStackTrace();
                }
                return true;
            }
            else
            {
                Main.displayErrorMessage("ไม่สามารถล็อกอินได้", "โปรดเช็คชื่อผู้ใช้และรหัสผ่านให้ถูกต้อง");
                return false;
            }
        }
        catch (MalformedURLException | ProtocolException e)
        {
            Main.displayErrorMessage("ไม่สามารถเชื่อมต่อฐานข้อมูลได้", e.getMessage());
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            Main.displayErrorMessage("ไม่สามารถส่งข้อมูลไปยังฐานข้อมูลได้", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkUsername()
    {
        String username = this.usernameField.getText();

        if (username == null || username.isEmpty())
        {
            Main.displayInfoMessage("ชื่อผู้ใช้ว่างเปล่า", "โปรดกรอกในช่อง");
            return false;
        }

        Matcher usernameMat = Main.STUDENT_ID_PATTERN.matcher(username);

        if (usernameMat.matches())
        {
            return true;
        }
        else
        {
            Main.displayInfoMessage("ชื่อผู้ใช้ไม่ถูกต้อง", "ต้องเป็นรหัสนักศึกษาและความยาวอย่างน้อย 11 ตัว");
            return false;
        }
    }

    private boolean checkPassword()
    {
        String password = String.valueOf(this.passwordTextField.getPassword());

        if (password == null || password.isEmpty())
        {
            Main.displayInfoMessage("รหัสผ่านว่างเปล่า", "โปรดกรอกในช่อง");
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
        this.frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.tray.remove(this.trayIcon);
        this.passwordTextField.setText("");
    }

    private static void displayInfoMessage(String message, String info)
    {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, "เหตุผล: " + info, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void displayErrorMessage(String message, String info)
    {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, "เหตุผล: " + info, message, JOptionPane.ERROR_MESSAGE);
    }
}