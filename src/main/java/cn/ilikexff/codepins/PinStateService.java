package cn.ilikexff.codepins;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件持久化服务类，实现 PersistentStateComponent 接口
 * 用于保存和加载所有图钉的状态
 */
@State(
        name = "CodePinsStorage",
        storages = @Storage("codepins.xml")
)
public class PinStateService implements PersistentStateComponent<PinStateService.State> {

    /**
     * 表示保存状态的内部结构，包含所有图钉
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
        return com.intellij.openapi.application.ApplicationManager.getApplication().getService(PinStateService.class);
    }

    public List<PinState> getPins() {
        return state.pins;
    }

    public void addPin(PinEntry entry) {
        state.pins.add(new PinState(entry.filePath, entry.line, entry.note));
    }

    public void clear() {
        state.pins.clear();
    }
}