package cn.ilikexff.codepins;

/**
 * 这个类用于测试带标签的注释指令功能
 */
public class TagAnnotationTest {

    /**
     * @cp 这是一个单行图钉测试 #测试 #单行
     */
    public void testSingleLinePin() {
        System.out.println("测试单行图钉");
    }

    /**
     * @cpb 这是一个代码块图钉测试 #测试 #代码块
     */
    public void testBlockPin() {
        System.out.println("测试代码块图钉");
        System.out.println("这个方法应该被完整标记");
        System.out.println("包括所有这些行");
    }

    /**
     * @cpb1-10 这是一个带行号范围的代码块图钉测试 #测试 #行号范围
     */
    public void testBlockPinWithRange() {
        // 这些行应该被标记为图钉
        System.out.println("行1");
        System.out.println("行2");
        System.out.println("行3");
        System.out.println("行4");
        System.out.println("行5");
        // 以上行应该被标记为图钉
    }

    /**
     * @cp 这是一个多标签测试 #重要 #待办 #高优先级
     */
    public void testMultipleTags() {
        System.out.println("测试多个标签");
    }

    /**
     * @cp 这是一个中文标签测试 #中文标签 #测试
     */
    public void testChineseTags() {
        System.out.println("测试中文标签");
    }

    /**
     * 测试旧格式兼容性
     * @pin 这是旧格式测试 #测试 #兼容性
     */
    public void testOldFormatCompatibility() {
        System.out.println("测试旧格式兼容性");
    }

    /**
     * 测试旧格式代码块兼容性
     * @pin-block 这是旧格式代码块测试 #测试 #代码块 #兼容性
     */
    public void testOldFormatBlockCompatibility() {
        System.out.println("测试旧格式代码块兼容性");
        System.out.println("这个方法应该被完整标记");
    }
}
