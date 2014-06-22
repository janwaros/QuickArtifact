package com.github.janwaros.QuickArtifact.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 25.03.2014
 * Time: 22:49
 */
public class QuickArtifactException extends Exception {

    public QuickArtifactException(String message) {
        super(message);
    }

    public QuickArtifactException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuickArtifactException(Throwable cause) {
        super(cause);
    }
}
