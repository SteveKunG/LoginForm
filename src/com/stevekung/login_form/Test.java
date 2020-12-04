package com.stevekung.login_form;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

public class Test implements ActionListener
{
    private JFrame frame;
    private JTextField usernameField;
    private JTextField passwordTextField;
    JButton loginButton;

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^(?=.*\\d).{11}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\da-zA-Z]).{8,15}$");

    public static void main(String[] args)
    {
        EventQueue.invokeLater(Test::new);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public Test()
    {
        this.initialize();
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == this.loginButton)
        {
            this.performedLogin();
        }
    }

    private void initialize()
    {
        this.frame = new JFrame();
        this.frame.setVisible(true);
        this.frame.setBounds(100, 100, 450, 300);
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.getContentPane().setLayout(null);

        this.usernameField = new JTextField();
        this.usernameField.setBounds(140, 112, 86, 20);
        this.frame.getContentPane().add(this.usernameField);
        this.usernameField.setColumns(10);

        this.passwordTextField = new JTextField();
        this.passwordTextField.setBounds(140, 154, 86, 20);
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

    private void performedLogin()
    {
        boolean checkedUsername = this.checkUsername();

//        if (!checkedUsername)
//        {
//            return;
//        }

        boolean checkedPassword = this.checkPassword();

//        if (!checkedPassword)
//        {
//            return;
//        }

        try
        {
            URL url = new URL("http://www.example.com/practice.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("username", "root");
            arguments.put("password", "sjh76HSn!");
            StringJoiner joiner = new StringJoiner("&");

            for (Map.Entry<String, String> entry : arguments.entrySet())
            {
                joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            byte[] out = joiner.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();

            try (OutputStream os = http.getOutputStream())
            {
                os.write(out);
                System.out.println(os);
            }
            System.out.println(http.getInputStream());
            System.out.println(http.getInputStream());
        }
        catch (MalformedURLException | ProtocolException e)
        {
            Test.displayErrorMessage("An exception occurred when trying to connect the database", e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Test.displayErrorMessage("An exception occurred when trying to post data", e.getMessage());
            e.printStackTrace();
        }

        if (checkedUsername && checkedPassword)
        {
            System.out.println("all matched");
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
            System.out.println("username matched pattern");
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
        String password = this.passwordTextField.getText();

        if (password == null || password.isEmpty())
        {
            Test.displayInfoMessage("Empty password!", "Please complete fill in the box");
            return false;
        }

        Matcher passMat = Test.PASSWORD_PATTERN.matcher(password);

        if (passMat.matches())
        {
            System.out.println("password matched pattern");
            return true;
        }
        else
        {
            System.out.println("password not matched pattern");
            return false;
        }
    }

    private static void displayInfoMessage(String message, String info)
    {
        JOptionPane.showMessageDialog(null, "Reason: " + info, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void displayErrorMessage(String message, String info)
    {
        JOptionPane.showMessageDialog(null, "Reason: " + info, message, JOptionPane.ERROR_MESSAGE);
    }
}