package cn.ilikexff.codepins.services;

import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 图标预加载服务
 * 在应用启动时预加载所有SVG图标，确保它们能够正确显示
 */
@Service(Service.Level.APP)
public final class IconPreloader implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(IconPreloader.class);
    private static final String ICONS_PATH = "/icons";
    private static boolean preloaded = false;

    @Override
    public void runActivity(@NotNull Project project) {
        if (preloaded) {
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                preloadIcons();
                preloaded = true;
            } catch (Exception e) {
                LOG.warn("Failed to preload icons", e);
            }
        });
    }

    /**
     * 预加载所有SVG图标
     */
    private void preloadIcons() {
        try {
            LOG.info("Starting to preload SVG icons...");
            List<String> iconPaths = findAllSvgIcons();
            AtomicInteger loadedCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);

            for (String iconPath : iconPaths) {
                try {
                    // 预加载图标
                    IconUtil.loadIcon(iconPath, getClass());
                    loadedCount.incrementAndGet();
                } catch (Exception e) {
                    LOG.warn("Failed to preload icon: " + iconPath, e);
                    failedCount.incrementAndGet();
                }
            }

            LOG.info("Finished preloading SVG icons. Loaded: " + loadedCount.get() + ", Failed: " + failedCount.get());
        } catch (Exception e) {
            LOG.warn("Error during icon preloading", e);
        }
    }

    /**
     * 查找所有SVG图标
     *
     * @return SVG图标路径列表
     */
    private List<String> findAllSvgIcons() {
        List<String> iconPaths = new ArrayList<>();
        try {
            // 获取图标目录的URL
            Enumeration<URL> resources = getClass().getClassLoader().getResources(ICONS_PATH.substring(1));
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("file")) {
                    // 如果是文件系统中的资源
                    Path path = Paths.get(url.toURI());
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.toString().toLowerCase().endsWith(".svg")) {
                                String relativePath = path.relativize(file).toString();
                                iconPaths.add(ICONS_PATH + "/" + relativePath.replace('\\', '/'));
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else if (url.getProtocol().equals("jar")) {
                    // 如果是JAR包中的资源
                    String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                    try (FileSystem fs = FileSystems.newFileSystem(Paths.get(jarPath), (ClassLoader)null)) {
                        Path path = fs.getPath(ICONS_PATH);
                        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                if (file.toString().toLowerCase().endsWith(".svg")) {
                                    String relativePath = path.relativize(file).toString();
                                    iconPaths.add(ICONS_PATH + "/" + relativePath.replace('\\', '/'));
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to find SVG icons", e);
        }
        return iconPaths;
    }
}
