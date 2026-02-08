package com.photoviewer.ai;

import java.awt.image.BufferedImage;

/**
 * Base interface for AI provider implementations.
 */
public interface AIClient {
    /**
     * Send a message to the AI with optional image context.
     * 
     * @param prompt The user's message/command
     * @param image  The current image (can be null)
     * @return AI's response
     */
    String sendMessage(String prompt, BufferedImage image) throws Exception;

    /**
     * Execute a command that modifies the image.
     * 
     * @param command The command to execute
     * @return Result message
     */
    String executeCommand(String command) throws Exception;
}
