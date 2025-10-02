package com.app;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.io.ByteArrayOutputStream;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Mono<Media> saveMedia(Post post, FilePart filePart, String type) {
        Media media = new Media();
        media.setFilename(filePart.filename());
        // Use the file's content type if available, otherwise use the provided type
        media.setContentType(filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : type);
        media.setPost(post);

        return DataBufferUtils.join(filePart.content())
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer); // Important: release the buffer
                media.setData(bytes);
                return mediaRepository.save(media);
            })
            .onErrorResume(e -> {
                System.err.println("Error saving media: " + e.getMessage());
                e.printStackTrace();
                return Mono.error(new RuntimeException("Failed to save media: " + e.getMessage(), e));
            });
    }

    // Alternative method using reduce (your original approach, but improved)
    public Mono<Media> saveMediaAlternative(Post post, FilePart filePart, String type) {
        Media media = new Media();
        media.setFilename(filePart.filename());
        media.setContentType(filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : type);
        media.setPost(post);

        return filePart.content()
            .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                try {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    baos.write(bytes);
                    DataBufferUtils.release(dataBuffer); // Release buffer
                } catch (Exception e) {
                    throw new RuntimeException("Error processing file data", e);
                }
                return baos;
            })
            .map(baos -> {
                try {
                    media.setData(baos.toByteArray());
                    baos.close(); // Close the stream
                    return mediaRepository.save(media);
                } catch (Exception e) {
                    throw new RuntimeException("Error saving media to database", e);
                }
            })
            .onErrorResume(e -> {
                System.err.println("Error saving media: " + e.getMessage());
                e.printStackTrace();
                return Mono.error(new RuntimeException("Failed to save media: " + e.getMessage(), e));
            });
    }

    // Standalone method to save media without post association
    public Media saveMedia(Media media) {
        try {
            return mediaRepository.save(media);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save media: " + e.getMessage(), e);
        }
    }

    // Retrieve the media bytes for a given Media object
    public byte[] getMediaBytes(Media media) {
        if (media == null || media.getData() == null) {
            return null;
        }
        return media.getData();
    }

    // Get media by ID
    public Media getMediaById(Long id) {
        return mediaRepository.findById(id).orElse(null);
    }

    // Delete media
    public void deleteMedia(Long id) {
        try {
            mediaRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete media: " + e.getMessage(), e);
        }
    }
}