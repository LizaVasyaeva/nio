import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static void copyFileSync(Path source, Path target) throws IOException {
        try (FileChannel inChannel = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel outChannel = FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
        }
    }

    public static void copyFileAsync(Path source, Path target) throws IOException, ExecutionException, InterruptedException {
        try (AsynchronousFileChannel inChannel = AsynchronousFileChannel.open(source, StandardOpenOption.READ);
             AsynchronousFileChannel outChannel = AsynchronousFileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            long position = 0;
            int bytesRead;

            while (true) {
                Future<Integer> readResult = inChannel.read(buffer, position);
                bytesRead = readResult.get();
                if (bytesRead == -1) break;

                buffer.flip();
                Future<Integer> writeResult = outChannel.write(buffer, position);
                writeResult.get();

                buffer.clear();
                position += bytesRead;
            }
        }
    }

    public static void main(String[] args) {
        Path source1 = Paths.get("input1.txt");
        Path target1 = Paths.get("output_sync.txt");
        Path source2 = Paths.get("input2.txt");
        Path target2 = Paths.get("output_async.txt");

        try {
            // Синхронное копирование
            long start = System.nanoTime();
            copyFileSync(source1, target1);
            long end = System.nanoTime();
            System.out.println("Синхронное копирование прошло за " + ((end - start) / 1_000_000) + " мс.");

            // Асинхронное копирование
            start = System.nanoTime();
            copyFileAsync(source2, target2);
            end = System.nanoTime();
            System.out.println("Асинхронное копирование прошло за " + ((end - start) / 1_000_000) + " мс.");

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}