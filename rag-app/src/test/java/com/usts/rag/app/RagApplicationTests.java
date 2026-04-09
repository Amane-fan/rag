package com.usts.rag.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class RagApplicationTests {

    @Test
    void mainClassShouldExist() {
        Assertions.assertDoesNotThrow(() -> RagApplication.class.getDeclaredConstructor().newInstance());
    }
}
