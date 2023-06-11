package org.leti.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageSplitter {
    public static void main(String[] args) {
        int imageWidth = 8192;
        int imageHeight = 4096;
        int fragmentSize = 128;

        int numFragmentsX = imageWidth / fragmentSize;
        int numFragmentsY = imageHeight / fragmentSize;

        String sourcePath = "src/main/resources/pictures/8k_earth_daymap.jpg";
        String destinationFolder = "src/main/resources/pictures/EarthFragments";

        try {
            // Открытие исходного изображения
            File sourceFile = new File(sourcePath);
            BufferedImage image = ImageIO.read(sourceFile);

            // Создание папки назначения, если она не существует
            File destinationDir = new File(destinationFolder);
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            // Разбиение изображения на фрагменты и сохранение каждого фрагмента в папку назначения
            for (int y = 0; y < numFragmentsY; y++) {
                for (int x = 0; x < numFragmentsX; x++) {
                    int startX = x * fragmentSize;
                    int startY = y * fragmentSize;

                    // Кадрирование фрагмента изображения
                    BufferedImage fragment = image.getSubimage(startX, startY, fragmentSize, fragmentSize);

                    // Генерация названия файла назначения
                    String fileName = String.format("%d_%d.jpg", y, x);

                    // Путь к файлу назначения для текущего фрагмента
                    String destinationPath = destinationFolder + "/" + fileName;

                    // Сохранение фрагмента в файл
                    ImageIO.write(fragment, "jpg", new File(destinationPath));
                }
            }

            System.out.println("Изображение успешно разбито на фрагменты");

        } catch (Exception e) {
            System.err.println("Ошибка при разбиении изображения: " + e.getMessage());
        }
    }
}