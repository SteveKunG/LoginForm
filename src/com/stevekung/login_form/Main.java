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
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;

public class Main extends JFrame implements ActionListener, KeyListener
{
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private JButton infoButton;
    private MenuItem logoutMenu;
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

    public Main()
    {
        this.init();

        try
        {
            this.setIconImage(ImageIO.read(new File("resources/icon.png")));
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
        else if (event.getSource() == this.logoutMenu)
        {
            this.processLogout();
        }
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        if (event.isAltDown() && event.getKeyCode() == KeyEvent.VK_F4)
        {
            this.dispose();
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
            this.logoutMenu = new MenuItem("Logout");
            this.logoutMenu.addActionListener(this);
            popup.add(this.logoutMenu);
            this.trayIcon = new TrayIcon(image, "Net Login", popup);
            this.trayIcon.setImageAutoSize(true);
        }
        else
        {
            Main.displayErrorMessage("System Tray not supported", "Current operation system is not supported!");
        }

        Rectangle rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.setUndecorated(true);
        this.setAlwaysOnTop(true);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setSize(rectangle.width, rectangle.height);
        this.setType(JFrame.Type.UTILITY);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.contentPane.addKeyListener(this);
        this.contentPane.setFocusable(true);
        this.contentPane.setFocusTraversalKeysEnabled(false);
        this.setContentPane(this.contentPane);

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }

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
        this.setFont(this.font);

        JPanel backgroundPanel = new JPanel()
        {
            @Override
            public void paintComponent(Graphics graphics)
            {
                try
                {
                    Image image = ImageIO.read(new File("resources/bg.png"));
                    graphics.drawImage(image, 0, 0, this.getSize().width, this.getSize().height, this);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
        backgroundPanel.setBackground(Color.LIGHT_GRAY);

        JPanel panel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(this.contentPane);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, groupLayout.createSequentialGroup()
                        .addComponent(backgroundPanel, GroupLayout.DEFAULT_SIZE, 1217, Short.MAX_VALUE)
                        .addGap(18)
                        .addComponent(panel, GroupLayout.PREFERRED_SIZE, 409, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(panel, GroupLayout.PREFERRED_SIZE, 964, Short.MAX_VALUE)
                        .addGap(11))
                .addComponent(backgroundPanel, GroupLayout.DEFAULT_SIZE, 1028, Short.MAX_VALUE));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{ 45, 248, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gridBagLayout.rowHeights = new int[]{ 23, 272, 0, 0, 47, 0, 0, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        panel.setLayout(gridBagLayout);

        this.usernameField = new JTextField();
        this.usernameField.addKeyListener(this);
        this.usernameField.setFont(this.font.deriveFont(Font.PLAIN, 18.0F));
        GridBagConstraints usernameGbc = new GridBagConstraints();
        usernameGbc.fill = GridBagConstraints.BOTH;
        usernameGbc.gridheight = 2;
        usernameGbc.insets = new Insets(0, 0, 5, 5);
        usernameGbc.gridx = 1;
        usernameGbc.gridy = 4;
        panel.add(this.usernameField, usernameGbc);
        this.usernameField.setColumns(10);

        this.passwordTextField = new JPasswordField();
        this.passwordTextField.addKeyListener(this);
        this.passwordTextField.setFont(this.passwordTextField.getFont().deriveFont(20.0F));
        GridBagConstraints passwordGbc = new GridBagConstraints();
        passwordGbc.fill = GridBagConstraints.BOTH;
        passwordGbc.gridheight = 2;
        passwordGbc.insets = new Insets(0, 0, 5, 5);
        passwordGbc.gridx = 1;
        passwordGbc.gridy = 7;
        panel.add(this.passwordTextField, passwordGbc);
        this.passwordTextField.setColumns(10);

        this.loginButton = new JButton("Login");
        this.loginButton.addActionListener(this);
        this.loginButton.setFont(this.font);
        GridBagConstraints loginGbc = new GridBagConstraints();
        loginGbc.anchor = GridBagConstraints.WEST;
        loginGbc.insets = new Insets(0, 0, 5, 5);
        loginGbc.gridx = 1;
        loginGbc.gridy = 9;
        panel.add(this.loginButton, loginGbc);

        this.infoButton = new JButton("");
        this.infoButton.addActionListener(this);
        GridBagConstraints infoGbc = new GridBagConstraints();
        infoGbc.anchor = GridBagConstraints.WEST;
        infoGbc.insets = new Insets(0, 0, 5, 5);
        infoGbc.gridx = 1;
        infoGbc.gridy = 10;

        try
        {
            Image image = ImageIO.read(new File("resources/help.png")).getScaledInstance(44, 44, Image.SCALE_SMOOTH);
            this.infoButton.setIcon(new ImageIcon(image));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        panel.add(this.infoButton, infoGbc);

        JLabel titleLabel = new JLabel("LPRU Authentication");
        titleLabel.setFont(this.font.deriveFont(32.0F));
        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.fill = GridBagConstraints.HORIZONTAL;
        titleGbc.insets = new Insets(0, 0, 5, 5);
        titleGbc.gridx = 1;
        titleGbc.gridy = 2;
        panel.add(titleLabel, titleGbc);

        JLabel usernameLabel = new JLabel("รหัสนักศึกษา/Student ID");
        usernameLabel.setFont(this.font);
        GridBagConstraints usernameLabelGbc = new GridBagConstraints();
        usernameLabelGbc.fill = GridBagConstraints.HORIZONTAL;
        usernameLabelGbc.insets = new Insets(0, 0, 5, 5);
        usernameLabelGbc.gridx = 1;
        usernameLabelGbc.gridy = 3;
        panel.add(usernameLabel, usernameLabelGbc);

        JLabel passwordLabel = new JLabel("รหัสผ่าน/Password");
        passwordLabel.setFont(this.font);
        GridBagConstraints passwordLabelGbc = new GridBagConstraints();
        passwordLabelGbc.fill = GridBagConstraints.HORIZONTAL;
        passwordLabelGbc.insets = new Insets(0, 0, 5, 5);
        passwordLabelGbc.gridx = 1;
        passwordLabelGbc.gridy = 6;
        panel.add(passwordLabel, passwordLabelGbc);

        this.contentPane.setLayout(groupLayout);
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
                System.out.println("Logged in");

                try
                {
                    this.tray.add(this.trayIcon);
                    this.setVisible(false);
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
        this.setVisible(true);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
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