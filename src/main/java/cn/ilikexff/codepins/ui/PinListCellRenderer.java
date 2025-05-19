package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 自定义图钉列表单元格渲染器
 * 使用现代卡片式设计，提供更丰富的视觉信息
 */
public class PinListCellRenderer extends DefaultListCellRenderer {

    // 缓存图标
    private final Icon blockIcon = IconLoader.getIcon("/icons/code.svg", getClass());
    private final Icon lineIcon = IconLoader.getIcon("/icons/bookmark.svg", getClass());
    private final Icon fileIcon = IconLoader.getIcon("/icons/file.svg", getClass());
    private final Icon timeIcon = IconLoader.getIcon("/icons/clock.svg", getClass());

    // 颜色常量
    private static final Color SELECTED_BG = new JBColor(new Color(45, 90, 148, 200), new Color(45, 90, 148, 200));
    private static final Color HOVER_BG = new JBColor(new Color(50, 50, 60, 100), new Color(50, 50, 60, 100));
    private static final Color NORMAL_BG = new JBColor(new Color(60, 63, 65, 80), new Color(60, 63, 65, 80));
    private static final Color BORDER_COLOR = new JBColor(new Color(80, 80, 90, 120), new Color(80, 80, 90, 120));

    // 边框常量
    private static final Border NORMAL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            JBUI.Borders.empty(8, 10)
    );
    private static final Border SELECTED_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new JBColor(new Color(100, 150, 200), new Color(100, 150, 200)), 1),
            JBUI.Borders.empty(8, 10)
    );

    // 记录鼠标悬停的索引
    private int hoverIndex = -1;

    public void setHoverIndex(int index) {
        this.hoverIndex = index;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(true);

        // 设置背景颜色和边框
        if (isSelected) {
            panel.setBackground(SELECTED_BG);
            panel.setBorder(SELECTED_BORDER);
        } else if (index == hoverIndex) {
            panel.setBackground(HOVER_BG);
            panel.setBorder(NORMAL_BORDER);
        } else {
            panel.setBackground(NORMAL_BG);
            panel.setBorder(NORMAL_BORDER);
        }

        if (value instanceof PinEntry entry) {
            // 使用 ReadAction 包装文档访问操作，确保线程安全
            com.intellij.openapi.application.ReadAction.run(() -> {
                try {
                    // 左侧图标面板
                    JPanel iconPanel = createIconPanel(entry);
                    panel.add(iconPanel, BorderLayout.WEST);

                    // 中间内容面板
                    JPanel contentPanel = createContentPanel(entry);
                    panel.add(contentPanel, BorderLayout.CENTER);

                    // 右侧信息面板
                    JPanel infoPanel = createInfoPanel(entry);
                    panel.add(infoPanel, BorderLayout.EAST);

                } catch (Exception e) {
                    // 如果发生异常，显示简化的面板
                    JLabel errorLabel = new JLabel("加载图钉信息失败");
                    errorLabel.setForeground(JBColor.RED);
                    panel.add(errorLabel, BorderLayout.CENTER);
                    e.printStackTrace();
                }
            });
        }

        return panel;
    }

    /**
     * 创建左侧图标面板
     */
    private JPanel createIconPanel(PinEntry entry) {
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);

        // 选择适当的图标
        Icon icon = entry.isBlock ? blockIcon : lineIcon;
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(JBUI.Borders.empty(0, 5, 0, 5));

        iconPanel.add(iconLabel, BorderLayout.CENTER);
        return iconPanel;
    }

    /**
     * 创建中间内容面板
     */
    private JPanel createContentPanel(PinEntry entry) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        try {
            // 文件名
            String fileName = getFileName(entry.filePath);
            JLabel fileNameLabel = new JLabel(fileName);
            fileNameLabel.setFont(fileNameLabel.getFont().deriveFont(Font.BOLD, 13f));
            fileNameLabel.setForeground(new JBColor(new Color(220, 220, 220), new Color(220, 220, 220)));
            fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 备注（如果有）
            JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            notePanel.setOpaque(false);
            notePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (entry.note != null && !entry.note.isEmpty()) {
                JLabel noteLabel = new JLabel(entry.note);
                noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 12f));
                noteLabel.setForeground(new JBColor(new Color(120, 220, 120), new Color(120, 220, 120)));
                notePanel.add(noteLabel);
            } else {
                JLabel emptyNoteLabel = new JLabel("(无备注)");
                emptyNoteLabel.setFont(emptyNoteLabel.getFont().deriveFont(Font.ITALIC, 12f));
                emptyNoteLabel.setForeground(new JBColor(new Color(150, 150, 150), new Color(150, 150, 150)));
                notePanel.add(emptyNoteLabel);
            }

            // 标签面板
            JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            tagsPanel.setOpaque(false);
            tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            List<String> tags = entry.getTags();
            if (!tags.isEmpty()) {
                for (String tag : tags) {
                    JLabel tagLabel = createTagLabel(tag);
                    tagsPanel.add(tagLabel);
                }
            }

            // 添加到内容面板
            contentPanel.add(fileNameLabel);
            contentPanel.add(Box.createVerticalStrut(3));
            contentPanel.add(notePanel);

            if (!tags.isEmpty()) {
                contentPanel.add(Box.createVerticalStrut(5));
                contentPanel.add(tagsPanel);
            }

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("内容加载失败");
            errorLabel.setForeground(JBColor.RED);
            contentPanel.add(errorLabel);
            e.printStackTrace();
        }

        return contentPanel;
    }

    /**
     * 创建右侧信息面板
     */
    private JPanel createInfoPanel(PinEntry entry) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        try {
            // 行号信息
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            linePanel.setOpaque(false);
            linePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

            int line = 0;
            if (entry.marker != null && entry.marker.isValid()) {
                Document doc = entry.marker.getDocument();
                if (doc != null) {
                    line = entry.getCurrentLine(doc);
                }
            }

            JLabel lineLabel = new JLabel("Line " + (line + 1));
            lineLabel.setFont(lineLabel.getFont().deriveFont(12f));
            lineLabel.setForeground(new JBColor(new Color(247, 140, 108), new Color(247, 140, 108)));

            linePanel.add(lineLabel);

            // 时间信息
            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            timePanel.setOpaque(false);
            timePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JLabel timeIcon = new JLabel(this.timeIcon);
            timeIcon.setPreferredSize(new Dimension(12, 12));

            String timeStr = new SimpleDateFormat("MM-dd HH:mm").format(new Date(entry.timestamp));
            JLabel timeLabel = new JLabel(timeStr);
            timeLabel.setFont(timeLabel.getFont().deriveFont(11f));
            timeLabel.setForeground(new JBColor(new Color(150, 150, 150), new Color(150, 150, 150)));

            timePanel.add(timeIcon);
            timePanel.add(timeLabel);

            // 添加到信息面板
            infoPanel.add(linePanel);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(timePanel);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("信息加载失败");
            errorLabel.setForeground(JBColor.RED);
            infoPanel.add(errorLabel);
            e.printStackTrace();
        }

        return infoPanel;
    }

    /**
     * 获取文件名
     */
    private String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "未知文件";
        }

        String fileName = path;
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf(File.separatorChar));
        if (lastSlash >= 0) {
            fileName = path.substring(lastSlash + 1);
        }

        return fileName;
    }

    /**
     * 创建标签标签
     */
    private JLabel createTagLabel(String tag) {
        JLabel tagLabel = new JLabel(tag);
        tagLabel.setFont(tagLabel.getFont().deriveFont(Font.BOLD, 10f));

        // 生成标签颜色
        Color tagColor = getTagColor(tag);

        // 根据背景色决定文本颜色
        boolean isDark = ColorUtil.isDark(tagColor);
        Color textColor = isDark ? new JBColor(Color.WHITE, Color.WHITE) : new JBColor(new Color(50, 50, 50), new Color(50, 50, 50));

        tagLabel.setForeground(textColor);
        tagLabel.setBackground(tagColor);
        tagLabel.setOpaque(true);

        // 设置圆角边框
        tagLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new JBColor(new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), 100),
                                                          new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), 100)), 1),
                JBUI.Borders.empty(2, 6)
        ));

        // 添加标签图标
        tagLabel.setIcon(IconLoader.getIcon("/icons/tag-small.svg", getClass()));
        tagLabel.setIconTextGap(4);

        return tagLabel;
    }

    /**
     * 根据标签名称生成颜色
     */
    private Color getTagColor(String tag) {
        // 使用标签的哈希值生成颜色，确保相同标签有相同颜色
        int hash = tag.hashCode();

        // 现代感强的色调
        Color[] lightPalette = {
                new Color(79, 195, 247),  // 浅蓝
                new Color(129, 199, 132), // 浅绿
                new Color(255, 183, 77),  // 浅橙
                new Color(240, 98, 146),  // 浅红
                new Color(149, 117, 205), // 浅紫
                new Color(224, 224, 224), // 浅灰
                new Color(77, 208, 225),  // 浅青
                new Color(174, 213, 129)  // 浅黄绿
        };

        Color[] darkPalette = {
                new Color(41, 121, 255),  // 深蓝
                new Color(67, 160, 71),   // 深绿
                new Color(255, 152, 0),   // 深橙
                new Color(233, 30, 99),   // 深红
                new Color(103, 58, 183),  // 深紫
                new Color(117, 117, 117), // 深灰
                new Color(0, 172, 193),   // 深青
                new Color(104, 159, 56)   // 深黄绿
        };

        int index = Math.abs(hash) % lightPalette.length;
        return new JBColor(lightPalette[index], darkPalette[index]);
    }

    /**
     * 颜色工具类
     */
    private static class ColorUtil {
        /**
         * 判断颜色是否为深色
         */
        public static boolean isDark(Color color) {
            // 使用人眼对不同颜色的敏感度公式
            double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
            return brightness < 0.5;
        }
    }
}
