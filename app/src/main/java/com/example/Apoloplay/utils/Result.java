package com.example.Apoloplay.utils;

public abstract class Result<T> {
    private Result() {}

    // Classe para representar o sucesso
    public static final class Success<T> extends Result<T> {
        public final T data;
        public Success(T data) { this.data = data; }
    }

    // Classe para representar o erro
    public static final class Error<T> extends Result<T> {
        public final Exception exception;
        public Error(Exception exception) { this.exception = exception; }
    }
}