package com.hostel.mess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import com.hostel.mess.util.AppConstants;
import com.hostel.mess.util.UIHelper;

/**
 * Modal Login Dialog for Mess Warden / Admin Authentication.
 * Demonstrates:
 * - AWT/Swing Layout Managers: GridBagLayout, BorderLayout, FlowLayout
 * - Swing Components: JDialog, JLabel, JTextField, JPasswordField, JButton
 * - Event Handling: ActionListener, KeyAdapter
 */
public class AdminLoginDialog extends JDialog {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private boolean authenticated = false;

    public AdminLoginDialog(Frame parent) {
        super(parent, "Mess Admin Login", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(400, 260);
        setLocationRelativeTo(getParent());

        // Header Panel
        JPanel headerPanel = UIHelper.createHeaderBanner("Mess Admin Panel", "Please enter Warden/Admin credentials");
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppConstants.COLOR_BG_LIGHT);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(AppConstants.FONT_BODY_BOLD);
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtUsername = UIHelper.createStyledTextField(15);
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(AppConstants.FONT_BODY_BOLD);
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(AppConstants.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 32));
        formPanel.add(txtPassword, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(AppConstants.COLOR_BG_LIGHT);

        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        JButton btnLogin = UIHelper.createPrimaryButton("Login");

        btnCancel.addActionListener(e -> dispose());

        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        };

        btnLogin.addActionListener(loginAction);

        // Enter key triggers login
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnLogin);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (AppConstants.DEFAULT_ADMIN_USER.equals(username) && AppConstants.DEFAULT_ADMIN_PASS.equals(password)) {
            authenticated = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid Username or Password!\nPlease check credentials and try again.",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
