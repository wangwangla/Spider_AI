package kw.test.file;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;

public class IconResizer {

    // 常见 Android 图标尺寸
    private static final int[] ICON_SIZES = {48, 72, 96, 144, 192, 512};

    public static void run() {

        FileHandle inputFile = new FileHandle("assets1/src/icon.png");
        FileHandle outputDir = new FileHandle("assets1/out");

        if (!inputFile.exists()) {
            System.err.println("输入文件不存在: " + inputFile.path());
            return;
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Pixmap original = new Pixmap(inputFile);

        Array<String> outputs = new Array<>();

        for (int size : ICON_SIZES) {
            Pixmap scaled = new Pixmap(size, size, original.getFormat());
            scaled.drawPixmap(original,
                    0, 0, original.getWidth(), original.getHeight(),
                    0, 0, size, size);

            FileHandle outFile = outputDir.child("ic_launcher_" + size + "x" + size + ".png");
            PixmapIO.writePNG(outFile, scaled);
            scaled.dispose();

            outputs.add(outFile.path());
        }

        original.dispose();

        System.out.println("生成的图标文件：");
        for (String path : outputs) {
            System.out.println(" - " + path);
        }
    }
}
