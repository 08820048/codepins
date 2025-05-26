package cn.ilikexff.codepins.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Language settings for CodePins
 */
@Service
@State(
        name = "CodePinsLanguageSettings",
        storages = @Storage("codepins-language-settings.xml")
)
public final class LanguageSettings implements PersistentStateComponent<LanguageSettings.State> {
    private State myState = new State();

    public enum Language {
        SYSTEM("settings.language.system", null),
        ENGLISH("settings.language.english", Locale.ENGLISH),
        CHINESE("settings.language.chinese", Locale.SIMPLIFIED_CHINESE);

        private final String displayNameKey;
        private final Locale locale;

        Language(String displayNameKey, Locale locale) {
            this.displayNameKey = displayNameKey;
            this.locale = locale;
        }

        public String getDisplayNameKey() {
            return displayNameKey;
        }

        public Locale getLocale() {
            return locale != null ? locale : Locale.getDefault();
        }
    }

    public static class State {
        public Language language = Language.SYSTEM;
    }

    public static LanguageSettings getInstance() {
        return ApplicationManager.getApplication().getService(LanguageSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public Language getLanguage() {
        return myState.language;
    }

    public void setLanguage(Language language) {
        myState.language = language;
    }

    public Locale getSelectedLocale() {
        return myState.language.getLocale();
    }
}
