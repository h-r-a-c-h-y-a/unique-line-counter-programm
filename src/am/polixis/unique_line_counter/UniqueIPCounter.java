package am.polixis.unique_line_counter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;

/**
 * 5
 * <p>
 * 1.3.4.1
 * 2.3.4.2
 * 1.5.2.3
 * 1.3.4.1
 * 1.3.4.1
 * 1.2.4.2
 * 2.2.4.2
 * 2.3.4.2
 */

public class UniqueIPCounter {

    public static void main(String[] args) throws IOException {
        ipGenerator();
        System.out.println(uniqueLinesWithHashSet("ip_addresses.txt"));
        System.out.println(uniqueLinesWithStreamDistinctCount("ip_addresses.txt"));
        System.out.println(uniqueLinesCounter("ip_addresses.txt"));
        System.out.println(uniqueLinesCounter2("ip_addresses.txt"));
    }

    public static void ipGenerator() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("ip_addresses.txt"))) {
            StringBuilder builder = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                builder.append(random.nextInt(256)).append('.')
                        .append(random.nextInt(256)).append('.')
                        .append(random.nextInt(256)).append('.')
                        .append(random.nextInt(256));
                writer.write(builder.toString());
                writer.newLine();
                writer.flush();
                builder.delete(0, builder.length());
            }
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int uniqueLinesWithHashSet(final String path) throws IOException {
        HashSet<String> set = Files.lines(Paths.get(path)).collect(HashSet::new, HashSet::add, HashSet::addAll);
        return set.size();
    }

    public static long uniqueLinesWithStreamDistinctCount(final String path) throws IOException {
        return Files.lines(Paths.get(path)).distinct().count();
    }

    public static long uniqueLinesCounter(final String path) throws IOException {
        long count = 0;
        long currentPointer = 0;
        String generalLine = "";
        String currentLine = "";
        try (RandomAccessFile accessFile = new RandomAccessFile(path, "r")) {
            first:
            while ((generalLine = accessFile.readLine()) != null) {
                currentPointer = accessFile.getFilePointer();
                while ((currentLine = accessFile.readLine()) != null) {
                    if (generalLine.equals(currentLine)) {
                        accessFile.seek(currentPointer);
                        continue first;
                    }
                }
                accessFile.seek(currentPointer);
                count++;
            }
        }
        return count;
    }

    public static long uniqueLinesCounter2(final String path) throws IOException {
        long count = 0;
        StringBuilder generalLine = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            MappedByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            first:
            for (int i = 0; i < buffer.limit(); i++) {
                byte currentByte = buffer.get();
                if (currentByte == 13) {
                    buffer.get();
                    buffer.mark();
                    for (int j = buffer.position(); j < buffer.limit(); j++) {
                        currentByte = buffer.get();
                        if (currentByte == 13) {
                            if (currentLine.toString().equals(generalLine.toString())) {
                                count++;
                                buffer.reset();
                                continue first;
                            }
                            currentLine.delete(0, currentLine.length());
                            buffer.get();
                        } else {
                            currentLine.append((char) currentByte);
                        }
                    }
                    buffer.reset();
                }
                generalLine.append((char) currentByte);
            }
        }
        return count;
    }
}

