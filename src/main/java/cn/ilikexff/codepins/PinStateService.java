package cn.ilikexff.codepins;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件持久化服务类，实现 PersistentStateComponent 接口。
 * 该类用于保存和加载所有图钉的状态信息。
 */
@State(
        name = "CodePinsStorage",
        storages = @Storage("codepins.xml")
)
public class PinStateService implements PersistentStateComponent<PinStateService.State> {

    /**
     * 内部状态类：用于表示插件需要保存的所有数据结构。
     */
    public static class State {
        public List<PinState> pins = new ArrayList<>();
    }

    private final State state = new State();

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State loadedState) {
        state.pins = loadedState.pins;
    }

    public static PinStateService getInstance() {
        return ApplicationManager.getApplication().getService(PinStateService.class);
    }

    public List<PinState> getPins() {
        return state.pins;
    }

    /**
     * 添加图钉：将 PinEntry 转换为 PinState（持久化结构），并记录当前行号
     */
    public void addPin(PinEntry entry) {
        // 获取当前文档对象
        Document doc = entry.marker.getDocument();
        int currentLine = doc.getLineNumber(entry.marker.getStartOffset());

        state.pins.add(new PinState(entry.filePath, currentLine, entry.note));
    }

    /**
     * 添加图钉：直接存储 PinState 结构
     */
    public void addPin(PinState pin) {
        state.pins.add(pin);
    }

    public void clear() {
        state.pins.clear();
    }
}