package com.usts.rag.domain.port;

import java.util.List;

public interface ChatCompletionPort {

    String complete(String question, List<String> contexts);
}
