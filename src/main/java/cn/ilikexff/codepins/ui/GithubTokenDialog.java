package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.services.GistService;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * GitHub令牌设置对话框
 */
public class GithubTokenDialog extends DialogWrapper {

    private final JBTextField tokenField;
    private final GistService gistService;

    /**
     * 构造函数
     *
     * @param project 当前项目
     */
    public GithubTokenDialog(Project project) {
        super(project);
        this.gistService = GistService.getInstance();
        
        setTitle("设置GitHub令牌");
        setSize(500, 250);
        
        tokenField = new JBTextField();
        if (gistService.isConfigured()) {
            tokenField.setText(gistService.getGithubToken());
        }
        
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(JBUI.Borders.empty(10));
        
        // 创建说明面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("GitHub令牌说明"));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(UIManager.getColor("Panel.background"));
        infoArea.setText("为了使用GitHub Gist分享功能，您需要提供一个GitHub个人访问令牌。\n\n" +
                "该令牌将安全地存储在您的IDE设置中，并仅用于创建Gist。\n\n" +
                "创建令牌时，请确保选择'gist'权限范围。");
        
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        // 创建链接面板
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel linkLabel = new JBLabel("<html><a href=''>点击这里创建GitHub令牌</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse("https://github.com/settings/tokens/new?scopes=gist&description=CodePins%20Gist%20Sharing");
            }
        });
        linkPanel.add(linkLabel);
        
        infoPanel.add(linkPanel, BorderLayout.SOUTH);
        
        // 创建令牌输入面板
        JPanel tokenPanel = new JPanel(new BorderLayout());
        tokenPanel.setBorder(BorderFactory.createTitledBorder("GitHub令牌"));
        
        tokenPanel.add(new JBLabel("请输入您的GitHub个人访问令牌:"), BorderLayout.NORTH);
        tokenPanel.add(tokenField, BorderLayout.CENTER);
        
        // 组装面板
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(tokenPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    @Override
    protected void doOKAction() {
        // 保存令牌
        gistService.setGithubToken(tokenField.getText().trim());
        super.doOKAction();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (tokenField.getText().trim().isEmpty()) {
            return new ValidationInfo("请输入GitHub令牌", tokenField);
        }
        return null;
    }
}
