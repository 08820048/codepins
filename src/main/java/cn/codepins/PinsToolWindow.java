package ilikexff.codepins;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;

public class PinsToolWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (PinEntry pin : PinStorage.getPins()) {
            model.addElement(pin.toString());
        }

        JList<String> pinList = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(pinList);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}