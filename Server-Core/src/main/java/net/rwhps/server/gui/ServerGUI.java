///*
// * Copyright 2020-2023 RW-HPS Team and contributors.
// *
// * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
// * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
// *
// * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
// */
//
//package net.rwhps.server.gui;
//
//import org.apache.commons.compress.utils.Lists;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import javax.swing.border.EtchedBorder;
//import javax.swing.border.TitledBorder;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.Document;
//import java.awt.*;
//import java.awt.event.FocusAdapter;
//import java.awt.event.FocusEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collection;
//import java.util.Objects;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.regex.Pattern;
//
//public class ServerGUI extends JComponent {
//    private static final String c = "RW-HPS server";
//    private static final String d = "RW-HPS - shutting down!";
//    private Thread f;
//    private final Collection<Runnable> g = Lists.newArrayList();
//    final AtomicBoolean h = new AtomicBoolean();
//    private static final Font a = new Font("Monospaced", 0, 12);
//    private static final Pattern ANSI = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})*)?[m|K]");
//
//    public static ServerGUI a() {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//        }
//        final JFrame jframe = new JFrame(c);
//        ServerGUI servergui = new ServerGUI();
//        jframe.setDefaultCloseOperation(2);
//        jframe.add(servergui);
//        jframe.pack();
//        jframe.setLocationRelativeTo((Component) null);
//        jframe.setVisible(true);
//        jframe.setName(c);
//        try {
//            jframe.setIconImage(ImageIO.read((InputStream) Objects.requireNonNull(ServerGUI.class.getClassLoader().getResourceAsStream("logo.png"))));
//        } catch (IOException e2) {
//        }
//        jframe.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent windowevent) {
//                if (!servergui.h.getAndSet(true)) {
//                    jframe.setTitle(ServerGUI.d);
//                    servergui.f();
//                }
//            }
//        });
//        Objects.requireNonNull(jframe);
//        Objects.requireNonNull(jframe);
//        //servergui.a(this::dispose);
//        servergui.a();
//        return servergui;
//    }
//
//    private ServerGUI() {
//        this.e = server;
//        setPreferredSize(new Dimension(854, 480));
//        setLayout(new BorderLayout());
//        try {
//            add(e(), "Center");
//            add(c(), "West");
//        } catch (Exception exception) {
//            b.error("Couldn't build server GUI", exception);
//        }
//    }
//
//    public void a(Runnable task) {
//        this.g.add(task);
//    }
//
//    private JComponent c() {
//        JPanel jpanel = new JPanel(new BorderLayout());
//        com.destroystokyo.paper.gui.GuiStatsComponent guistatscomponent = new com.destroystokyo.paper.gui.GuiStatsComponent(this.e);
//        Collection<Runnable> collection = this.g;
//        Objects.requireNonNull(guistatscomponent);
//        Objects.requireNonNull(guistatscomponent);
//        collection.add(this::close);
//        jpanel.add(guistatscomponent, "North");
//        jpanel.add(d(), "Center");
//        jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
//        return jpanel;
//    }
//
//    private JComponent d() {
//        JList<?> jlist = new PlayerListBox(this.e);
//        JScrollPane jscrollpane = new JScrollPane(jlist, 22, 30);
//        jscrollpane.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
//        return jscrollpane;
//    }
//
//    private JComponent e() {
//        JPanel jpanel = new JPanel(new BorderLayout());
//        JTextArea jtextarea = new JTextArea();
//        JScrollPane jscrollpane = new JScrollPane(jtextarea, 22, 30);
//        jtextarea.setEditable(false);
//        jtextarea.setFont(a);
//        JTextField jtextfield = new JTextField();
//        jtextfield.addActionListener(actionevent -> {
//            String s = jtextfield.getText().trim();
//            if (!s.isEmpty()) {
//                this.e.a(s, this.e.aD());
//            }
//            jtextfield.setText("");
//        });
//        jtextarea.addFocusListener(new FocusAdapter() { // from class: net.minecraft.server.gui.ServerGUI.2
//            public void focusGained(FocusEvent focusevent) {
//            }
//        });
//        jpanel.add(jscrollpane, "Center");
//        jpanel.add(jtextfield, "South");
//        jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
//        this.f = new Thread(() -> {
//            while (true) {
//                String s = LogQueues.getNextLogEvent("ServerGuiConsole");
//                if (s != null) {
//                    a(jtextarea, jscrollpane, s);
//                } else {
//                    return;
//                }
//            }
//        });
//        this.f.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(b));
//        this.f.setDaemon(true);
//        return jpanel;
//    }
//
//    public void a() {
//        this.f.start();
//    }
//
//    public void b() {
//        if (!this.h.getAndSet(true)) {
//            f();
//        }
//    }
//
//    void f() {
//        this.g.forEach((v0) -> {
//            v0.run();
//        });
//    }
//
//    public void a(JTextArea textArea, JScrollPane scrollPane, String message) {
//        if (!SwingUtilities.isEventDispatchThread()) {
//            SwingUtilities.invokeLater(() -> {
//                a(textArea, scrollPane, message);
//            });
//            return;
//        }
//        Document document = textArea.getDocument();
//        JScrollBar jscrollbar = scrollPane.getVerticalScrollBar();
//        boolean flag = false;
//        if (scrollPane.getViewport().getView() == textArea) {
//            flag = (((double) jscrollbar.getValue()) + jscrollbar.getSize().getHeight()) + ((double) (a.getSize() * 4)) > ((double) jscrollbar.getMaximum());
//        }
//        try {
//            document.insertString(document.getLength(), ANSI.matcher(message).replaceAll(""), (AttributeSet) null);
//        } catch (BadLocationException e) {
//        }
//        if (flag) {
//            jscrollbar.setValue(Integer.MAX_VALUE);
//        }
//    }
//}