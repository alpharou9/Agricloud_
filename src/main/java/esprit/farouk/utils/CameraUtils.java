package esprit.farouk.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.nio.ByteBuffer;

/**
 * Utility class for camera management and image conversion.
 */
public class CameraUtils {

    private static final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    /**
     * Creates and starts a camera grabber.
     * @param cameraIndex Camera device index (usually 0 for default webcam)
     * @return Initialized FrameGrabber
     * @throws FrameGrabber.Exception If camera cannot be initialized
     */
    public static FrameGrabber createCameraGrabber(int cameraIndex) throws FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(cameraIndex);
        grabber.setImageWidth(640);
        grabber.setImageHeight(480);
        grabber.setFormat("dshow"); // DirectShow for Windows
        grabber.start();
        return grabber;
    }

    /**
     * Converts OpenCV Mat to JavaFX Image.
     * @param mat OpenCV Mat (BGR format)
     * @return JavaFX Image
     */
    public static Image matToImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }

        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        // Create WritableImage
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        // Get pixel data
        ByteBuffer buffer = mat.createBuffer();
        byte[] data = new byte[width * height * channels];
        buffer.get(data);

        // Convert BGR to ARGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * channels;

                int b = data[index] & 0xFF;
                int g = data[index + 1] & 0xFF;
                int r = data[index + 2] & 0xFF;

                int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                pixelWriter.setArgb(x, y, argb);
            }
        }

        return image;
    }

    /**
     * Converts JavaCV Frame to OpenCV Mat.
     * @param frame JavaCV Frame
     * @return OpenCV Mat
     */
    public static Mat frameToMat(Frame frame) {
        if (frame == null) {
            return null;
        }
        return converter.convert(frame);
    }

    /**
     * Releases camera grabber resources.
     * @param grabber FrameGrabber to release
     */
    public static void releaseGrabber(FrameGrabber grabber) {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error releasing camera: " + e.getMessage());
            }
        }
    }
}
