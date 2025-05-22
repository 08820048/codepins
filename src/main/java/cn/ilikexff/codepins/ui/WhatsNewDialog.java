package cn.ilikexff.codepins.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import cn.ilikexff.codepins.utils.IconUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * "What's New" 对话框
 * 用于显示插件更新内容
 */
public class WhatsNewDialog extends DialogWrapper {

    private final String version;

    /**
     * 构造函数
     *
     * @param project 当前项目
     * @param version 当前版本号
     */
    public WhatsNewDialog(@Nullable Project project, String version) {
        super(project);
        this.version = version;
        setTitle("CodePins " + version + " 更新内容");
        setSize(600, 400);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(JBUI.Borders.empty(10));

        // 创建标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(JBUI.Borders.emptyBottom(10));

        // 加载图标
        Icon icon = IconUtil.loadIcon("/icons/logo.svg", getClass());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(JBUI.Borders.emptyRight(10));

        // 创建标题标签
        JLabel titleLabel = new JBLabel("CodePins " + version + " 更新内容");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));

        // 添加到标题面板
        titlePanel.add(iconLabel, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // 创建内容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10));

        // 创建更新内容
        JPanel updatesPanel = new JPanel();
        updatesPanel.setLayout(new BoxLayout(updatesPanel, BoxLayout.Y_AXIS));

        // 根据版本号添加不同的更新内容
        if (version.equals("1.1.3")) {
            addUpdateItem(updatesPanel, "标签管理增强", 
                    "• 免费版现在可以使用最多10种不同标签\n" +
                    "• 免费版每个图钉最多可添加3个标签\n" +
                    "• 专业版用户可以使用无限标签");
            
            addUpdateItem(updatesPanel, "用户界面优化", 
                    "• 标签筛选面板显示当前标签使用情况\n" +
                    "• 标签编辑对话框增加标签限制提示\n" +
                    "• 改进了标签颜色生成算法");
            
            addUpdateItem(updatesPanel, "性能改进", 
                    "• 优化了标签筛选性能\n" +
                    "• 减少了内存占用");
        } else {
            // 默认更新内容
            addUpdateItem(updatesPanel, "新版本更新", "感谢您使用CodePins插件！");
        }

        // 添加滚动面板
        JBScrollPane scrollPane = new JBScrollPane(updatesPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(JBUI.Borders.emptyTop(10));

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 创建访问网站按钮
        JButton websiteButton = new JButton("访问插件主页");
        websiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks");
            }
        });

        // 添加到按钮面板
        buttonPanel.add(websiteButton);

        // 添加到底部面板
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 组装面板
        dialogPanel.add(titlePanel, BorderLayout.NORTH);
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        dialogPanel.add(bottomPanel, BorderLayout.SOUTH);

        return dialogPanel;
    }

    /**
     * 添加更新项
     *
     * @param panel       面板
     * @param title       标题
     * @param description 描述
     */
    private void addUpdateItem(JPanel panel, String title, String description) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(JBUI.Borders.emptyBottom(15));

        // 创建标题标签
        JLabel titleLabel = new JBLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14));
        titleLabel.setBorder(JBUI.Borders.emptyBottom(5));

        // 创建描述标签
        JLabel descLabel = new JBLabel("<html><pre>" + description.replace("\n", "<br>") + "</pre></html>");
        descLabel.setBorder(JBUI.Borders.empty(0, 10, 0, 0));

        // 添加到项目面板
        itemPanel.add(titleLabel, BorderLayout.NORTH);
        itemPanel.add(descLabel, BorderLayout.CENTER);

        // 添加到主面板
        panel.add(itemPanel);
    }
}
