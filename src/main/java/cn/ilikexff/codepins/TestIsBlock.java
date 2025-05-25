package cn.ilikexff.codepins;

import cn.ilikexff.codepins.core.PinEntry;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * 测试类：用于验证 isBlock 属性是否正确设置
 */
public class TestIsBlock extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 创建一个测试图钉
        Document doc = null;
        RangeMarker marker = null;
        
        // 测试代码块图钉
        PinEntry blockPin = new PinEntry(
                "/test/path.java",
                marker,
                "测试代码块图钉",
                System.currentTimeMillis(),
                "test",
                true // 设置为代码块图钉
        );
        
        // 测试单行图钉
        PinEntry linePin = new PinEntry(
                "/test/path.java",
                marker,
                "测试单行图钉",
                System.currentTimeMillis(),
                "test",
                false // 设置为单行图钉
        );
        
        // 显示测试结果
        String message = "代码块图钉 isBlock = " + blockPin.isBlock + "\n" +
                         "单行图钉 isBlock = " + linePin.isBlock;
        
        Messages.showInfoMessage(project, message, "测试 isBlock 属性");
    }
}
