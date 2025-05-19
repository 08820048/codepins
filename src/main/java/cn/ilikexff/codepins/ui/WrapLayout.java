package cn.ilikexff.codepins.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 自动换行布局
 * 当容器宽度不足时，组件会自动换行显示
 */
public class WrapLayout extends FlowLayout {
    private Dimension preferredLayoutSize;

    /**
     * 构造一个新的 WrapLayout
     */
    public WrapLayout() {
        super();
    }

    /**
     * 构造一个新的 FlowLayout，指定对齐方式和水平、垂直间距
     *
     * @param align 对齐方式
     * @param hgap  水平间距
     * @param vgap  垂直间距
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * 返回指定容器的首选尺寸
     *
     * @param target 要布局的容器
     * @return 容器的首选尺寸
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    /**
     * 返回指定容器的最小尺寸
     *
     * @param target 要布局的容器
     * @return 容器的最小尺寸
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    /**
     * 计算容器的尺寸
     *
     * @param target     要布局的容器
     * @param preferred  如果为 true，计算首选尺寸，否则计算最小尺寸
     * @return 容器的尺寸
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            // 每次重新计算尺寸时，重置首选布局尺寸
            preferredLayoutSize = null;

            int targetWidth = target.getSize().width;
            Container container = target;

            while (container.getSize().width == 0 && container.getParent() != null) {
                container = container.getParent();
            }

            targetWidth = container.getSize().width;

            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            // 计算所有组件的首选尺寸
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    // 如果组件太宽，调整其宽度
                    if (d.width > maxWidth) {
                        d.width = maxWidth;
                    }

                    // 如果不能添加到当前行，创建新行
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    // 添加到当前行
                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }

                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            // 当使用滚动窗格或应该调整大小的窗口时，
            // 确保首选高度适合可用空间
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.height = Math.min(dim.height, scrollPane.getHeight());
            }

            preferredLayoutSize = dim;
            return dim;
        }
    }

    /**
     * 将行的尺寸添加到总尺寸
     *
     * @param dim       总尺寸
     * @param rowWidth  行宽度
     * @param rowHeight 行高度
     */
    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);

        if (dim.height > 0) {
            dim.height += getVgap();
        }

        dim.height += rowHeight;
    }
}
