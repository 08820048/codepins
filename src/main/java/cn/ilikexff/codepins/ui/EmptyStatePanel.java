package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 空状态面板
 * 当没有图钉时显示友好的引导信息
 */
public class EmptyStatePanel extends JPanel {

    // 文档地址常量
    private static final String DOCS_URL = "https://docs.codepins.cn/";

    public EmptyStatePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));
        setBorder(JBUI.Borders.empty(30));

        // 添加图标
        Icon emptyIcon = IconUtil.loadIcon("/icons/empty-pins.svg", getClass());
        JLabel iconLabel = new JLabel(emptyIcon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加标题
        JLabel titleLabel = new JLabel(CodePinsBundle.message("empty.title"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new JBColor(new Color(220, 220, 220), new Color(220, 220, 220)));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加说明文字
        JTextPane descriptionPane = new JTextPane();
        descriptionPane.setContentType("text/html");
        String description = CodePinsBundle.message("empty.description");
        // 将\n替换为<br/>
        description = description.replace("\n", "<br/>");
        descriptionPane.setText(
                "<html><div style='text-align:center; font-size:12px; color:#BBBBBB;'>" +
                description +
                "</div></html>"
        );
        descriptionPane.setEditable(false);
        descriptionPane.setOpaque(false);
        descriptionPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton helpButton = new JButton(CodePinsBundle.message("empty.help.button"));
        helpButton.setFocusPainted(false);

        // 添加点击事件，打开文档地址
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse(DOCS_URL);
            }
        });

        buttonPanel.add(helpButton);

        // 添加所有组件
        add(Box.createVerticalGlue());
        add(iconLabel);
        add(Box.createVerticalStrut(20));
        add(titleLabel);
        add(Box.createVerticalStrut(15));
        add(descriptionPane);
        add(Box.createVerticalStrut(25));
        add(buttonPanel);
        add(Box.createVerticalGlue());
    }
}
